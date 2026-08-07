package com.campusassistant.notice.pojo.vo;

import lombok.Data;

@Data
public class NoticeResponseVO {

    private Boolean enabled;
    private Integer version;
    private String title;
    private String content;
    private String level;
    private String updatedAt;

}
