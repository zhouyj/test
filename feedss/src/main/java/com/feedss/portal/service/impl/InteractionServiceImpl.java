package com.feedss.portal.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feedss.base.ErrorCode;
import com.feedss.base.util.CommonUtil;
import com.feedss.base.util.DateUtil;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.contact.entity.Message;
import com.feedss.contact.service.MessageService;
import com.feedss.content.entity.Stream;
import com.feedss.content.entity.Stream.StreamStatus;
import com.feedss.content.service.StreamService;
import com.feedss.portal.entity.Interaction;
import com.feedss.portal.entity.Interaction.InteractionStatus;
import com.feedss.portal.entity.Interaction.InteractionType;
import com.feedss.portal.repository.InteractionRepository;
import com.feedss.portal.service.InteractionService;
import com.feedss.user.entity.AccountTransaction.AccoutTransactionSourceType;
import com.feedss.user.model.UserVo;
import com.feedss.user.repository.AccountTransactionRepository;
import com.feedss.user.service.AccountService;
import com.feedss.user.service.UserService;

@Service
public class InteractionServiceImpl implements InteractionService {

    private static final Logger logger = LoggerFactory.getLogger(InteractionService.class);
	
	@Resource
	private InteractionRepository interactionRepository;
	@Resource
	private AccountService accountService;
	@Resource
	private AccountTransactionRepository accountTransactionRepository;
	@Resource
	private UserService userService;
	@Resource
	private StreamService streamService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	MessageService messageService;
	@Autowired
	ConfigureUtil configureUtil;

	@Override
	public Interaction publish(UserVo creator, UserVo receiver, String type, int price, Stream stream,
			String description) {
		Interaction interaction = new Interaction();
		interaction.setCreated(new Date());
		interaction.setCreator(creator.getUuid());
		interaction.setDescription(description);
		interaction.setPrice(price);
		interaction.setReceiver(receiver.getUuid());
		interaction.setType(type);
		interaction.setStatus(InteractionStatus.PUBLISHED.ordinal());
		interaction.setStreamId(stream.getUuid());

		interaction = interactionRepository.save(interaction);
		if (interaction != null) {
			boolean isSuccess = accountService.transfer(creator, receiver, AccoutTransactionSourceType.interactionNew,
					interaction.getUuid(), InteractionType.valueOf(type).getMsg(), interaction.getDescription(), price);
			if (!isSuccess) {
				interactionRepository.delete(interaction);
				return null;
			}
			if (stream.getStatus() == StreamStatus.Publishing.ordinal()) {
//				messageApi.sendGroupMsgInteration(creator, CommonUtil.getGroupId(stream.getUuid()), type, interaction);
				Map<String, Object> ext = new HashMap<String, Object>();
				Map<String, Object> userInfo = new HashMap<String, Object>();
				userInfo.put("uuid", creator.getUuid());
				userInfo.put("nickname", creator.getProfile().getNickname());
				userInfo.put("avatar", creator.getProfile().getAvatar());
				ext.put("messageSource", Message.Source.Barrage.name());
				ext.put("type", type);
				ext.put("interaction", interaction);
				ext.put("text", "发布了一个" + (type.equalsIgnoreCase(InteractionType.Bid.name()) ? "投标" : "任务") + ": "
						+ interaction.getDescription());
				ext.put("userInfo", userInfo);
				messageService.sendGroupMessage(CommonUtil.getGroupId(stream.getUuid()), "", ext, creator.getUuid());
			} else {
				sendSystemMsgInteration(receiver.getUuid(), creator, interaction, interaction.getStatus());
			}
		}
		return interaction;
	}

	@Override
	public ErrorCode confirmFinish(String userId, String interactionId) {
		if (StringUtils.isEmpty(interactionId) || StringUtils.isEmpty(userId)) {
			return ErrorCode.INVALIDPARAMETERS;
		}
		Interaction i = interactionRepository.findOne(interactionId);
		if (i == null)
			return ErrorCode.INVALIDPARAMETERS;
		String receiver = i.getReceiver();
		if (StringUtils.isEmpty(receiver))
			return ErrorCode.INTERNALERROR;
		if (!receiver.equals(userId))
			return ErrorCode.NO_AUTH;
		i.setStatus(InteractionStatus.CONFIRMED.ordinal());
		try {
			interactionRepository.save(i);
		} catch (Exception e) {
			return ErrorCode.INTERNALERROR;
		}

		sendSystemMsgInteration(i.getCreator(), userService.getUserVoByUserId(userId), i,
				InteractionStatus.CONFIRMED.ordinal());
		return ErrorCode.SUCCESS;
	}

