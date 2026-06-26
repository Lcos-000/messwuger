package com.campusassistant.personalization.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "上传自定义图片返回结果")
@Data
public class UploadCustomAssetVO {

    @Schema(description = "资源类型")
    private String type;

    @Schema(description = "上传后的访问路径")
    private String url;
}