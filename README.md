# Queryble

Queryble helps you with SQL (JDBC) or HQL (Hibernate QL) query composition and execution, simplifies and speeds up your development. 

If your goal is to provide results to some imaginary paginated UI page that lists users and their addresses:

| User name | Email | Address |
|---|---|---|
| John Doe | jd@email.com | 123 Main str., Pleasanton, CA |
|  |  | 2 South str., Cupertino, CA |
| Ann Lake | al@email.com | 2 Sea View str., San Leandro, CA |
| Paul Smith | ps@email.com | 7 Mountain str., San Leandro, CA |
| Jane Lake | ps@email.com | |
Page 1 of 2

Then code below will:
1. Follow "query by example" pattern and dynamically compose search criteria based on email, first and last name values. If either value is null, the query will not filter on it.
2. Transform your query into record counting query and report total record count for your actual search criteria. It wil not join to address table for record counting since this is faster and will not affect record count results.
3. Solve “HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!” hibernate issue.
4. Execute data retrieval query and fetch list of users with their addresse.
5. Build order by clause.


```java

	@Override
	public List<User> findWithQuerybleHql(User searchUser, QuerybleDescriptor querybleDescriptor) throws Exception {
		QuerybleHibernate<List<User>> q = QuerybleHibernate.create(hibernateSessionFactory.getCurrentSession());
		q.gueryble()
			.addFrom()
			.add("User u")
			.add("LEFT JOIN FETCH u.addresses").withFlags("c")
			.addWhere()
			.add("AND u.email =", searchUser.getEmail())
			.add("AND u.firstName =", searchUser.getFirstName())
			.add("AND u.lastName =", searchUser.getLastName());
			q.withQuerybleDescriptor(querybleDescriptor);
			q.withEntityIdFor3StepPagination("u.uuid");
		List<User> users = q.result();	
		return users;
	}

```

