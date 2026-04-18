package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.DcChannelMessagesMapper;
import com.tencent.wxcloudrun.dto.DcChannelMessageRequest;
import com.tencent.wxcloudrun.model.DcChannelMessage;
import com.tencent.wxcloudrun.service.DcChannelMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class DcChannelMessageServiceImpl implements DcChannelMessageService {

    private final DcChannelMessagesMapper dcChannelMessagesMapper;

    @Autowired
    public DcChannelMessageServiceImpl(DcChannelMessagesMapper dcChannelMessagesMapper) {
        this.dcChannelMessagesMapper = dcChannelMessagesMapper;
    }

    @Override
    public void saveMessage(DcChannelMessageRequest request) {
        DcChannelMessage message = new DcChannelMessage();
        message.setChannelId(request.getChannelId());
        message.setChannelName(request.getChannelName());
        message.setTimestamp(Timestamp.valueOf(request.getTimestamp()));
        message.setUser(request.getUser());
        message.setContent(request.getContent());
        dcChannelMessagesMapper.insert(message);
    }
}
