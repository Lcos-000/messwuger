package com.campusassistant.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class GradeVO extends BaseGradeItem {

    private LocalDateTime syncTime;
}