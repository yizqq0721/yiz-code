package com.yizqq.yizcode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yizqq.yizcode.exception.BusinessException;
import com.yizqq.yizcode.exception.ErrorCode;
import com.yizqq.yizcode.model.entity.User;
import com.yizqq.yizcode.mapper.UserMapper;
import com.yizqq.yizcode.model.enums.UserRoleEnum;
import com.yizqq.yizcode.model.vo.LoginUserVO;
import com.yizqq.yizcode.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.yizqq.yizcode.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/yizqq0721">yizqq</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        //检验参数
        if (userAccount == null || userPassword == null || checkPassword == null) {
            throw new IllegalArgumentException("参数为空");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new IllegalArgumentException("两次输入密码不一致");
        }
        if (userAccount.length() < 4 || userAccount.length() > 16) {
            throw new IllegalArgumentException("账号长度在4到16个字符之间");
        }
        if (userPassword.length() < 8 || userPassword.length() > 20) {
            throw new IllegalArgumentException("密码长度在8到20个字符之间");
        }

        //检查用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        //加密密码
        String encryptedPassword = getEncryptedPassword(userPassword);

        //创建用户，插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setUserName("noName");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败");
        }
        return user.getId();


    }


    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null){ return null;}
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword , HttpServletRequest request) {

        //检验参数
        if (userAccount == null || userPassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度在4到16个字符之间");
        }
        if (userPassword.length() < 8 || userPassword.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度在8到20个字符之间");
        }

        //加密
        String encryptedPassword = getEncryptedPassword(userPassword);

        //查看用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount)
                .eq("userPassword", encryptedPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        //用户存在，记录登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        return this.getLoginUserVO(user);

    }

    @Override
    /*
     * 获取加密密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    public String getEncryptedPassword(String userPassword) {

        final String SALT = "yizqq";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
}
