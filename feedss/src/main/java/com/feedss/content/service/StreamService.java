package com.feedss.content.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.feedss.base.Constants;
import com.feedss.base.Pages;
import com.feedss.base.util.ConvertUtil;
import com.feedss.base.util.DateUtil;
import com.feedss.base.util.conf.ConfigureUtil;
import com.feedss.content.entity.Stream;
import com.feedss.content.entity.Stream.StreamStatus;
import com.feedss.content.entity.Stream.StreamType;
import com.feedss.content.repository.StreamRepository;
import com.feedss.user.service.UserService;

@Component
public class StreamService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StreamRepository streamRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private UserService userService;
	
	@Autowired
	private ConfigureUtil configureUtil;

	/**
	 * stream转化map
	 */
	public Map<String, Object> streamToMap(Stream stream) {
		Map<String, Object> reMap = new HashMap<String, Object>();
		if (stream == null) {
			return reMap;
		}
		reMap.put("uuid", stream.getUuid());
		reMap.put("position", ConvertUtil.objectToString(stream.getPosition(), false));
		reMap.put("status", stream.getStatus());
		reMap.put("ended",
				stream.getEnded() == null ? "" : DateUtil.dateToString(stream.getEnded(), DateUtil.FORMAT_STANDERD));
		reMap.put("type", ConvertUtil.objectToString(stream.getType(), false));
		reMap.put("appName", ConvertUtil.objectToString(stream.getAppName(), false));
		reMap.put("parentId",
				stream.getParentId() == null ? "" : ConvertUtil.objectToString(stream.getParentId(), false));
		reMap.put("cover", ConvertUtil.objectToString(stream.getCover(), false));
		reMap.put("category", ConvertUtil.objectToString(stream.getCategory(), false));
		reMap.put("playUri", ConvertUtil.objectToString(stream.getPlayUri(), false));
		reMap.put("publishUri", ConvertUtil.objectToString(stream.getPublishUri(), false));
		reMap.put("playbackUri", ConvertUtil.objectToString(stream.getPlaybackUri(), false));
		reMap.put("userId", ConvertUtil.objectToString(stream.getUserId(), false));
		reMap.put("longitude", ConvertUtil.objectToDouble(stream.getLongitude(), false));
		reMap.put("isVR", stream.getType().equals(StreamType.VR.toString()) ? 1 : 0);// 是否VR
																						// 0：否
																						// 1：是
		reMap.put("latitude", ConvertUtil.objectToDouble(stream.getLatitude(), false));
		reMap.put("started", DateUtil.dateToString(stream.getStarted(), DateUtil.FORMAT_STANDERD));
		reMap.put("isSecret", StringUtils.isEmpty(stream.getSecret()) ? 0 : 1);// 是否加密，0：否，1:是
		return reMap;
	}

	/**
	 * 创建直播流
	 * 
	 * @param userId
	 * @param title
	 * @param categoryId
	 * @param cover
	 * @param secret
	 * @return
	 */
	public Stream createStream(String userId, String title, String categoryId, String cover, String clearSecret,
			String secret, Double longitude, Double latitude, String position, StreamType type, String playbackUri,
			String bannerId, Long duration, String terminalPublishUrl) {
		String APPNAME = configureUtil.getConfig(Constants.APP_NAME);
		Stream stream = new Stream();
		stream.setUserId(userId);
		stream.setCategory(categoryId);
		stream.setName(title);
		stream.setCover(cover);
		if (null != secret) {
			stream.setSecret(secret);
			stream.setClearSecret(clearSecret);
		}
		stream.setLongitude(longitude);
		stream.setLatitude(latitude);
		stream.setPosition(position);
		stream.setStarted(new Date());
		stream.setAppName(APPNAME);
		stream.setBannerId(bannerId);

		// 非app的其他设备推流
		String path = "";
		if (StringUtils.isNotBlank(terminalPublishUrl)) {
			logger.debug("terminalPublishUrl = " + terminalPublishUrl);
			String prefix = configureUtil.getConfig(Constants.PUBLISH_DOMAIN) + "/" + APPNAME + "/";
			if (prefix.length() > terminalPublishUrl.length()) {
				logger.error("terminalPublishUrl = " + terminalPublishUrl + ", and prefix = " + prefix);
				path = terminalPublishUrl;
			} else {
				path = terminalPublishUrl.substring(prefix.length());
			}

		} else {
			path = stream.getUuid();
		}

		String publishUrl = configureUtil.getConfig(Constants.PUBLISH_DOMAIN) + "/" + APPNAME + "/" + path;
		String playUrl = configureUtil.getConfig(Constants.PLAY_DOMAIN) + "/" + APPNAME + "/" + path;
		String hlsUrl = configureUtil.getConfig(Constants.HLS_DOMAIN) + "/" + APPNAME + "/" + path + ".m3u8";

		if (StringUtils.isNotBlank(playbackUri)) {
			stream.setStatus(StreamStatus.Ended.ordinal());// 创建状态
			stream.setStreamStatus(StreamStatus.Ended.ordinal());
			stream.setStreamUpdateDate(new Date());
			stream.setPlaybackUri(playbackUri);
			stream.setHlsUri(playbackUri);

			DateTime dateTime = new DateTime(new Date());
			if (duration == null || duration <= 0) {

			}
			stream.setEnded(dateTime.plus(duration).toDate());
			stream.setDuration(duration);
		} else {
			stream.setStatus(StreamStatus.Created.ordinal());// 创建状态
			stream.setStreamStatus(StreamStatus.Created.ordinal());
			stream.setStreamUpdateDate(new Date());
			stream.setPublishUri(publishUrl);
			stream.setPlayUri(playUrl);
			stream.setHlsUri(hlsUrl);
		}

		stream.setType(type.name());
		return streamRepository.save(stream);
	}

	public Stream findStreamById(String streamId) {
		return streamRepository.findByUuid(streamId);
	}

	/**
	 * 开始推流
	 * 
	 * @param userId
	 * @param streamId
	 * @return
	 */
	public Stream publish(String userId, String streamId) {
		Stream stream = streamRepository.findOne(streamId);
		stream.setStatus(StreamStatus.Publishing.ordinal());
		streamRepository.save(stream);
		return stream;
	}

	/**
	 * 结束推流
	 * 
	 * @param userId
	 * @param streamId
	 * @return
	 */
	public Stream unpublish(String userId, String streamId) {
		Stream stream = streamRepository.findOne(streamId);
		stream.setStatus(StreamStatus.Ended.ordinal());
		streamRepository.save(stream);
		return stream;
	}

	public Pages<Stream> findStreams(int status, String userId, String categoryId, String name, int pageNo,
			int pageSize) {
		Sort sort = new Sort(Direction.DESC, "created");// 排序
		PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);// 分页查询
		Page<Stream> page = findStreams(status, userId, categoryId, name, pageRequest);
		int total = (int) page.getTotalElements();
		return new Pages<Stream>(total, page.getContent());
	}

	public Page<Stream> findStreams(final int status, final String userId, final String categoryId, final String name,
			Pageable pageable) {
		Specification<Stream> spec = new Specification<Stream>() {
			public Predicate toPredicate(Root<Stream> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				list.add(cb.isFalse(root.get("deleted")));
				list.add(cb.isNull(root.get("parentId")));
				if (status == 1 || status == 2) {
					list.add(cb.equal(root.get("status").as(Integer.class), status));
				}
				if (StringUtils.isNotBlank(userId)) {
					list.add(cb.equal(root.get("userId").as(String.class), userId));
				}
				if (StringUtils.isNotBlank(categoryId)) {
					list.add(cb.equal(root.get("categoryId").as(String.class), categoryId));
				}

				if (StringUtils.isNotBlank(name)) {
					list.add(cb.like(root.get("name").as(String.class), "%" + name + "%"));
				}
				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
		return streamRepository.findAll(spec, pageable);
	}

	public Pages<Stream> findAllStreams(int status, String userId, String name, int pageNo,
			int pageSize) {
		Sort sort = new Sort(Direction.DESC, "created");// 排序
		
		PageRequest pageRequest = new PageRequest(pageNo-1, pageSize, sort);// 分页查询,从0开始

		Specification<Stream> spec = new Specification<Stream>() {
			public Predicate toPredicate(Root<Stream> stream, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				list.add(cb.isFalse(stream.get("deleted")));
				list.add(cb.isNull(stream.get("parentId")));
				if (status == 1 || status == 2) {
					list.add(cb.equal(stream.get("status").as(Integer.class), status));
				}
				if (StringUtils.isNotBlank(userId)) {
					list.add(cb.equal(stream.get("userId").as(String.class), userId));
				}
				if (StringUtils.isNotBlank(name)) {
					list.add(cb.like(stream.get("name").as(String.class), "%" + name + "%"));
				}
				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
		Page<Stream> page = streamRepository.findAll(spec, pageRequest);
		int total = (int) page.getTotalElements();
		return new Pages<Stream>(total, page.getContent());
	}

	/**
	 * 查询数量<br>
	 * 
	 * @param categoryId
	 * @param status
	 * @return Long
	 */
	public Long selectStreamCount(String categoryId, int status) {
		Map<String, Object> paraMap = new HashMap<String, Object>();
		StringBuilder sql = new StringBuilder(
				"select count(s.uuid) from Stream s where s.deleted=false and s.parentId=null");
		if (categoryId != null) {
			sql.append(" and s.category=:category");
			paraMap.put("category", categoryId);
		}
		sql.append(" and s.status=:status");
		paraMap.put("status", status);
		if (status == 2) {// 回放路径不等于空
			sql.append(" and s.playbackUri != null");
		}
		TypedQuery<Long> result = entityManager.createQuery(sql.toString(), Long.class);
		paraMap.forEach((k, v) -> {
			result.setParameter(k, v);
		});
		Long count = result.getSingleResult().longValue();
		return count != null ? count : 0;
	}

	public int getTotalStreamTime(String userId) {
		if (streamRepository.hasStream(userId) > 0) {
			return streamRepository.selectStreamTime(userId);
		}
		return 0;
	}

	/**
	 * 根据分类和状态获取直播流
	 * 
	 * @param categoryId
	 * @param status
	 * @param cursorId
	 * @param pageSize
	 * @param direction
	 * @return
	 */
	public List<Stream> selectStreams(String categoryId, int status, String cursorId, Integer pageSize,
			Integer direction) {
		Map<String, Object> paraMap = new HashMap<String, Object>();
		StringBuilder sqlCount = new StringBuilder("select s from Stream s where s.deleted=false and s.parentId=null");

		if (StringUtils.isBlank(categoryId)) {// 最热查询
			Integer playCount = Integer.MAX_VALUE;
			Long streamId = Long.MAX_VALUE;
			if (StringUtils.isNotBlank(cursorId)) {
				Stream stream = streamRepository.findOne(cursorId);
				playCount = stream.getPlayCount();
				streamId = stream.getId();
			}
			if (status == 2) {// 回放的路径不能为空
				sqlCount.append(" and s.playbackUri != null");
			}
			if (direction.intValue() == 1) {// 升序
				sqlCount.append(
						" and s.status=:status and (s.playCount >:playCount or (s.playCount = :playCount and s.id > :streamId)) "
								+ "order by s.playCount,s.id");
			} else {// 降序
				sqlCount.append(
						" and s.status=:status and (s.playCount <:playCount or (s.playCount = :playCount and s.id < :streamId)) "
								+ "order by s.playCount desc,s.id desc");
			}
			paraMap.put("status", status);
			paraMap.put("playCount", playCount);
			paraMap.put("streamId", streamId);
		} else {// 分类查询
			Date created = new Date();
			Long streamId = Long.MAX_VALUE;
			if (StringUtils.isNotBlank(cursorId)) {
				Stream stream = streamRepository.findOne(cursorId);
				created = stream.getCreated();
				streamId = stream.getId();
			}
			if (status == 2) {// 回放的路径不能为空
				sqlCount.append(" and s.playbackUri != null");
			}
			if (direction.intValue() == 1) {// 升序
				sqlCount.append(
						" and s.category=:category and s.status=:status and (s.created > :created or (s.created = :created and s.id > :streamId))"
								+ " order by s.id");
			} else {// 降序
				sqlCount.append(
						" and s.category=:category and s.status=:status and (s.created < :created or (s.created = :created and s.id < :streamId)) "
								+ "order by s.id desc");
			}
			paraMap.put("category", categoryId);
			paraMap.put("status", status);
			paraMap.put("created", created);
			paraMap.put("streamId", streamId);
		}
		TypedQuery<Stream> result = entityManager.createQuery(sqlCount.toString(), Stream.class).setFirstResult(0)
				.setMaxResults(pageSize);
		;
		paraMap.forEach((k, v) -> {
			result.setParameter(k, v);
		});
		List<Stream> list = result.getResultList();
		return list == null ? new ArrayList<Stream>() : list;
	}

	/**
	 * 根据分类和状态获取直播流，按播放次数倒排
	 * 
	 * @param categoryId
	 * @param status
	 * @param cursorId
	 * @param pageSize
	 * @param direction
	 * @return
	 */
	public List<Stream> getStreamsByCategoryOrderByPlaycount(String categoryId, String cursorId, Integer pageSize,
			Integer direction) {
		Map<String, Object> paraMap = new HashMap<String, Object>();
		StringBuilder sqlSB = new StringBuilder(
				"select s from Stream s where s.deleted=false and s.parentId=null and s.status!=0");

		if (StringUtils.isBlank(categoryId)) {// 最热查询
			if (StringUtils.isNotBlank(cursorId)) {
				Stream stream = streamRepository.findOne(cursorId);
				if (stream != null) {
					Integer playCount = stream.getPlayCount();
					Long streamId = stream.getId();
					int status = stream.getStatus();
					if (direction.intValue() == 1) {// 上一页
						sqlSB.append(
								" and (s.playCount >:playCount or (s.playCount = :playCount and s.id > :streamId)) and s.status >= :status");
					} else {// 下一页
						sqlSB.append(
								" and (s.playCount <:playCount or (s.playCount = :playCount and s.id < :streamId)) and s.status >= :status");
					}
					paraMap.put("playCount", playCount);
					paraMap.put("streamId", streamId);
					paraMap.put("status", status);
				}
			}
			sqlSB.append(" order by s.status, s.playCount desc,s.id desc");
		} else {// 分类查询
			sqlSB.append(" and s.category=:category");
			paraMap.put("category", categoryId);

			if (StringUtils.isNotBlank(cursorId)) {
				Stream stream = streamRepository.findOne(cursorId);
				if (stream != null) {
					Date created = stream.getCreated();
					Long streamId = stream.getId();
					int status = stream.getStatus();
					if (direction.intValue() == 1) {// 上一页
						sqlSB.append(
								" and (s.created > :created or (s.created = :created and s.id > :streamId)) and s.status >= :status");
					} else {// 下一页
						sqlSB.append(
								" and (s.created < :created or (s.created = :created and s.id < :streamId)) and s.status >= :status");
					}
					paraMap.put("created", created);
					paraMap.put("streamId", streamId);
					paraMap.put("status", status);
				}
			}
			sqlSB.append(" order by s.status, s.created desc,s.id desc");
		}
		TypedQuery<Stream> result = entityManager.createQuery(sqlSB.toString(), Stream.class).setFirstResult(0)
				.setMaxResults(pageSize);
		;
		paraMap.forEach((k, v) -> {
			result.setParameter(k, v);
		});
		List<Stream> list = result.getResultList();
		return list == null ? new ArrayList<Stream>() : list;
	}

	/**
	 * 根据分类和状态获取直播流，按时间倒排
	 * 
	 * @param categoryId
	 * @param status
	 * @param cursorId
	 * @param pageSize
	 * @param direction
	 * @return
	 */
	public List<Stream> getStreamsByCategoryOrderByTime(String categoryId, String cursorId, Integer pageSize,
			Integer direction) {
		Map<String, Object> paraMap = new HashMap<String, Object>();
		StringBuilder sqlSB = new StringBuilder(
				"select s from Stream s where s.deleted=false and s.parentId=null and s.status!=0");

		if (StringUtils.isBlank(categoryId)) {// 最热查询
			if (StringUtils.isNotBlank(cursorId)) {
				Stream stream = streamRepository.findOne(cursorId);
				if (stream != null) {
					Long streamId = stream.getId();
					int status = stream.getStatus();
					if (direction.intValue() == 1) {// 上一页
						sqlSB.append(
								" and (s.created >:created or (s.created = :created and s.id > :streamId)) and s.status >= :status");
					} else {// 下一页
						sqlSB.append(
								" and (s.created <:created or (s.created = :created and s.id < :streamId)) and s.status >= :status");
					}
					paraMap.put("created", stream.getCreated());
					paraMap.put("streamId", streamId);
					paraMap.put("status", status);
				}
			}
			sqlSB.append(" order by s.sort desc, s.status, s.created desc, s.id desc");
		} else {// 分类查询
			sqlSB.append(" and s.category=:category");
			paraMap.put("category", categoryId);

			if (StringUtils.isNotBlank(cursorId)) {
				Stream stream = streamRepository.findOne(cursorId);
				if (stream != null) {
					Date created = stream.getCreated();
					Long streamId = stream.getId();
					int status = stream.getStatus();
					if (direction.intValue() == 1) {// 上一页
						sqlSB.append(
								" and (s.created > :created or (s.created = :created and s.id > :streamId)) and s.status >= :status");
					} else {// 下一页
						sqlSB.append(
								" and (s.created < :created or (s.created = :created and s.id < :streamId)) and s.status >= :status");
					}
					paraMap.put("created", created);
					paraMap.put("streamId", streamId);
					paraMap.put("status", status);
				}
			}
			sqlSB.append(" order by s.sort desc,  s.status, s.created desc,s.id desc");
		}
		TypedQuery<Stream> result = entityManager.createQuery(sqlSB.toString(), Stream.class).setFirstResult(0)
				.setMaxResults(pageSize);
		;
		paraMap.forEach((k, v) -> {
			result.setParameter(k, v);
		});
		List<Stream> list = result.getResultList();
		return list == null ? new ArrayList<Stream>() : list;
	}

	public List<Map<String, Object>> getStreams(List<Stream> streamList, boolean needSecret) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (streamList == null || streamList.isEmpty())
			return list;
		for (Stream stream : streamList) {
			if (stream == null)
				continue;
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("uuid", stream.getUuid());
			tempMap.put("title", ConvertUtil.objectToString(stream.getName(), false));// 标题
			tempMap.put("cover", ConvertUtil.objectToString(stream.getCover(), false));// 封面路径
			tempMap.put("duration",
					DateUtil.duration(stream.getStarted(), stream.getEnded() == null ? new Date() : stream.getEnded()));// 时长
			tempMap.put("started", DateUtil.dateToString(stream.getStarted(), DateUtil.FORMAT_MONTH_DAY_SIMPLE));// 开始时间
			String playbackUri = ConvertUtil.objectToString(stream.getPlaybackUri(), false);// 播放URL
			String playUri = stream.getPlayUri();
			int isOnline = 0;
			if (stream.getStatus() == 1) {// 判断是否直播
				isOnline = 1;
			}
			int isSecret = 0;// 是否加密 0：否 1：是
			if (stream.getSecret() != null && !stream.getSecret().trim().equals("")) {// 判断是个加密
				isSecret = 1;
				playbackUri = "";
				playUri = "";
			}
			tempMap.put("isOnline", isOnline);// 是否直播 0：否 1：是
			tempMap.put("playUri", playUri);// 播放URL
			tempMap.put("playbackUri", playbackUri);// 回放播放URL
			tempMap.put("isSecret", isSecret);// 是否加密 0：否 1：是
			if (needSecret) {
				tempMap.put("clearSecret", stream.getClearSecret());// 明文密码
			}
			tempMap.put("likeCount", stream.getPraiseCount());// 点赞数
			tempMap.put("playCount", stream.getPlayCount());// 播放数
			tempMap.put("bannerId", stream.getBannerId());// 活动ID
			tempMap.put("position", ConvertUtil.objectToString(stream.getPosition(), false));// 地理位置
			tempMap.put("isVR", stream.getType().equals(StreamType.VR.toString()) ? 1 : 0);// 是否VR
																							// 0：否
																							// 1：是
			tempMap.put("bannerId", stream.getBannerId());
			tempMap.put("user", userService.getUserVoByUserId(stream.getUserId()));// 放用户信息

			list.add(tempMap);
		}
		return list;
	}

	/**
	 * 获取用户直播流列表<br>
	 * 
	 * @param userId
	 * @param cursorId
	 * @param pageSize
	 * @return
	 */
	public Pages<Stream> findStreams(String userId, Integer pageNo, Integer pageSize) {
		// 总数量
		Integer count = streamRepository.findCountByUserId(userId);
		List<Stream> list = new ArrayList<Stream>();// strean列表
		// step:1
		if (count > 0) {
			Sort sort = new Sort(Direction.DESC, "started");// 排序
			PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);// 分页查询
			Page<Stream> page = streamRepository.findByUserId(userId, pageRequest);
			list = page.getContent();
		}
		return new Pages<Stream>(count, list);
	}

	/**
	 * 获取用户直播流数量
	 * 
	 * @param userId
	 * @return
	 */
	public int findUserStreamCount(String userId) {
		return streamRepository.findCountByUserId(userId);
	}

	/**
	 * 请求连线
	 * 
	 * @param userId
	 * @param parentId
	 * @return
	 */
	public Stream requestConnect(String userId, String parentId) {
		String APPNAME = configureUtil.getConfig(Constants.APP_NAME);
		Stream stream = new Stream();
		stream.setUserId(userId);
		stream.setParentId(parentId);
		stream.setStatus(StreamStatus.Created.ordinal());
		stream.setStreamStatus(StreamStatus.Created.ordinal());
		stream.setStreamUpdateDate(new Date());
		stream.setStarted(new Date());
		stream.setAppName(APPNAME);
		stream.setType(StreamType.Normal.toString());
		stream.setPublishUri(configureUtil.getConfig(Constants.PUBLISH_DOMAIN) + "/" + APPNAME + "/" + stream.getUuid());
		stream.setPlayUri(configureUtil.getConfig(Constants.PLAY_DOMAIN) + "/" + APPNAME + "/" + stream.getUuid());
		stream.setHlsUri(configureUtil.getConfig(Constants.HLS_DOMAIN) + "/" + APPNAME + "/" + stream.getUuid() + ".m3u8");

		Stream tmp = streamRepository.save(stream);
		// TODO 在save后返回的id＝0,临时处理如下
		return streamRepository.findOne(tmp.getUuid());
	}

	/**
	 * 获取请求连线流
	 * 
	 * @param userId
	 * @param parentId
	 * @return
	 */
	public Iterable<Stream> findRequests(String userId, String parentId) {
		return streamRepository.findByParentIdAndStatus(parentId, StreamStatus.Created.ordinal());
	}

	/**
	 * 更新stream状态<br>
	 * 
	 * @param userId
	 * @param childrenId
	 * @return
	 */
	@Transactional
	public Stream updateStatus(Integer status, String uuid) {
		if (status.intValue() == StreamStatus.Ended.ordinal()) {
			List<String> streams = new ArrayList<String>();
			streams.add(uuid);
			Integer count = streamRepository.closeStreams(status, new Date(), streams);
			if (count < 1) {
				logger.info("updateStatus->更新状态失败:" + uuid);
				return null;
			}
		} else {
			Integer count = streamRepository.updateStatusByUuid(status, uuid);
			if (count < 1) {
				logger.info("updateStatus->更新状态失败:" + uuid);
				return null;
			}
		}
		return streamRepository.findOne(uuid);
	}

	/**
	 * 根据主流Id获取所有连线流的Id列表
	 * 
	 * @param userId
	 * @param parentId
	 * @return
	 */
	public Iterable<Stream> findConnectings(String userId, String parentId) {
		return streamRepository.findByParentIdAndStatus(parentId, StreamStatus.Publishing.ordinal());
	}

	/**
	 * 查询stream信息<br>
	 * 
	 * @param uuid
	 * @return
	 */
	public Stream selectStream(String uuid) {
		return streamRepository.findByUuid(uuid);
	}

	public Map<String, Object> getStreamInfo(String streamId) {
		Stream stream = selectStream(streamId);
		Map<String, Object> tempMap = new HashMap<String, Object>();
		tempMap.put("uuid", stream.getUuid());
		tempMap.put("title", ConvertUtil.objectToString(stream.getName(), false));// 标题
		tempMap.put("cover", ConvertUtil.objectToString(stream.getCover(), false));// 封面路径
		tempMap.put("started", DateUtil.dateToString(stream.getStarted(), DateUtil.FORMAT_MONTH_DAY_SIMPLE));// 开始时间
		String playbackUri = stream.getPlaybackUri();// 播放URL
		String playUri = stream.getPlayUri();
		int isOnline = 0;
		if (stream.getStatus() == 1) {// 判断是否直播
			isOnline = 1;
		}
		int isSecret = 0;// 是否加密 0：否 1：是
		if (stream.getSecret() != null && !stream.getSecret().trim().equals("")) {// 判断是个加密
			isSecret = 1;
			playbackUri = "";
			playUri = "";
		}
		tempMap.put("isOnline", isOnline);// 是否直播 0：否 1：是
		tempMap.put("playUri", playUri);// 播放URL
		tempMap.put("playbackUri", playbackUri);// 回放播放URL
		tempMap.put("isSecret", isSecret);// 是否加密 0：否 1：是
		tempMap.put("isVR", stream.getType().equals(StreamType.VR.toString()) ? 1 : 0);// 是否VR
																						// 0：否
																						// 1：是
		tempMap.put("userId", stream.getUserId());
		return tempMap;
	}

	/**
	 * 删除视频<br>
	 * 
	 * @param userId
	 *            用户uuid
	 * @param uuid
	 *            视频uuid
	 * @return
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED, readOnly = false, propagation = Propagation.REQUIRED, value = "transactionManager")
	public Integer deleteStream(String userId, String uuid) {
		return streamRepository.deleteByUserIdAndUUId(userId, uuid);
	}

	/**
	 * 查询当前所有请求连接<br>
	 * 
	 * @param parentId
	 *            父类视频uuid
	 */
	public List<Stream> selectRequsetList(String parentId) {
		return streamRepository.selectRequsetList(parentId);
	}

	/**
	 * 批量关闭连接<br>
	 * 
	 * @param streamIds
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED, readOnly = false, propagation = Propagation.REQUIRED, value = "transactionManager")
	public Integer closeStreams(List<String> streamIds) {
		return streamRepository.closeStreams(StreamStatus.Ended.ordinal(), new Date(), streamIds);
	}

	/**
	 * 查询当前游标位置<br>
	 * 
	 * @param id
	 *            stream的id
	 * @param type
	 *            类型 1：时间 2：攒数
	 * @return
	 */
	public Integer selectStreamIndex(String uuid, String categoryId, Integer status, Integer type) {
		// step：1查询当前游标信息
		Stream stream = streamRepository.findByUuid(uuid);
		Integer index = 0;
		if (type.intValue() > 0) {// 根据时间排序
			Sort sort = new Sort(Direction.DESC, "started");
			index = streamRepository.selectByStartedGreaterThan(status,
					stream == null ? new Date() : stream.getStarted(), sort);
		} else {// 根据攒数排序
			Sort sort = new Sort(Direction.DESC, "playCount");
			index = streamRepository.selectByPlayCountGreaterThan(status, stream.getPlayCount(), stream.getId(), sort);
		}
		return index;
	}

	public Pages<Stream> selectStreamByUserIds(List<String> userIds, Integer pageNo, Integer pageSize) {
		// 总数量
		Integer count = streamRepository.selectByUserIds(userIds);
		List<Stream> list = new ArrayList<Stream>();// strean列表
		if (count > 0) {
			Sort sort = new Sort(Direction.DESC, "started");// 排序
			PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);// 分页查询
			Page<Stream> page = streamRepository.selectByUserIds(userIds, pageRequest);
			list = page.getContent();
		}
		return new Pages<Stream>(count, list);
	}

	/**
	 * 更新攒数<br>
	 * 
	 * @param streamId
	 * @param count
	 * @return
	 */
	@Transactional
	public Integer updatePraise(String streamId, Integer count) {
		return streamRepository.updatePraise(count, streamId);
	}

	/**
	 * 更新观看次数<br>
	 * 
	 * @param streamId
	 * @param count
	 * @return
	 */
	@Async
	@Transactional
	public Integer updatePlayCount(String streamId, Integer count) {
		return streamRepository.updatePlayCount(count, streamId);
	}

	/**
	 * 根据streamId列表查询stream信息<br>
	 * 
	 * @param streamIds
	 * @return List<Stream>
	 */
	public List<Stream> selectByUuids(List<String> streamIds) {
		return streamRepository.selectByUuids(streamIds);
	}

	/**
	 * 获取连线流
	 * 
	 * @param userId
	 * @param parentId
	 * @return Iterable<Stream>
	 */
	public Iterable<Stream> findConnectStream(String userId, String parentId) {
		return streamRepository.selectRequsetList(userId, parentId);
	}

	/**
	 * 查询当前用户正在直播的stream
	 * 
	 * @param userId
	 */
	public List<Stream> selectOnLine(String userId) {
		Sort sort = new Sort(Direction.DESC, "started");
		return streamRepository.findByUserIdAndStatus(userId, StreamStatus.Publishing.ordinal(), sort);
	}

	/**
	 * 保存stream信息<br>
	 * 
	 * @param stream
	 * @return
	 */
	public Stream save(Stream stream) {
		return streamRepository.save(stream);
	}

	/**
	 * 获取用户视频列表<br>
	 * 
	 * @param userId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public Pages<Stream> findAllStreams(String userId, Integer pageNo, Integer pageSize) {
		// 总数量
		Integer count = streamRepository.findCountByUserId(userId);
		List<Stream> list = new ArrayList<Stream>();// strean列表
		// step:1
		if (count > 0) {
			Sort sort = new Sort(Direction.DESC, "started");// 排序
			PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);// 分页查询
			Page<Stream> page = streamRepository.findByUserId(userId, pageRequest);
			list = page.getContent();
		}
		return new Pages<Stream>(count, list);
	}

	/**
	 * 查询正在推流的视频<br>
	 * 
	 * @return List<Stream>
	 */
	public List<Stream> findPublishingStream() {
		return streamRepository.findPublishingStream();
	}

	/**
	 * 更新推流状态<br>
	 */
	@Transactional
	public Integer updateStreamStatus(Integer streamStatus, Date streamUpdateDate, String streamId) {
		return streamRepository.updateStreamStatus(streamStatus, streamUpdateDate, streamId);
	}

	/**
	 * 更新回放地址<br>
	 */
	@Transactional
	public Stream updatePlayBackUri(String playBackUrl, Stream stream) {
		if (stream != null) {
			logger.info("updatePlayBackUri, before updatePlayBackUri: " + stream.getPlaybackUri() + ", streamId = "
					+ stream.getUuid());
		} else {
			return null;
		}
		String url = "";
		if (StringUtils.isNotBlank(stream.getPlaybackUri())) {
			url = stream.getPlaybackUri() + "," + playBackUrl;
		} else {
			url = playBackUrl;
		}
		stream.setPlaybackUri(url);
		return streamRepository.save(stream);
	}

	public List<Stream> findByUserIdAndStatus(String userId, int status) {
		return streamRepository.findByUserIdAndStatus(userId, status);
	}

	/**
	 * 查询用户未结束的流
	 * 
	 * @param userId
	 * @return
	 */
	public List<Stream> userActiveStream(String userId) {
		return streamRepository.findByUserAndActive(userId);
	}

	@Transactional
	public Integer updateStreamCategory(String categoryNew, String categoryOld) {
		return streamRepository.updateStreamCategory(categoryNew, categoryOld);
	}

	public Stream findUserPublishingStream(String userId) {
		List<Stream> list = streamRepository.findPublishingStream(userId);
		if (list == null || list.isEmpty())
			return null;
		else
			return list.get(0);
	}
}
