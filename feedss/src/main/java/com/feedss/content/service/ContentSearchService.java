package com.feedss.content.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContentSearchService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${elasticSearchHost}")
	private String host;
	@Value("${elasticSearchPort}")
	private int port;

	// .must(QueryBuilders.matchQuery("status", "0"))
	public SearchHits keywordSearch(String keyword, int from, int size) {
		SearchHits hits = null;
//		List<Stream> result = new ArrayList<Stream>();
		try {
			Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
			SearchResponse response = client.prepareSearch("live").setTypes("stream").setSearchType(SearchType.QUERY_AND_FETCH).addField("uuid")
					.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("name", keyword)).must(QueryBuilders.matchQuery("is_delete", "0"))).setFrom(from).setSize(size).setExplain(true).execute()
					.actionGet();
			hits = response.getHits();
//			log.info("Total: {}", hits.getTotalHits());
//			for (SearchHit hit : hits.getHits()) {
//				String uuid = hit.getFields().get("uuid").getValue().toString();
//				log.info("uuid: {}", uuid);
//				result.add(streamRepository.findOne(uuid));
//			}
			client.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return hits;
	}

	public SearchHits geoSearch(double latitude, double longitude, int from, int size) {
		SearchHits hits = null;
		long start = System.currentTimeMillis();
//		List<Stream> result = new ArrayList<Stream>();
		try {
			Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));

			GeoDistanceQueryBuilder qb = new GeoDistanceQueryBuilder("location").point(latitude, longitude).distance(50, DistanceUnit.KILOMETERS)
					.geoDistance(GeoDistance.ARC);

			GeoDistanceSortBuilder sort = new GeoDistanceSortBuilder("location").point(latitude, longitude).unit(DistanceUnit.KILOMETERS).order(SortOrder.ASC)
					.geoDistance(GeoDistance.ARC);

			SearchResponse response = client.prepareSearch("live").setTypes("stream").setSearchType(SearchType.QUERY_AND_FETCH).addField("uuid")
					.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery()).must(QueryBuilders.matchQuery("is_delete", "0")).filter(qb)).addSort(sort).setFrom(from).setSize(size)
					.setExplain(true).execute().actionGet();
			hits = response.getHits();
//			log.info("Total: {} use {} ms", hits.getTotalHits(), (System.currentTimeMillis() - start));
//			for (SearchHit hit : hits.getHits()) {
//				String uuid = hit.getFields().get("uuid").getValue().toString();
//				Stream stream = streamRepository.findOne(uuid);
//				log.info("uuid: {}, position:{}, longitude:{}, latitude:{}", uuid, stream.getPosition(), stream.getLongitude(), stream.getLatitude());
//				result.add(stream);
//			}
			client.close();
			log.info("finish search total use {} ms", (System.currentTimeMillis() - start));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return hits;
	}
}
