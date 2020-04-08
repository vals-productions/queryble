package com.vals.app.controller;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vals.app.dao.UserDao;
import com.vals.app.model.Company;
import com.vals.app.model.User;
import com.vals.queryble.QuerybleDescriptor;


@Controller
public class WebController {
	
//	public static final int totalUserRecords
	
	@Autowired
	private UserDao userDao;
	
	@RequestMapping(value = "/testAll", method = RequestMethod.GET)
	public String test(Model model) throws Exception {
	
		testJdbc1(model);
		testJdbc2(model);
		testJdbc3(model);
		
		testQHql1(model);		
		
		return "view01";
	}

	@RequestMapping(value = "/testJdbc1", method = RequestMethod.GET)
	public String testJdbc1(Model model) throws Exception {
		System.out.println("testJdbc1");
		User searchUser = new User();
		searchUser.setEmail("jdoe@email.com");
		searchUser.setFirstName("Joe");
		
		Company searchCompany = new Company();
		searchCompany.setName("Food Co");
		searchUser.setCompany(searchCompany);
		
		List<User> users = userDao.findUsersJdbc(searchUser);
		assertTrue("JDBC returned 1 user", users.size() == 1);
		users = userDao.findUsersQuerybleJdbc(searchUser);
		assertTrue("JDBC Queryble returned 1 user", users.size() == 1);
		
		return "view01";
	}
	
	@RequestMapping(value = "/testJdbc2", method = RequestMethod.GET)
	public String testJdbc2(Model model) throws Exception {
		System.out.println("testJdbc2");
		List<User> allUsers = userDao.findAllUsersHql();
		User searchUser = new User();
//		searchUser.setFirstName("Joe");
		List<User> users = userDao.findUsersQuerybleJdbc(searchUser);
		assertTrue("JDBC Queryble returned all users", users.size() == allUsers.size());
		return "view01";
	}
	
	@RequestMapping(value = "/testJdbc3", method = RequestMethod.GET)
	public String testJdbc3(Model model) throws Exception {
		System.out.println("testJdbc3");
		
		User searchUser = new User();
		searchUser.setFirstName("Joe");
		
		List<User> users = null;
		
		try {
			users = userDao.findUsersQuerybleJdbcReq(searchUser);
		} catch (Throwable t) {
			assertTrue("Query failed", true);
		}
		
		assertTrue("Query failed", users == null);
		
		searchUser.setLastName("Doe");
		
		users = userDao.findUsersQuerybleJdbc(searchUser);
		assertTrue("JDBC Queryble returned 1 user", users.size() == 1);
		
		return "view01";
	}
	
	@RequestMapping(value = "/testQHql1", method = RequestMethod.GET)
	public String testQHql1(Model model) throws Exception {
		System.out.println("testQHql1");
		QuerybleDescriptor querybleDescriptor = new QuerybleDescriptor();
		querybleDescriptor.pageNumber = 1l;
		querybleDescriptor.pageSize = 2l;
		querybleDescriptor.sortMap = new HashMap<Integer, AbstractMap.SimpleEntry<String,String>>();
		querybleDescriptor.sortMap.put(1, new AbstractMap.SimpleEntry<String,String>("u.lastName", "asc"));
		querybleDescriptor.sortMap.put(2, new AbstractMap.SimpleEntry<String,String>("u.firstName", "asc"));
		
		User searchUser = new User();
		
		List<User> users = userDao.findWithQuerybleHql(searchUser, querybleDescriptor);
		
 		return "view01";
	}
	
	private void assertTrue(String name, boolean condition) throws Exception {
		if (!condition) {
			throw new Exception("Test Failed " + (name == null ? "" : ": " + name));
		}
	}

}
