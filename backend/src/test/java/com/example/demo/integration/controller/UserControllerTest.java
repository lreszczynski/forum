package com.example.demo.integration.controller;

import com.example.demo.categories.CategoryRepository;
import com.example.demo.categories.CategoryService;
import com.example.demo.posts.PostService;
import com.example.demo.roles.RoleRepository;
import com.example.demo.roles.RoleService;
import com.example.demo.security.RoleContainer;
import com.example.demo.security.SecurityUtility;
import com.example.demo.threads.ThreadRepository;
import com.example.demo.threads.ThreadService;
import com.example.demo.users.UserService;
import com.example.demo.users.dto.UserDTO;
import com.example.demo.users.dto.UserProfileDTO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class UserControllerTest {
	@LocalServerPort
	private int port;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private ThreadRepository threadRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private PostService postService;
	@Autowired
	private ThreadService threadService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private UserService userService;
	@Autowired
	private RoleService roleService;
	
	Long userId = 1L;
	Long bannedUserId = 3L;
	Long modId = 4L;
	Long adminId = 5L;
	
	private String token;
	
	void initData() {
	}
	
	ImmutablePair<String, String> getCredentialsFor(String role) {
		switch (role) {
			case RoleContainer.USER:
				return new ImmutablePair<>("user123", "user123");
			case RoleContainer.MODERATOR:
				return new ImmutablePair<>("mod123", "mod123");
			case RoleContainer.ADMIN:
				return new ImmutablePair<>("admin123", "admin123");
		}
		return new ImmutablePair<>("", "");
	}
	
	String getJwtToken(String role) {
		ImmutablePair<String, String> credentials = getCredentialsFor(role);
		//@formatter:off
		return RestAssured
				.given()
					.contentType("multipart/form-data")
						.multiPart("username", credentials.getLeft())
						.multiPart("password", credentials.getRight())
				.when()
					.post(SecurityUtility.LOGIN_PATH)
				.then()
					.statusCode(HttpStatus.OK.value())
					.extract().path("accessToken");
		//@formatter:on
	}
	
	@BeforeEach
	public void setUp() throws SQLException {
		System.out.println("setUp");
		Connection connection = dataSource.getConnection();
		ScriptUtils.executeSqlScript(connection, new ClassPathResource("database-scripts/delete.sql"));
		ScriptUtils.executeSqlScript(connection, new ClassPathResource("database-scripts/create.sql"));
		ScriptUtils.executeSqlScript(connection, new ClassPathResource("database-scripts/insert.sql"));
		connection.close();
		RestAssured.port = port;
		initData();
	}
	
	@AfterEach
	void tearDown() {
		System.out.println("tearDown");
	}
	
	@Nested
	@DisplayName("If user is a GUEST")
	class GuestTests {
		@BeforeEach
		public void setUp() {
		
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			//@formatter:off
			RestAssured
					.given()
					.when()
					.get(SecurityUtility.USERS_PATH)
					.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void getUserAccountSettingsShouldSucceed() {
			UserDTO userDTO = userService.getById(userId).orElseThrow();
			
			//@formatter:off
			RestAssured
					.given()
					.when()
					.get(SecurityUtility.USERS_PATH+"/account")
					.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void getUserPublicProfileShouldSucceed() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			
			//@formatter:off
			RestAssured
					.given()
					.when()
					.get(SecurityUtility.USERS_PATH+"/profile/"+userDTO.getId())
					.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void updateShouldFail() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			UserDTO updated = userDTO.withBanned(false);
			
			//@formatter:off
			RestAssured
					.given()
					.body(updated).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.USERS_PATH+"/"+userDTO.getId())
					.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldSucceedIfEntityExists() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			//@formatter:off
			RestAssured
					.given()
					.when()
					.delete(SecurityUtility.USERS_PATH+"/"+userDTO.getId())
					.then()
					.statusCode(HttpStatus.UNAUTHORIZED.value());
			//@formatter:on
		}
	}
	
	@Nested
	@DisplayName("If user has a role USER")
	class UserTests {
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.USER);
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.USERS_PATH)
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void getUserAccountSettingsShouldSucceed() {
			UserDTO userDTO = userService.getById(userId).orElseThrow();
			
			//@formatter:off
			UserDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.USERS_PATH+"/account")
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(UserDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(userDTO);
		}
		
		@Test
		void getUserPublicProfileShouldSucceed() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			
			//@formatter:off
			UserProfileDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.USERS_PATH+"/profile/"+userDTO.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(UserProfileDTO.class);
			//@formatter:on
			
			assertThat(result.getUsername()).isEqualTo(userDTO.getUsername());
		}
		
		@Test
		void updateShouldFail() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			UserDTO updated = userDTO.withBanned(false);
			
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.body(updated).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.USERS_PATH+"/"+userDTO.getId())
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void deleteShouldSucceedIfEntityExists() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.delete(SecurityUtility.USERS_PATH+"/"+userDTO.getId())
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
	}
	
	@Nested
	@DisplayName("If user has a role MODERATOR")
	class ModeratorTests {
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.MODERATOR);
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.USERS_PATH)
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
		
		@Test
		void getUserAccountSettingsShouldSucceed() {
			UserDTO userDTO = userService.getById(modId).orElseThrow();
			
			//@formatter:off
			UserDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.USERS_PATH+"/account")
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(UserDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(userDTO);
		}
		
		@Test
		void getUserPublicProfileShouldSucceed() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			
			//@formatter:off
			UserProfileDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.USERS_PATH+"/profile/"+userDTO.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(UserProfileDTO.class);
			//@formatter:on
			
			assertThat(result.getUsername()).isEqualTo(userDTO.getUsername());
		}
		
		@Test
		void updateShouldSucceedIfDataIsValid() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			UserDTO updated = userDTO.withBanned(false);
			
			//@formatter:off
			UserDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.body(updated).contentType(ContentType.JSON)
					.when()
					.put(SecurityUtility.USERS_PATH+"/"+userDTO.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(UserDTO.class);
			//@formatter:on
			assertThat(result.isBanned()).isEqualTo(updated.isBanned());
		}
		
		@Test
		void deleteShouldSucceedIfEntityExists() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			//@formatter:off
			RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.delete(SecurityUtility.USERS_PATH+"/"+userDTO.getId())
					.then()
					.statusCode(HttpStatus.FORBIDDEN.value());
			//@formatter:on
		}
	}
	
	@Nested
	@DisplayName("If user has a role ADMIN")
	class AdminTests {
		@BeforeEach
		public void setUp() {
			token = getJwtToken(RoleContainer.ADMIN);
		}
		
		@Test
		void getAllShouldReturnAllEntities() {
			Page<UserDTO> page = userService.getAll(Pageable.ofSize(20));
			
			//@formatter:off
			List<UserDTO> list = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
						.get(SecurityUtility.USERS_PATH)
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().body().jsonPath().getList("content",UserDTO.class);
			//@formatter:on
			
			assertThat(list).isEqualTo(page.getContent());
		}
		
		@Test
		void getUserAccountSettingsShouldSucceed() {
			UserDTO userDTO = userService.getById(adminId).orElseThrow();
			
			//@formatter:off
			UserDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.USERS_PATH+"/account")
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(UserDTO.class);
			//@formatter:on
			
			assertThat(result).isEqualTo(userDTO);
		}
		
		@Test
		void getUserPublicProfileShouldSucceed() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			
			//@formatter:off
			UserProfileDTO result = RestAssured
					.given()
					.auth().oauth2(token)
					.when()
					.get(SecurityUtility.USERS_PATH+"/profile/"+userDTO.getId())
					.then()
					.statusCode(HttpStatus.OK.value())
					.extract().as(UserProfileDTO.class);
			//@formatter:on
			
			assertThat(result.getUsername()).isEqualTo(userDTO.getUsername());
		}
		
		@Test
		void updateShouldSucceedIfDataIsValid() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			UserDTO updated = userDTO.withBanned(false);
			
			//@formatter:off
			UserDTO result = RestAssured
					.given()
						.auth().oauth2(token)
						.body(updated).contentType(ContentType.JSON)
					.when()
						.put(SecurityUtility.USERS_PATH+"/"+userDTO.getId())
					.then()
						.statusCode(HttpStatus.OK.value())
						.extract().as(UserDTO.class);
			//@formatter:on
			assertThat(result.isBanned()).isEqualTo(updated.isBanned());
		}
		
		@Test
		void deleteShouldSucceedIfEntityExists() {
			UserDTO userDTO = userService.getById(bannedUserId).orElseThrow();
			//@formatter:off
			RestAssured
					.given()
						.auth().oauth2(token)
					.when()
						.delete(SecurityUtility.USERS_PATH+"/"+userDTO.getId())
					.then()
						.statusCode(HttpStatus.NO_CONTENT.value());
			//@formatter:on
			assertThat(userService.getById(userDTO.getId())).isEmpty();
		}
	}
}