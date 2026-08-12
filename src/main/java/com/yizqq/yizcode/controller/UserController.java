package com.yizqq.yizcode.controller;

import com.mybatisflex.core.paginate.Page;
import com.yizqq.yizcode.common.BaseResponse;
import com.yizqq.yizcode.common.ResultUtils;
import com.yizqq.yizcode.exception.ErrorCode;
import com.yizqq.yizcode.exception.ThrowUtils;
import com.yizqq.yizcode.model.dto.user.UserLoginRequest;
import com.yizqq.yizcode.model.dto.user.UserRegisterRequest;
import com.yizqq.yizcode.model.vo.LoginUserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yizqq.yizcode.model.entity.User;
import com.yizqq.yizcode.service.UserService;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author <a href="https://github.com/yizqq0721">yizqq</a>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 注册参数
     * @return 注册结果
     */
    @PostMapping("register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long result = userService.userRegister
                        (userRegisterRequest.getUserAccount(),
                         userRegisterRequest.getUserPassword(),
                         userRegisterRequest.getCheckPassword());
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     * @param userLoginRequest 用户登录请求
     * @param request HTTP请求
     * @return 登录结果
     */
    @PostMapping("login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest , HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO result = userService.userLogin
                            (userLoginRequest.getUserAccount(),
                             userLoginRequest.getUserPassword(),
                             request);
        return ResultUtils.success(result);
    }

    /**
     * 保存用户。
     *
     * @param user 用户
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody User user) {
        return userService.save(user);
    }

    /**
     * 根据主键删除用户。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return userService.removeById(id);
    }

    /**
     * 根据主键更新用户。
     *
     * @param user 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody User user) {
        return userService.updateById(user);
    }

    /**
     * 查询所有用户。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<User> list() {
        return userService.list();
    }

    /**
     * 根据主键获取用户。
     *
     * @param id 用户主键
     * @return 用户详情
     */
    @GetMapping("getInfo/{id}")
    public User getInfo(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 分页查询用户。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<User> page(Page<User> page) {
        return userService.page(page);
    }

}
