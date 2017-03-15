package com.feedss.portal.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.feedss.content.entity.Stream;
import com.feedss.content.entity.Stream.StreamStatus;
import com.feedss.content.service.StreamService;
import com.feedss.portal.entity.PushStreamTerminal;
import com.feedss.portal.repository.PushStreamTerminalRepository;
import com.feedss.portal.service.TerminalService;

@Service
public class TerminalServiceImpl implements TerminalService {

	@Autowired
	PushStreamTerminalRepository pushStreamTerminalRepository;
	@Autowired
	StreamService streamService;

	@Override
	public PushStreamTerminal save(PushStreamTerminal t) {
		return pushStreamTerminalRepository.save(t);
	}

	@Transactional
	@Override
	public PushStreamTerminal bindUser(String terminalId, String userId) {
		PushStreamTerminal t = pushStreamTerminalRepository.findOne(terminalId);
		if (t == null)
			return null;
		t.setUserId(userId);
		return pushStreamTerminalRepository.save(t);
	}

	@Override
	public Page<PushStreamTerminal> get(final String userId, final String name, Pageable pageable) {
		Specification<PushStreamTerminal> spec = new Specification<PushStreamTerminal>() {
			public Predicate toPredicate(Root<PushStreamTerminal> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				list.add(cb.equal(root.get("status").as(Integer.class), PushStreamTerminal.Status.NORMAL.ordinal()));
				if (StringUtils.isNotBlank(userId)) {
					list.add(cb.equal(root.get("userId").as(String.class), userId));
				}
				if (StringUtils.isNotBlank(name)) {
					list.add(cb.equal(root.get("name").as(String.class), name));
				}

				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
		return pushStreamTerminalRepository.findAll(spec, pageable);
	}

	@Override
	public void del(String terminalId) {
		pushStreamTerminalRepository.delete(terminalId);

	}

	@Override
	public PushStreamTerminal get(String terminalId) {
		return pushStreamTerminalRepository.findOne(terminalId);
	}

	@Override
	public List<PushStreamTerminal> getByUserId(String userId) {
		return pushStreamTerminalRepository.findByUserIdAndStatus(userId, PushStreamTerminal.Status.NORMAL.ordinal());
	}

	@Override
	public PushStreamTerminal getByUserIdAndName(String userId, String name) {
		return pushStreamTerminalRepository.findByUserIdAndName(userId, name);
	}

	@Override
	public String getPublishingStream(String publishUrl) {
		List<PushStreamTerminal> list = pushStreamTerminalRepository.findByPublishUrl(publishUrl,
				PushStreamTerminal.Status.NORMAL.ordinal());
		if (list == null || list.isEmpty())
			return null;
		for (PushStreamTerminal t : list) {
			String streamId = t.getStreamId();
			Stream s = streamService.findStreamById(streamId);
			if (s != null && !s.isDeleted() && s.getStatus() == StreamStatus.Publishing.ordinal()) {
				return streamId;
			}
		}
		return null;
	}

	@Override
	public Stream getLatestStream(String publishUrl) {
		List<PushStreamTerminal> list = pushStreamTerminalRepository.findByPublishUrl(publishUrl,
				PushStreamTerminal.Status.NORMAL.ordinal());
		if (list == null || list.isEmpty())
			return null;
		Stream stream = null;
		for (PushStreamTerminal t : list) {
			String id = t.getStreamId();
			Stream s = streamService.findStreamById(id);
			if (s != null && !s.isDeleted()) {
				if(s.getStatus()==StreamStatus.Ended.ordinal() && System.currentTimeMillis() > (s.getUpdated().getTime() + 60*1000)){//已经超期了,防止ondvr事件错误叠加
					t.setStreamId("");
					pushStreamTerminalRepository.save(t);
					continue;
				}
				if (stream==null || s.getCreated().after(stream.getCreated())) {
					stream = s;
				}
			}
		}

		return stream;
	}

	@Override
	public PushStreamTerminal getByStreamId(String streamId) {
		return pushStreamTerminalRepository.findByStreamId(streamId);
	}

}
