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
1. Follow "query by example" pattern and dynamically compose search criteria based on email, first and last name values. If either value is null, the query will not filter on it. This behavior is tunable.
2. Transform your query into record counting query and report total record count for your actual search criteria. It will not join to address table for record counting since this is faster and will not affect record count results.
3. Solve “HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!” hibernate issue if instructed to do so.
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
**Question:** How does it solve “HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!” hibernate issue?

**Background:** Sometime paginated queries involve fetching ManyToOne relationships (User/Address in example above). This does not let Hibernate count records by means of database query only. Hibernte solves this by doing post-processing of the data in your Java application. While this is not a big deal for tables with predictably low number of records, HHH000104 should not be ignored for tableswith unlimited growth. It will lead to java.lang.OutOfMemoryError before you know it. 

**Answer:** Queryble solves the issue by executng 3 step pagination. If we use above example to explain Queryble execution plan, then the following would happen:

1. Execute ```SELECT (*) ``` query to retrieve total counts.
2. Execute ```SELECT u.uuid FROM User u ... limit N, offset M ``` ***paginated query without fetching Address relation***. ```q.withEntityIdFor3StepPagination("u.uuid")``` giver queryble a hint on what query's primary id is.
3. Execute ```FROM User u LEFT JOIN FETCH u.addresses ..... WHERE u.uuid IN (UUIDs retrieved at (2)) AND ...```  not paginated query, **but** pagination is implicitly enforced with ```u.uuid IN (UUIDs retrieved at (2))```.






