package com.campusassistant.admin.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminLogFileListVO {

    private List<AdminLogFileItemVO> files;
}