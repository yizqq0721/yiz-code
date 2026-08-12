package com.yizqq.yizcode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yizqq.yizcode.model.dto.user.UserQueryRequest;
import com.yizqq.yizcode.model.entity.User;
import com.yizqq.yizcode.model.vo.LoginUserVO;
import com.yizqq.yizcode.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/yizqq0721">yizqq</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取加密密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptedPassword(String userPassword);


    /**
     * 用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request HttpServletRequest
     * @return 登录结果
     */
    LoginUserVO userLogin(String userAccount, String userPassword , HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @return 当前登录用户
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取当前登录用户信息
     * @return 当前登录用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取用户脱敏信息
     * @param user 用户
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户脱敏信息列表
     * @param userList 用户列表
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 根据查询条件构造数据查询参数
     * @param userQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);


}
