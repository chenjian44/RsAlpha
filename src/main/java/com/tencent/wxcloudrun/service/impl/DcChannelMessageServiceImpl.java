package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.DcChannelMessagesMapper;
import com.tencent.wxcloudrun.dto.DcChannelMessageRequest;
import com.tencent.wxcloudrun.model.DcChannelMessage;
import com.tencent.wxcloudrun.service.DcChannelMessageService;
import com.tencent.wxcloudrun.utils.MD5Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class DcChannelMessageServiceImpl implements DcChannelMessageService {

    private static final Logger log = LoggerFactory.getLogger(DcChannelMessageServiceImpl.class);

    private final DcChannelMessagesMapper dcChannelMessagesMapper;

    @Autowired
    public DcChannelMessageServiceImpl(DcChannelMessagesMapper dcChannelMessagesMapper) {
        this.dcChannelMessagesMapper = dcChannelMessagesMapper;
    }

    @Override
    public void saveMessage(DcChannelMessageRequest request) {
        try {
            DcChannelMessage message = new DcChannelMessage();
            message.setChannelId(request.getChannelId());
            message.setChannelName(request.getChannelName());
            message.setTimestamp(Timestamp.valueOf(request.getTimestamp()));
            message.setUser(request.getUser());
            message.setContent(request.getContent());
            message.setContentMd5(MD5Utils.getMD5(request.getContent()));
            dcChannelMessagesMapper.insert(message);
        } catch (DuplicateKeyException e) {
            log.info("Duplicate message detected, skipping. channelId: {}, contentMd5: {}",
                    request.getChannelId(), MD5Utils.getMD5(request.getContent()));
        } catch (Exception e) {
            log.error("Unexpected error when saving message: {}", e.getMessage());
        }
    }

    @Override
    public List<DcChannelMessage> getMessagesByChannelId(String channelId) {
        return dcChannelMessagesMapper.selectByChannelIdOrderByTimestamp(channelId);
    }

    @Override
    public List<DcChannelMessage> getMessagesByChannelIdAndTimeRange(String channelId, Timestamp beginTime, Timestamp endTime) {
        return dcChannelMessagesMapper.selectByChannelIdAndTimeRange(channelId, beginTime, endTime);
    }

    @Override
    public List<String> getAllChannelIds() {
        return dcChannelMessagesMapper.selectDistinctChannelIds();
    }

    @Override
    public List<String> getAllChannelIdsByTimeRange(Timestamp beginTime, Timestamp endTime) {
        return dcChannelMessagesMapper.selectDistinctChannelIdsByTimeRange(beginTime, endTime);
    }
}
