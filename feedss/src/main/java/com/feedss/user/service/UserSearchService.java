package com.feedss.user.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.feedss.user.entity.User;
import com.feedss.user.repository.UserRepository;

@Component
public class UserSearchService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${elasticSearchHost}")
	private String host;
	@Value("${elasticSearchPort}")
	private int port;

	@Autowired
	private UserRepository userRepository;

	public SearchHits keywordSearch(String keyword, int from, int size) {
		SearchHits hits = null;
//		List<User> result = new ArrayList<User>();
		try {
			Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
			SearchResponse response = client.prepareSearch("live").setTypes("profile").setSearchType(SearchType.QUERY_AND_FETCH).addField("user_id")
					.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("nickname", keyword)).must(QueryBuilders.matchQuery("status", "1"))).setFrom(from).setSize(size).setExplain(true).execute()
					.actionGet();
			hits = response.getHits();
			log.info("Total: {}", hits.getTotalHits());
//			for (SearchHit hit : hits.getHits()) {
//				String userId = hit.getFields().get("user_id").getValue().toString();
//				log.info("uuid: {}", userId);
//				result.add(userRepository.findOne(userId));
//			}
			client.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return hits;
	}
}
