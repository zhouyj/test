package com.feedss.portal.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.feedss.portal.entity.Interaction;
import com.feedss.user.repository.BaseRepository;

@Repository
public interface InteractionRepository extends BaseRepository<Interaction> {


	@Query("select i from Interaction i where i.creator=?1 and i.type= ?2 and i.streamId = ?3 and i.isCreatorDel=0 order by i.created desc,i.id desc")
	public List<Interaction> selectByCreator(String userId, String type, String streamId);

	@Query("select i from Interaction i where i.receiver=?1 and i.type= ?2 and i.streamId = ?3 and i.isReceiverDel=0 order by i.created desc,i.id desc")
	public List<Interaction> selectByReceiver(String userId, String type, String streamId);
	
	@Query("select count(1) from Interaction i where i.creator=?1 and i.type= ?2 and i.isCreatorDel=0")
	public int selectCountByCreator(String userId, String type);
	
	@Query("select count(1) from Interaction i where i.receiver=?1 and i.type= ?2 and i.isReceiverDel=0")
	public int selectCountByReceiver(String userId, String type);
	
	@Query("select i from Interaction i where i.creator=?1 and i.type= ?2 and i.isCreatorDel=0 and i.streamId=?3 order by i.created desc,i.id desc")
	public List<Interaction> selectLatestByCreatorAndStream(String userId, String type, String streamId);
	
	@Query("select i from Interaction i where i.receiver=?1 and i.type= ?2 and i.isReceiverDel=0 and i.streamId=?3 order by i.created desc,i.id desc")
	public List<Interaction> selectLatestByReceiverAndStream(String userId, String type, String streamId);
	
	@Query("select i from Interaction i where i.created<?1 and i.status= ?2 order by i.created desc,i.id desc")
	public List<Interaction> selectOverdue(Date time, int status);
	
	@Query("select sum(i.price) from Interaction i where i.streamId=?1 and i.status!=2 and i.isReceiverDel=false")
	public int getTotalIncome(String streamId);
	
	@Query("select count(1) from Interaction i where i.streamId=?1 and i.type=?2 and i.isReceiverDel=false")
	public int getNum(String streamId, String type);
	
	@Query("select count(1) from Interaction i where i.streamId=?1 and i.status!=2 and i.isReceiverDel=false")
	public int getNum(String streamId);
}
