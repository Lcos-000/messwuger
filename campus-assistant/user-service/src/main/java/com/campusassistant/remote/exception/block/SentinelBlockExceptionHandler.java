package com.campusassistant.remote.exception.block;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.campusassistant.pojo.Result;
import com.campusassistant.enums.RemoteCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
@RequiredArgsConstructor
public class SentinelBlockExceptionHandler implements BlockExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, String resourceName, BlockException e) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        RemoteCodeEnum blockType = RemoteCodeEnum.getByException(e);

        Result<?> result = Result.error(blockType.getCode(), blockType.getMessage());

        response.setStatus(blockType.getCode());
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter writer = response.getWriter();
        try {
            writer.write(objectMapper.writeValueAsString(result));
        } finally {
            writer.flush();
            writer.close();
        }
    }
}
