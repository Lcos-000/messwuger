package com.campusassistant.remote.exception.block;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.campusassistant.pojo.Result;
import com.campusassistant.enums.RemoteCodeEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SentinelBlockHandlers {

    // 只为特定的、标注了 Sentinel 注解的方法服务，默认处理逻辑
    public static Result<?> defaultBlockHandler(BlockException ex) {
        RemoteCodeEnum blockType = RemoteCodeEnum.getByException(ex);

        Result<?> result = Result.error(blockType.getCode(), blockType.getMessage());

        log.error("远程调用失败：[{}] {}",blockType.getCode(),blockType.getMessage(),ex);

        return result;
    }

}
