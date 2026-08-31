package com.yizqq.yizcode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yizqq.yizcode.exception.BusinessException;
import com.yizqq.yizcode.exception.ErrorCode;
import com.yizqq.yizcode.exception.ThrowUtils;
import com.yizqq.yizcode.mapper.AppMapper;
import com.yizqq.yizcode.model.dto.app.AppQueryRequest;
import com.yizqq.yizcode.model.entity.App;
import com.yizqq.yizcode.model.entity.User;
import com.yizqq.yizcode.model.vo.AppVO;
import com.yizqq.yizcode.model.vo.UserVO;
import com.yizqq.yizcode.service.AppService;
import com.yizqq.yizcode.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用服务层实现。
 *
 * @author <a href="https://github.com/yizqq0721">yizqq</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {


    @Resource
    private UserService userService;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public long addApp(App app) {
        // 初始化提示词是创建应用的必要条件。
        ThrowUtils.throwIf(app == null || StrUtil.isBlank(app.getInitPrompt()), ErrorCode.PARAMS_ERROR);
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        // 仅允许已知字段参与排序，避免由排序字段引入非法 SQL。
        String sortField = appQueryRequest.getSortField();
        if (!"id".equals(sortField) && !"appName".equals(sortField) && !"priority".equals(sortField)
                && !"userId".equals(sortField)) {
            sortField = "createTime";
        }
        return QueryWrapper.create()
                .eq("id", appQueryRequest.getId())
                .like("appName", appQueryRequest.getAppName())
                .like("cover", appQueryRequest.getCover())
                .like("initPrompt", appQueryRequest.getInitPrompt())
                .eq("codeGenType", appQueryRequest.getCodeGenType())
                .eq("deployKey", appQueryRequest.getDeployKey())
                .eq("priority", appQueryRequest.getPriority())
                .eq("userId", appQueryRequest.getUserId())
                .orderBy(sortField, "ascend".equals(appQueryRequest.getSortOrder()));
    }
}
