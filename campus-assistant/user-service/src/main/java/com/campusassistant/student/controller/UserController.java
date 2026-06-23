package com.campusassistant.student.controller;

import com.campusassistant.common.UserContext;
import com.campusassistant.pojo.Result;
import com.campusassistant.student.pojo.UserVO;
import com.campusassistant.student.service.CurrentUserService;
import com.campusassistant.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController //@RestController = @Controller + @ResponseBody (代表此类所有接口均返回 JSON 格式数据)
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {


    private final CurrentUserService currentUserService;

    @GetMapping("/search")
    public Result<UserVO> searchById() {
        UserContext userContext = ThreadLocalUtil.get();
        UserVO data = currentUserService.getPersonalByStudentId(userContext.getStudentId());
        return Result.success(data);
    }

    @DeleteMapping("/delete")
    public Result<Void> unsubscribe() {
        currentUserService.self_unsubscribe();
        return Result.success();
    }

    

}
