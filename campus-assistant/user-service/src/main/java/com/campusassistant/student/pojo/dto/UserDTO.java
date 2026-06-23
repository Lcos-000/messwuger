package com.campusassistant.student.pojo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
// 默认 @JsonIgnoreProperties(ignoreUnknown = true) 忽略未知字段，显式写为 false，或者留空，如果有不认识的字段，就抛异常
@JsonIgnoreProperties()
public class UserDTO {
    @NotBlank(message = "学号不能为空")
    private String studentId;

    // 前端传入的明文教务密码
    @NotBlank(message = "密码不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
