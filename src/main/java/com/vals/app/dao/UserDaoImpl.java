package com.vals.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.query.Query;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.hibernate.transform.RootEntityResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.vals.app.model.Address;
import com.vals.app.model.Company;
import com.vals.app.model.User;
import com.vals.queryble.Queryble;
import com.vals.queryble.QuerybleDescriptor;
import com.vals.queryble.QuerybleUtils;
import com.vals.queryble.hibernate.QuerybleHibernate;
import com.vals.queryble.jdbc.QuerybleJdbc;

@Repository
@Transactional
public class UserDaoImpl implements UserDao {
	
	@Autowired
	private SessionFactory hibernateSessionFactory;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void create() {
	}
	
	@Override
	public List<User> findUsersJdbc(User searchUser) throws Exception {
		Connection connection = jdbcTemplate.getDataSource().getConnection();
		PreparedStatement ps = connection.prepareStatement(
				" select u.uuid, u.firstName, u.lastName, u.email, u.companyUuid "
				+ " from User u "
				+ " where u.email = ? "
				);
		ps.setString(1, searchUser.getEmail());
		ps.execute();
		ResultSet resultSet = ps.getResultSet();
		return processJdbcUsersQuery(resultSet);
	}
	
	@Override
	public List<User> findUsersQuerybleJdbc(User searchUser) throws Exception {
		QuerybleJdbc<ResultSet> jdbc = QuerybleJdbc.create(jdbcTemplate.getDataSource().getConnection());
		jdbc.gueryble()
		    .add("select u.uuid, u.firstName, u.lastName, u.email, u.companyUuid ")
			.addFrom()
			.add("User u")
			.addWhere()
			.add("and u.email =", searchUser.getEmail())
		    .add("and u.firstName =", searchUser.getFirstName()) // .withFlags("r")
		    .add("and u.lastName =", searchUser.getLastName());
		ResultSet resultSet = jdbc.result();
		return processJdbcUsersQuery(resultSet);
	}
	
	@Override
	public List<User> findUsersQuerybleJdbcReq(User searchUser) throws Exception {
		QuerybleJdbc<ResultSet> jdbc = QuerybleJdbc.create(jdbcTemplate.getDataSource().getConnection());
		jdbc.gueryble()
		    .add("select u.uuid, u.firstName, u.lastName, u.email, u.companyUuid ")
			.addFrom()
			.add("User u")
			.addWhere()
			.add("and u.email =", searchUser.getEmail())
		    .add("and u.firstName =", searchUser.getFirstName())
		    .add("and u.lastName =", searchUser.getLastName()).withFlags("r");
		ResultSet resultSet = jdbc.result();
		return processJdbcUsersQuery(resultSet);
	}

	private List<User> processJdbcUsersQuery(ResultSet resultSet) throws Exception {
		List<User> list = new LinkedList<>();
		while(resultSet.next()) {
			User user = new User();
			int idx = 1;
			user.setUuid(resultSet.getString(idx++));
			user.setFirstName(resultSet.getString(idx++));
			user.setLastName(resultSet.getString(idx++));
			user.setEmail(resultSet.getString(idx++));
			Company company = new Company();
			company.setUuid(resultSet.getString(idx++));
			user.setCompany(company);
			list.add(user);
		}
		return list;
	}
	
	@Override
	public List<User> findAllUsersHql() {
		@SuppressWarnings("unchecked")
		Query<User> q = (Query<User>)hibernateSessionFactory.
				getCurrentSession().
				createQuery("from User u");
		List<User> users = q.list();
		return users;
	}
	
	@Override
	public List<User> findUsersWithAddressesHql() throws Exception {
		@SuppressWarnings("unchecked")
		Query<User> q = (Query<User>)hibernateSessionFactory.
				getCurrentSession().
				createQuery("from User u inner join fetch u.addresses ");
		List<User> users = q.list();
		return users;
	}	
	
	@Override
	public List<User> findUsersWithAddressesQuerybleHql() throws Exception {
		QuerybleHibernate<List<User>> qh = QuerybleHibernate.create(hibernateSessionFactory.getCurrentSession());
		qh.gueryble()
			.addFrom().add("User u")
			.add("inner join fetch u.addresses")
			.addWhere().add("and u.email =", "joed@email.com");
		List<User> users = qh.result();
		return users;
	}
	
	@Override
	public List<User> findUsersQuerybleHql(User searchUser) throws Exception {
		QuerybleHibernate<List<User>> qh = QuerybleHibernate.create(hibernateSessionFactory.getCurrentSession());
		qh.gueryble()
			.addFrom().add("User u")
			.add("left join fetch u.addresses").withFlags("c")
			.addWhere()
			.add("and u.email =", searchUser.getEmail())
			.add("and u.firstName =", searchUser.getFirstName())
			.add("and u.lastName =", searchUser.getLastName());
		List<User> users = qh.result();
		return users;
	}

	@Override
	public List<User> findUsersQueryble3() throws Exception {
		QuerybleHibernate<List<Integer>> qhc = QuerybleHibernate.create(hibernateSessionFactory.getCurrentSession());
		qhc.gueryble()
			.addFrom().add("User u")
			.add("inner join fetch u.addresses").withFlags("c")
			.addWhere().add("and u.email =", "joed@email.com").withFlags("r")
			.add("and u.name =", "joed@email.com").withFlags("r");
		qhc.buildCount();
		List<Integer> userCount = qhc.result();
		
		QuerybleHibernate<List<User>> qh = QuerybleHibernate.create(hibernateSessionFactory.getCurrentSession());
		qh.gueryble()
			.addFrom().add("User u")
			.add("inner join fetch u.addresses")
			.addWhere().add("and u.email =", "joed@email.com").withFlags("r");
		qh.withResultTransformer(RootEntityResultTransformer.INSTANCE);
		List<User> users = qh.result();
		
		return users;
	}
	
	@Override
	public List<User> findWithQuerybleHql(User searchUser, QuerybleDescriptor querybleDescriptor) throws Exception {
		QuerybleHibernate<List<User>> q = QuerybleHibernate.create(hibernateSessionFactory.getCurrentSession());
		q.gueryble()
			.addFrom()
			.add("User u")
			.add("left join fetch u.addresses").withFlags("c")
			.addWhere()
			.add("and u.email =", searchUser.getEmail())
			.add("and u.firstName =", searchUser.getFirstName())
			.add("and u.lastName =", searchUser.getLastName());
			q.withQuerybleDescriptor(querybleDescriptor);
			q.withResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
			q.withEntityIdFor3StepPagination("u.uuid");
		List<User> users = q.result();	
		return users;
	}

	@Override
	public List<User> findWithQuerybleHqlPlaceholder(User searchUser, QuerybleDescriptor querybleDescriptor) throws Exception {
		QuerybleHibernate<List<User>> q = QuerybleHibernate.create(hibernateSessionFactory.getCurrentSession());
		q.gueryble()
			.addFrom()
			.add("User u")
			.add("left join fetch u.addresses").withFlags("c")
			.addWhere()
			.add("and u.email =", searchUser.getEmail())
			.add("and u.firstName =", searchUser.getFirstName())
			.add("and u.lastName like {} AND u.status = 1", searchUser.getLastName()).withFormat("%s%%");
			q.withQuerybleDescriptor(querybleDescriptor);
			q.withResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
			q.withEntityIdFor3StepPagination("u.uuid");
		List<User> users = q.result();
		return users;
	}
	
}
