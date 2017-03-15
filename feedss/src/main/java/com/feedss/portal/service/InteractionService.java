package com.feedss.portal.service;

import java.util.Date;
import java.util.List;

import com.feedss.base.ErrorCode;
import com.feedss.content.entity.Stream;
import com.feedss.portal.entity.Interaction;
import com.feedss.user.model.UserVo;

public interface InteractionService {
    
    public Interaction publish(UserVo creator, UserVo receiver, String type, int price, Stream stream, String description);
    
    public ErrorCode confirmFinish(String userId, String interactionId);
    public ErrorCode reject(String userId, String interactionId);

    public ErrorCode del(String userId, String interactionId);
    
    public List<Interaction> select(String userId, String type, String streamId, boolean isCreator, String cursorId, int pageSize, int direction);
    
    public List<String> getRelatedStream(String userId, String type, boolean isCreator, String cursorId, int pageSize, int direction);
    
    public int getCount(String userId, String type, boolean isCreator);
    
    public List<Interaction> selectOverdue(Date time);
    
    public void handleOverdue();
    
    public int getTotalIncome(String streamId);
    
    public int getNumber(String streamId, Interaction.InteractionType type);
}
