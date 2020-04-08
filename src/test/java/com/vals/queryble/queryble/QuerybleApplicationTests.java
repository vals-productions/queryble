package com.vals.queryble.queryble;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.vals.app.dao.UserDao;
import com.vals.app.model.User;

//@ExtendWith(SpringExtension.class)
@SpringBootTest
//@DataJpaTest
class QuerybleApplicationTests {
	
	@Autowired
	private UserDao userDao;

	@Test
	void contextLoads() {
		List<User> users = userDao.findAllUsersHql();
		
		assertTrue(users != null);
	}

}
