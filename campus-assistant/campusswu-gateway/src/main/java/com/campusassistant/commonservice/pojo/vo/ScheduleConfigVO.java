package com.campusassistant.commonservice.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConfigVO {
    private String springStartDate;
    private String autumnStartDate;
    private Integer maxWeek;
}
