package com.portfolio.blogifyx.RepositoryTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.portfolio.blogifyx.models.User;
import com.portfolio.blogifyx.repository.UserRepository;

@SpringBootTest
public class UserRepositoryTest {
	
	@Autowired
	private UserRepository userRepository;
	
	@Test
	public void testFindByUsername() {
		User user = new User();
		user.setUsername("usernametest");
		user.setEmail("issam@gmail.com");
		user.setPassword("abcd");
		
		userRepository.save(user);
		
		//retrieve the user
		User retrieved = userRepository.findByUsername("usernametest").orElse(null);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getUsername()).isEqualTo("usernametest");
		
	}

}
