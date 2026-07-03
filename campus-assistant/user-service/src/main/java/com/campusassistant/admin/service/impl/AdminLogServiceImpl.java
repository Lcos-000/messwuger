package com.campusassistant.admin.service.impl;

import com.campusassistant.admin.config.AdminLogProperties;
import com.campusassistant.admin.pojo.vo.AdminLogFileItemVO;
import com.campusassistant.admin.pojo.vo.AdminLogFileListVO;
import com.campusassistant.admin.pojo.vo.AdminLogTailVO;
import com.campusassistant.admin.service.AdminLogService;
import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLogServiceImpl implements AdminLogService {

    private static final Pattern SAFE_FILE_NAME = Pattern.compile("^[A-Za-z0-9._-]+$");
    private static final String LOG_SUFFIX = ".log";
    private static final String GZ_SUFFIX = ".gz";
    private static final int FIXED_TAIL_LINES = 500;
    private final AdminLogProperties adminLogProperties;

    // 前端获取日志文件列表
    @Override
    public AdminLogFileListVO listLogFiles() {
        // 获取日志目录
        Path baseDir = getBaseDir();
        // 遍历日志目录下的所有文件
        try (Stream<Path> stream = Files.list(baseDir)) {
            List<AdminLogFileItemVO> files = stream
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedLogFile)
                    .sorted(Comparator.comparing(this::getLastModifiedTime).reversed())
                    .map(this::toFileItem)
                    .toList();

            return AdminLogFileListVO.builder()
                    .files(files)
                    .build();
        } catch (IOException e) {
            log.error("获取日志文件列表失败", e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "获取日志文件列表失败");
        }
    }

    // 前端初始化读取日志文件
    @Override
    public AdminLogTailVO initTail(String fileName) {
        Path logFile = resolveLogFile(fileName, false);

        try {
            long fileSize = Files.size(logFile);
            BackwardReadResult result = readPreviousLines(logFile, fileSize, FIXED_TAIL_LINES);

            return AdminLogTailVO.builder()
                    .fileName(logFile.getFileName().toString())
                    .lines(result.lines())
                    .startOffset(result.startOffset())
                    .offset(fileSize)
                    .fileSize(fileSize)
                    .reset(false)
                    .hasMoreOldLines(result.hasMoreOldLines())
                    .hasMoreNewLines(false)
                    .build();
        } catch (IOException e) {
            log.error("初始化读取日志失败, fileName={}", fileName, e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "初始化读取日志失败");
        }
    }

    // 前端轮询读取日志文件
    @Override
    public AdminLogTailVO pollTail(String fileName, Long offset) {
        Path logFile = resolveLogFile(fileName, false);
        int limit = FIXED_TAIL_LINES;
        long safeOffset = offset == null ? 0L : Math.max(offset, 0L);

        try {
            long fileSize = Files.size(logFile);

            if (safeOffset > fileSize) {
                BackwardReadResult result = readPreviousLines(logFile, fileSize, limit);

                return AdminLogTailVO.builder()
                        .fileName(logFile.getFileName().toString())
                        .lines(result.lines())
                        .startOffset(result.startOffset())
                        .offset(fileSize)
                        .fileSize(fileSize)
                        .reset(true)
                        .hasMoreOldLines(result.hasMoreOldLines())
                        .hasMoreNewLines(false)
                        .build();
            }

            IncrementalReadResult result = readIncrementalLines(logFile, safeOffset, limit);

            return AdminLogTailVO.builder()
                    .fileName(logFile.getFileName().toString())
                    .lines(result.lines())
                    .startOffset(null)
                    .offset(result.nextOffset())
                    .fileSize(fileSize)
                    .reset(false)
                    .hasMoreOldLines(null)
                    .hasMoreNewLines(result.hasMoreNewLines())
                    .build();
        } catch (IOException e) {
            log.error("轮询读取日志失败, fileName={}, offset={}", fileName, safeOffset, e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "轮询读取日志失败");
        }
    }

    @Override
    public AdminLogTailVO historyTail(String fileName, Long beforeOffset) {
        Path logFile = resolveLogFile(fileName, false);
        int limit = FIXED_TAIL_LINES;
        long safeBeforeOffset = beforeOffset == null ? 0L : Math.max(beforeOffset, 0L);

        try {
            long fileSize = Files.size(logFile);

            if (safeBeforeOffset > fileSize) {
                BackwardReadResult result = readPreviousLines(logFile, fileSize, limit);

                return AdminLogTailVO.builder()
                        .fileName(logFile.getFileName().toString())
                        .lines(result.lines())
                        .startOffset(result.startOffset())
                        .offset(fileSize)
                        .fileSize(fileSize)
                        .reset(true)
                        .hasMoreOldLines(result.hasMoreOldLines())
                        .hasMoreNewLines(false)
                        .build();
            }

            BackwardReadResult result = readPreviousLines(logFile, safeBeforeOffset, limit);

            return AdminLogTailVO.builder()
                    .fileName(logFile.getFileName().toString())
                    .lines(result.lines())
                    .startOffset(result.startOffset())
                    .offset(null)
                    .fileSize(fileSize)
                    .reset(false)
                    .hasMoreOldLines(result.hasMoreOldLines())
                    .hasMoreNewLines(false)
                    .build();
        } catch (IOException e) {
            log.error("向前翻查日志失败, fileName={}, beforeOffset={}", fileName, safeBeforeOffset, e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "向前翻查日志失败");
        }
    }


    @Override
    public ResponseEntity<Resource> downloadLog(String fileName) {
        Path logFile = resolveLogFile(fileName, true);

        try {
            Resource resource = new UrlResource(logFile.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(ResultCodeEnum.NOT_FOUND.getCode(), "日志文件不存在");
            }

            MediaType mediaType = fileName.endsWith(GZ_SUFFIX)
                    ? MediaType.parseMediaType("application/gzip")
                    : MediaType.TEXT_PLAIN;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(fileName, StandardCharsets.UTF_8)
                                    .build()
                                    .toString())
                    .body(resource);
        } catch (IOException e) {
            log.error("下载日志文件失败, fileName={}", fileName, e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "下载日志文件失败");
        }
    }

    // 获取管理员日志目录
    private Path getBaseDir() {
        String providedBaseDir = adminLogProperties.getBaseDir();
        if (!StringUtils.hasText(providedBaseDir)) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "管理员日志目录未配置");
        }
        // 防止路径遍历攻击，将路径归一化为绝对路径，确保在管理员日志目录下
        Path baseDir = Paths.get(providedBaseDir).normalize();
        if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "管理员日志目录不存在");
        }

        return baseDir;
    }

    // 解析日志文件路径
    private Path resolveLogFile(String fileName, boolean allowCompressed) {
        if (!StringUtils.hasText(fileName)) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "文件名不能为空");
        }

        if (!SAFE_FILE_NAME.matcher(fileName).matches()) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "非法文件名");
        }

        boolean validSuffix = fileName.endsWith(LOG_SUFFIX) || (allowCompressed && fileName.endsWith(GZ_SUFFIX));
        if (!validSuffix) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "不支持的日志文件类型");
        }

        Path baseDir = getBaseDir();
        Path target = baseDir.resolve(fileName).normalize();

        if (!target.startsWith(baseDir)) {
            throw new BusinessException(ResultCodeEnum.FORBIDDEN.getCode(), "禁止访问该文件");
        }

        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND.getCode(), "日志文件不存在");
        }

        return target;
    }

    // 检查是否为支持的日志文件
    private boolean isSupportedLogFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(LOG_SUFFIX) || fileName.endsWith(GZ_SUFFIX);
    }

    // 获取文件最后修改时间
    private long getLastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            // 降级处理：返回0表示无法获取最后修改时间
            return Instant.EPOCH.toEpochMilli();
        }
    }

    // 转换为日志文件项VO
    private AdminLogFileItemVO toFileItem(Path path) {
        try {
            String fileName = path.getFileName().toString();

            return AdminLogFileItemVO.builder()
                    .fileName(fileName)
                    .size(Files.size(path))
                    .lastModified(Files.getLastModifiedTime(path).toMillis())
                    .compressed(fileName.endsWith(GZ_SUFFIX))
                    .active(fileName.endsWith(LOG_SUFFIX))
                    .build();
        } catch (IOException e) {
            log.warn("读取日志文件信息失败, path={}", path, e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "读取日志文件信息失败");
        }
    }

    // 读取日志文件之前几行数据
    private BackwardReadResult readPreviousLines(Path logFile, long beforeOffset, int limit) throws IOException {
        Deque<LineSlice> deque = new ArrayDeque<>(limit);

        try (RandomAccessFile raf = new RandomAccessFile(logFile.toFile(), "r")) {
            long fileLength = raf.length();
            long safeBeforeOffset = Math.max(0L, Math.min(beforeOffset, fileLength));

            raf.seek(0L);

            while (raf.getFilePointer() < safeBeforeOffset) {
                long lineStart = raf.getFilePointer();
                String rawLine = raf.readLine();
                if (rawLine == null) {
                    break;
                }

                long nextPointer = raf.getFilePointer();
                if (lineStart >= safeBeforeOffset) {
                    break;
                }

                if (deque.size() == limit) {
                    deque.pollFirst();
                }

                deque.offerLast(new LineSlice(
                        lineStart,
                        new String(rawLine.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)
                ));

                if (nextPointer >= safeBeforeOffset) {
                    break;
                }
            }

            List<String> lines = deque.stream()
                    .map(LineSlice::content)
                    .toList();

            long startOffset = deque.isEmpty() ? safeBeforeOffset : deque.peekFirst().startOffset();
            boolean hasMoreOldLines = startOffset > 0;

            return new BackwardReadResult(lines, startOffset, hasMoreOldLines);
        }
    }

    // 读取增量日志行
    private IncrementalReadResult readIncrementalLines(Path logFile, long offset, int limit) throws IOException {
        List<String> lines = new ArrayList<>(limit);

        try (RandomAccessFile raf = new RandomAccessFile(logFile.toFile(), "r")) {
            raf.seek(offset);

            while (lines.size() < limit) {
                String line = raf.readLine();
                if (line == null) {
                    return new IncrementalReadResult(lines, raf.getFilePointer(), false);
                }
                lines.add(new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
            }

            boolean hasMoreNewLines = raf.getFilePointer() < raf.length();
            return new IncrementalReadResult(lines, raf.getFilePointer(), hasMoreNewLines);
        }
    }

    private record IncrementalReadResult(
            List<String> lines,      // 1. 本次读取到的数据行
            long nextOffset,         // 2. 下次读取的起始位置（指针）
            boolean hasMoreNewLines  // 3. 是否还有更多新数据
    ) {}

    private record LineSlice(
            long startOffset,
            String content
    ) {}

    private record BackwardReadResult(
            List<String> lines,
            long startOffset,
            boolean hasMoreOldLines
    ) {}

}