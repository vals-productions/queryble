package com.vals.app.dao;

import java.sql.SQLException;
import java.util.List;

import com.vals.app.model.User;
import com.vals.queryble.QuerybleDescriptor;

public interface UserDao {

	List<User> findUsersJdbc(User searchUser) throws Exception;

	List<User> findUsersQuerybleJdbc(User searchUser) throws Exception;


	
	List<User> findUsersQuerybleHql(User searchUser) throws Exception;
	
	List<User> findAllUsersHql();

	void create();

	List<User> findUsersWithAddressesHql()  throws Exception ;
	
	List<User> findUsersWithAddressesQuerybleHql()  throws Exception ;


	List<User> findUsersQueryble3() throws Exception;

	List<User> findUsersQuerybleJdbcReq(User searchUser) throws Exception;

	List<User> findWithQuerybleHql(User searchUserObject, QuerybleDescriptor querybleDescriptor) throws Exception;

}
