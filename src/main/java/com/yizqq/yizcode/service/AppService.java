package com.yizqq.yizcode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yizqq.yizcode.model.dto.app.AppQueryRequest;
import com.yizqq.yizcode.model.entity.App;
import com.yizqq.yizcode.model.entity.User;
import com.yizqq.yizcode.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用服务层。
 *
 * @author <a href="https://github.com/yizqq0721">yizqq</a>
 */
public interface AppService extends IService<App> {

    /**
     * 新增应用，并校验初始化提示词。
     *
     * @param app 应用实体
     * @return 新建应用 id
     */
    long addApp(App app);

    /**
     * 获取应用封装类
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用封装类列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 根据查询条件构造应用查询参数。
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
    /**
     * 通过对话生成应用代码
     *
     * @param appId     应用 ID
     * @param prompt   提示词
     * @param loginUser 登录用户
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String prompt, User loginUser);

    /**
     * 应用部署
     *
     * @param appId     应用 ID
     * @param loginUser 登录用户
     * @return 可访问的部署地址
     */
    String deployApp(Long appId, User loginUser);
}
