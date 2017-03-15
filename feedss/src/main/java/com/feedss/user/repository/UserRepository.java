package com.feedss.user.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.feedss.user.entity.User;

public interface UserRepository extends BaseRepository<User>, JpaSpecificationExecutor<User> {

	@Query("select u from User u where u.mobile=:mobile ")
	public User findByMobile(@Param("mobile") String mobile);

	@Query("select u from User u where u.thirdpartyName=:thirdpartyName and u.thirdpartyId=:thirdpartyId")
	public User findByThirdparty(@Param("thirdpartyName") String thirdpartyName,
			@Param("thirdpartyId") String thirdpartyId);

	@Query("select u from User u where u.username=:username or u.mobile=:username")
	public User getByUserNameAndMobile(@Param("username") String userName);

	@Query("select u from User u where u.username=:username and u.password=:password")
	public User findByUserNamePassword(@Param("username") String userName, @Param("password") String password);

	@Modifying
	@Query("update User u set u.status=:status where u.uuid=:userId")
	public int updateStatus(@Param("userId") String userId, @Param("status") int status);

	@Modifying
	@Query("update User u set u.username=:username,u.password=:password" + " where u.uuid=:userId")
	public int updateUsernameAndPassword(@Param("username") String username, @Param("password") String password,
			@Param("userId") String userId);

	@Modifying
	@Query("update User u set u.password=:password where u.uuid=:userId")
	public int updatePassword(@Param("password") String password, @Param("userId") String userId);
}