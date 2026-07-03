package com.campusassistant.admin.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminLogTailVO {

    private String fileName;

    private List<String> lines;

    private Long startOffset;

    private Long offset;

    private Long fileSize;

    private Boolean reset;

    private Boolean hasMoreOldLines;

    private Boolean hasMoreNewLines;
}