	@Override
	public ErrorCode reject(String userId, String interactionId) {
		if (StringUtils.isEmpty(interactionId) || StringUtils.isEmpty(userId)) {
			return ErrorCode.INVALIDPARAMETERS;
		}
		Interaction i = interactionRepository.findOne(interactionId);
		if (i == null)
			return ErrorCode.INVALIDPARAMETERS;
		Object o=CommonUtil.getKey(userId+" reject "+i.getUuid());
		synchronized (o) {
			String receiver = i.getReceiver();
			if (StringUtils.isEmpty(receiver))
				return ErrorCode.INTERNALERROR;
			if (!receiver.equals(userId))
				return ErrorCode.NO_AUTH;
			if(i.getStatus()!=InteractionStatus.PUBLISHED.ordinal()){
				return ErrorCode.HAVE_HANDLED;
			}
			i.setStatus(InteractionStatus.REJECTED.ordinal());
			try {
				i = interactionRepository.save(i);
				if (i != null) {
					boolean isSuccess = accountService.transfer(userService.getUserVoByUserId(i.getReceiver()),
							userService.getUserVoByUserId(i.getCreator()), AccoutTransactionSourceType.interactionReject,
							i.getUuid(), InteractionType.valueOf(i.getType()).getMsg(), i.getDescription(), i.getPrice());

				}
			} catch (Exception e) {
				return ErrorCode.INTERNALERROR;
			}
			// TODO 发系统消息给creator
			sendSystemMsgInteration(i.getCreator(), userService.getUserVoByUserId(userId), i,
					InteractionStatus.REJECTED.ordinal());
		}
		
		return ErrorCode.SUCCESS;
	}

	@Override
	public ErrorCode del(String userId, String interactionId) {
		if (StringUtils.isEmpty(interactionId) || StringUtils.isEmpty(userId)) {
			return ErrorCode.INVALIDPARAMETERS;
		}
		Interaction i = interactionRepository.findOne(interactionId);
		if (i == null)
			return ErrorCode.INVALIDPARAMETERS;
		if (StringUtils.isEmpty(i.getReceiver()) || StringUtils.isEmpty(i.getCreator()))
			return ErrorCode.INTERNALERROR;
		boolean needSend = false;
		if (i.getReceiver().equals(userId)) {
			if (i.getStatus() == InteractionStatus.PUBLISHED.ordinal()) {
				i.setStatus(InteractionStatus.REJECTED.ordinal());
				needSend = true;
			}
			i.setReceiverDel(true);
		} else if (i.getCreator().equals(userId)) {
			if (i.getStatus() == InteractionStatus.CONFIRMED.ordinal()
					|| i.getStatus() == InteractionStatus.REJECTED.ordinal()) {
				i.setCreatorDel(true);
			} else {// 主播尚未处理，不可删除
				return ErrorCode.NO_AUTH;
			}
		} else {
			return ErrorCode.NO_AUTH;
		}

		try {
			interactionRepository.save(i);
			if (i != null) {
				accountService.transfer(userService.getUserVoByUserId(i.getReceiver()),
						userService.getUserVoByUserId(i.getCreator()), AccoutTransactionSourceType.interactionReject,
						i.getUuid(), InteractionType.valueOf(i.getType()).getMsg(), i.getDescription(), i.getPrice());

			}
		} catch (Exception e) {
			return ErrorCode.INTERNALERROR;
		}
		// 发系统消息给creator
		if (needSend) {
			sendSystemMsgInteration(i.getCreator(), userService.getUserVoByUserId(userId), i,
					InteractionStatus.REJECTED.ordinal());
		}

		return ErrorCode.SUCCESS;
	}

	@Override
	public List<Interaction> select(String userId, String type, String streamId, boolean isCreator, String cursorId,
			int pageSize, int direction) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		StringBuilder sqlSB = new StringBuilder(
				"select i from Interaction i where i.type= :type and i.streamId = :streamId");
		paramMap.put("type", type);
		paramMap.put("streamId", streamId);
		if (isCreator) {
			sqlSB.append(" and i.creator=:userId and i.isCreatorDel=0");
		} else {
			sqlSB.append(" and i.receiver=:userId and i.isReceiverDel=0");
		}
		paramMap.put("userId", userId);

		if (StringUtils.isNotBlank(cursorId)) {
			Interaction cursor = interactionRepository.findOne(cursorId);
			if (cursor != null) {
				if (direction == 1) {// 上一页
					sqlSB.append(" and (i.price >:price or (i.price = :price and i.created < :created))");
				} else {// 下一页
					sqlSB.append(" and (i.price <:price or (i.price = :price and i.created > :created))");
				}
				paramMap.put("created", cursor.getCreated());
				paramMap.put("price", cursor.getPrice());
			}
		}
		sqlSB.append(" order by i.price desc, i.created");

		TypedQuery<Interaction> result = entityManager.createQuery(sqlSB.toString(), Interaction.class)
				.setFirstResult(0).setMaxResults(pageSize);
		;
		paramMap.forEach((k, v) -> {
			result.setParameter(k, v);
		});
		List<Interaction> list = result.getResultList();
		if (list == null || list.isEmpty()) {
			return null;
		}
		for (Interaction i : list) {
			i.setCreateUserInfo(userService.getUserVoByUserId(i.getCreator()));
			i.setReceiveUserInfo(userService.getUserVoByUserId(i.getReceiver()));
		}

