package com.campusassistant.remote.exception.block;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.campusassistant.pojo.Result;
import com.campusassistant.enums.RemoteCodeEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SentinelBlockHandlers {

    public static Result<?> defaultBlockHandler(BlockException ex) {
        RemoteCodeEnum blockType = RemoteCodeEnum.getByException(ex);

        Result<?> result = Result.error(blockType.getCode(), blockType.getMessage());

        log.error("远程调用失败：[{}] {}",blockType.getCode(),blockType.getMessage(),ex);

        return result;
    }

}
