package com.campusassistant.remote.exception.fallback;

import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.pojo.Result;
import com.campusassistant.enums.RemoteCodeEnum;
import com.campusassistant.remote.course.client.CourseServiceClient;
import com.campusassistant.remote.course.pojo.RemoteCourseDTO;
import com.campusassistant.remote.course.pojo.RemoteCourseVO;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import feign.FeignException;
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
        if (cause == null){
            log.error("未知错误，未捕获到异常信息");
        }
        else if (cause instanceof com.alibaba.csp.sentinel.slots.block.BlockException) {
            RemoteCodeEnum blockType = RemoteCodeEnum.getByException((BlockException) cause);
            log.error("远程调用失败：[{}] {}",blockType.getCode(),blockType.getMessage(),cause);
        }
        else if (cause instanceof IOException) {
            log.error("网络故障：无法连接到课程服务，message={}", cause.getMessage(), cause);
        }
        // FeignException 是所有 HTTP 错误的父类
        else if (cause instanceof FeignException) {
            int status = ((FeignException) cause).status();
            if (status >= 500) {
                log.error("服务端报错：[{}]课程服务内部错误:{}: ", status, cause.getMessage(), cause);
            } else if (status >= 400) {
                switch (status) {
                    case 400:
                        log.warn("参数校验失败: [{}] {}", status, cause.getMessage(), cause);
                        break;
                    case 401:
                        log.warn("认证失效: [{}] {}", status, cause.getMessage(), cause);
                        break;
                    case 403:
                        log.warn("权限不足: [{}] {}", status, cause.getMessage(), cause);
                        break;
                    case 404:
                        log.error("接口不存在: [{}] {}", status, cause.getMessage(), cause);
                        break;
                    default:
                        log.error("其他客户端错误: [{}] {}", status, cause.getMessage(), cause);
                        break;
                }
            }
        }
        else if (cause instanceof TimeoutException) {
            log.error("响应超时：{}",  cause.getMessage(), cause);
        }
        else {
            log.error("未知异常：{}", cause.getMessage(), cause);
        }

        return new CourseServiceClient() {
            @Override
            public Result<RemoteCourseVO> getSchedule(String studentId) {
                return Result.error(ResultCodeEnum.RPC_ERROR.getCode(), "查询课表失败，请稍后再试");
            }

            @Override
            public Result<String> syncScheduleData(RemoteCourseDTO remoteCourseDTO) {
                return Result.error(ResultCodeEnum.RPC_ERROR.getCode(), "更新课表失败，请稍后再试");
            }
        };
    }
}