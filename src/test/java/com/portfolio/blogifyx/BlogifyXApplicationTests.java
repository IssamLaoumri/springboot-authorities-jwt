package com.portfolio.blogifyx;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BlogifyXApplicationTests {
	
	@Autowired
    private DataSource dataSource;
	
	@Test
    public void testDatabaseConnection() {
        try {
            dataSource.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Database connection test failed.");
        }
    }

	@Test
	void contextLoads() {
	}

}
