package com.campusassistant.student.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class EmptyClassroomTaskSubmitVO {

    // 空教室查询状态 RESULT_READY / QUERYING / TIMEOUT / SUBMITTED / FAILED
    private String queryStatus;

    // 是否已命中结果缓存
    private Boolean resultReady;

    // 本次是否真正发起了新任务
    private Boolean taskSubmitted;

}