		return list;
	}

	@Override
	public List<String> getRelatedStream(String userId, String type, boolean isCreator, String cursorId,
			int pageSize, int direction) {
		List<String> streamIdList = null;
		if (StringUtils.isNotBlank(cursorId)) {
			Interaction cursor = null;
			List<Interaction> list = null;
			if (isCreator) {
				list = interactionRepository.selectLatestByCreatorAndStream(userId, type, cursorId);
			} else {
				list = interactionRepository.selectLatestByReceiverAndStream(userId, type, cursorId);
			}
			if (list != null && !list.isEmpty()) {
				cursor = list.get(0);
			}
			if (cursor != null) {

				StringBuilder nativeSql = new StringBuilder();
				nativeSql.append("select tmp.stream_id from (");
				nativeSql.append(
						"select max(created) as created , stream_id from interaction where type='" + type + "'");
				if (isCreator) {
					nativeSql.append(" and creator='" + userId + "' and is_creator_del=0");
				} else {
					nativeSql.append(" and receiver='" + userId + "' and is_receiver_del=0");
				}

				nativeSql.append(" group by stream_id order by max(created) desc");
				nativeSql.append(") as tmp");
				if (direction == 1) {// 上一页
					nativeSql.append(" where tmp.created >'" + cursor.getCreated() + "'");
				} else {// 下一页
					nativeSql.append(" where tmp.created <'" + cursor.getCreated() + "'");
				}
				Query result = entityManager.createNativeQuery(nativeSql.toString()).setFirstResult(0)
						.setMaxResults(pageSize);
				;
				streamIdList = result.getResultList();
			}

		} else {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			StringBuilder sqlSB = new StringBuilder("select i.streamId from Interaction i where i.type= :type");
			paramMap.put("type", type);
			if (isCreator) {
				sqlSB.append(" and i.creator=:userId and i.isCreatorDel=0");
			} else {
				sqlSB.append(" and i.receiver=:userId and i.isReceiverDel=0");
			}
			paramMap.put("userId", userId);

			sqlSB.append(" group by streamId order by max(created) desc");
			TypedQuery<String> result = entityManager.createQuery(sqlSB.toString(), String.class).setFirstResult(0)
					.setMaxResults(pageSize);
			;
			paramMap.forEach((k, v) -> {
				result.setParameter(k, v);
			});
			streamIdList = result.getResultList();
		}
		return streamIdList;
	}

	@Override
	public int getCount(String userId, String type, boolean isCreator) {
		if (isCreator) {
			return interactionRepository.selectCountByCreator(userId, type);
		} else {
			return interactionRepository.selectCountByReceiver(userId, type);
		}
	}

	@Override
	public List<Interaction> selectOverdue(Date time) {
		return interactionRepository.selectOverdue(time, InteractionStatus.PUBLISHED.ordinal());
	}

	@Override
	public void handleOverdue() {
		int expiredDay = 7;
		try {
			String days = configureUtil.getConfig("interactionOverdue");
			expiredDay = Integer.parseInt(days);
		} catch (Exception e) {
		}
		try {
			List<Interaction> list = interactionRepository.selectOverdue(DateUtil.getDateBefore(new Date(), expiredDay),
					InteractionStatus.PUBLISHED.ordinal());
			if (list == null || list.isEmpty())
				return;
			// TODO 处理
			for(Interaction i:list){
				logger.info("handleOverdue, i = " + JSONObject.toJSONString(i));
				reject(i.getReceiver(), i.getUuid());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getTotalIncome(String streamId) {
		if(interactionRepository.getNum(streamId)>0){
			try{
				return interactionRepository.getTotalIncome(streamId);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		return 0;
	}

	@Override
	public int getNumber(String streamId, InteractionType type) {
		return interactionRepository.getNum(streamId, type.name());
	}
	
	private void sendSystemMsgInteration(String toAccount, UserVo sender, Interaction i, int status) {
		if (StringUtils.isEmpty(toAccount)) {
			return;
		}
		Map<String, Object> ext = new HashMap<String, Object>();
		ext.put("messageSource", i.getType());
		ext.put("userId", sender.getUuid());
		ext.put("interation", i);

		String title = "";
		if (status == InteractionStatus.CONFIRMED.ordinal()) {
			title = sender.getProfile().getNickname() + "确认了您的"
							+ (i.getType().equalsIgnoreCase(InteractionType.Bid.name()) ? "投标" : "任务") + ": "
							+ i.getDescription();
		} else if (status == InteractionStatus.REJECTED.ordinal()) {
			title = sender.getProfile().getNickname() + "拒绝了您的"
							+ (i.getType().equalsIgnoreCase(InteractionType.Bid.name()) ? "投标" : "任务") + ": "
							+ i.getDescription();
		} else if (status == InteractionStatus.PUBLISHED.ordinal()) {
			title = sender.getProfile().getNickname() + "向您发布了一个"
							+ (i.getType().equalsIgnoreCase(InteractionType.Bid.name()) ? "投标" : "任务") + ": "
							+ i.getDescription();
		}
		messageService.sendSystemMessage(title, ext, toAccount);
		
	}
}
