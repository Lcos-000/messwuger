package com.campusassistant.student.controller;

import com.campusassistant.pojo.Result;
import com.campusassistant.remote.course.pojo.RemoteGradeVO;
import com.campusassistant.student.pojo.dto.EmptyClassroomQueryDTO;
import com.campusassistant.student.pojo.dto.GradesQueryDTO;
import com.campusassistant.student.pojo.vo.EmptyClassroomQueryResultVO;
import com.campusassistant.student.pojo.vo.UserStatusVO;
import com.campusassistant.remote.spider.sync.pojo.vo.PersonalInfoVO;
import com.campusassistant.student.pojo.dto.AutoPunchSwitchDTO;
import com.campusassistant.student.service.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {

    private final CurrentUserService currentUserService;

    @Operation(summary = "获取用户状态")
    @GetMapping("/status")
    public Result<UserStatusVO> getStatus() {
        UserStatusVO statusVO = currentUserService.getStatusByStudentId();
        return Result.success(statusVO);
    }

    @Operation(summary = "获取用户个人信息")
    @GetMapping("/personal")
    public Result<PersonalInfoVO> getPersonal() {
        PersonalInfoVO personalVO = currentUserService.getPersonalByStudentId();
        return Result.success(personalVO);
    }

    @Operation(summary = "用户取消订阅")
    @DeleteMapping("/delete")
    public Result<Void> unsubscribe(HttpServletRequest request) {
        currentUserService.self_unsubscribe(request);
        return Result.success();
    }

    @Operation(summary = "更新自动打卡开关")
    @PutMapping("/auto-punch")
    public Result<Void> updateAutoPunch(@Valid @RequestBody AutoPunchSwitchDTO dto) {
        currentUserService.updateAutoPunchEnabled(dto.getAutoPunchEnabled());
        return Result.success();
    }

    @Operation(summary = "提交成绩查询任务")
    @PostMapping("/grades/task")
    public Result<?> submitGradesTask(@Valid @RequestBody GradesQueryDTO dto) {
        return currentUserService.submitGradesTask(dto);

    }

    @Operation(summary = "查询成绩")
    @GetMapping("/grades")
    public Result<List<RemoteGradeVO>> getGrades(@RequestParam("academicYear") String academicYear,
                                                 @RequestParam("semester") String semester) {
        return Result.success(currentUserService.getGrades(academicYear, semester));
    }

    @Operation(summary = "提交空教室查询任务")
    @PostMapping("/empty-classroom/task")
    public Result<?> submitEmptyClassroomTask(@Valid @RequestBody EmptyClassroomQueryDTO dto) {
        return currentUserService.submitEmptyClassroomTask(dto);
    }

    @Operation(summary = "查询空教室结果")
    @PostMapping("/empty-classroom/result")
    public Result<EmptyClassroomQueryResultVO> getEmptyClassroomResult(@Valid @RequestBody EmptyClassroomQueryDTO dto) {
        return Result.success(currentUserService.getEmptyClassroomResult(dto));
    }

}
