package com.campusassistant.admin.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLogFileItemVO {
    // 日志文件名
    private String fileName;
    // 日志文件大小
    private Long size;
    // 日志文件最后修改时间
    private Long lastModified;
    // 是否压缩
    private Boolean compressed;
    // 是否活跃
    private Boolean active;
}