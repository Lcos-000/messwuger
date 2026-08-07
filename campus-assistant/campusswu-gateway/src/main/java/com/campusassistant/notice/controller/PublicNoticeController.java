package com.campusassistant.notice.controller;

import com.campusassistant.notice.pojo.vo.NoticeResponseVO;
import com.campusassistant.notice.service.NoticeService;
import com.campusassistant.pojo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway/public")
@RequiredArgsConstructor
public class PublicNoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notice")
    public Result<NoticeResponseVO> getCurrentNotice() {
        return Result.success(noticeService.getCurrentNotice());
    }



}
