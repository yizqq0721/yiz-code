package com.yizqq.yizcode.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yizqq.yizcode.model.entity.ChatHistory;
import com.yizqq.yizcode.mapper.ChatHistoryMapper;
import com.yizqq.yizcode.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/yizqq0721">yizqq</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
