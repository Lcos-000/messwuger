package com.campusassistant.remote.exception.fallback;

import com.campusassistant.remote.exception.code.CourseRemoteCodeEnum;
import com.campusassistant.pojo.Result;
import com.campusassistant.enums.RemoteCodeEnum;
import com.campusassistant.remote.course.client.CourseServiceClient;
import com.campusassistant.remote.course.pojo.RemoteCourseDTO;
import com.campusassistant.remote.course.pojo.RemoteCourseVO;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class UserCourseFallbackFactory implements FallbackFactory<CourseServiceClient> {

    @Override
    public CourseServiceClient create(Throwable cause) {
        int code;
        String message;

        if (cause instanceof BlockException blockException) {
            RemoteCodeEnum blockType = RemoteCodeEnum.getByException(blockException);
            code = blockType.getCode();
            message = blockType.getMessage();
            log.error("课表服务调用被 Sentinel 拦截：[{}] {}", code, message, cause);
        } else if (cause instanceof RetryableException || cause instanceof TimeoutException) {
            code = CourseRemoteCodeEnum.COURSE_SERVICE_TIMEOUT.getCode();
            message = CourseRemoteCodeEnum.COURSE_SERVICE_TIMEOUT.getMessage();
            log.error("课表服务调用超时：[{}] {}", code, message, cause);
        } else if (cause instanceof IOException) {
            code = CourseRemoteCodeEnum.COURSE_SERVICE_UNAVAILABLE.getCode();
            message = CourseRemoteCodeEnum.COURSE_SERVICE_UNAVAILABLE.getMessage();
            log.error("课表服务不可达：[{}] {}", code, message, cause);
        } else if (cause instanceof FeignException feignException) {
            int status = feignException.status();

            if (status >= 500) {
                code = CourseRemoteCodeEnum.COURSE_SERVER_ERROR.getCode();
                message = CourseRemoteCodeEnum.COURSE_SERVER_ERROR.getMessage();
                log.error("课表服务 5xx 异常：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
            } else if (status >= 400) {
                switch (status) {
                    case 400 -> {
                        code = CourseRemoteCodeEnum.COURSE_BAD_REQUEST.getCode();
                        message = CourseRemoteCodeEnum.COURSE_BAD_REQUEST.getMessage();
                        log.warn("课表服务 400 参数异常：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
                    }
                    case 401, 403 -> {
                        code = CourseRemoteCodeEnum.COURSE_AUTH_FAILED.getCode();
                        message = CourseRemoteCodeEnum.COURSE_AUTH_FAILED.getMessage();
                        log.error("课表服务鉴权失败：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
                    }
                    case 404 -> {
                        code = CourseRemoteCodeEnum.COURSE_API_NOT_FOUND.getCode();
                        message = CourseRemoteCodeEnum.COURSE_API_NOT_FOUND.getMessage();
                        log.error("课表服务接口不存在：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
                    }
                    default -> {
                        code = CourseRemoteCodeEnum.COURSE_RPC_ERROR.getCode();
                        message = CourseRemoteCodeEnum.COURSE_RPC_ERROR.getMessage();
                        log.error("课表服务其他 4xx 异常：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
                    }
                }
            } else {
                code = CourseRemoteCodeEnum.COURSE_RPC_ERROR.getCode();
                message = CourseRemoteCodeEnum.COURSE_RPC_ERROR.getMessage();
                log.error("课表服务未知 Feign 异常：[{}] status={}, message={}", code, status, cause.getMessage(), cause);
            }
        } else {
            code = CourseRemoteCodeEnum.COURSE_RPC_ERROR.getCode();
            message = CourseRemoteCodeEnum.COURSE_RPC_ERROR.getMessage();
            log.error("课表服务未知异常：[{}] {}", code, message, cause);
        }

        return new CourseServiceClient() {
            @Override
            public Result<RemoteCourseVO> getSchedule(String studentId) {
                return Result.error(code, message);
            }

            @Override
            public Result<String> syncScheduleData(RemoteCourseDTO remoteCourseDTO) {
                return Result.error(code, message);
            }
        };
    }
}