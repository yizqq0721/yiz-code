package com.yizqq.yizcode.service;

import com.mybatisflex.core.service.IService;
import com.yizqq.yizcode.model.entity.User;
import com.yizqq.yizcode.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

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
     * 获取当前登录用户
     * @return 当前登录用户
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request HttpServletRequest
     * @return 登录结果
     */
    LoginUserVO userLogin(String userAccount, String userPassword , HttpServletRequest request);

}
