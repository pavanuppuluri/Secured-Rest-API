package org.secureapp.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.secureapp.dto.UserDto;
import org.secureapp.dto.UserUpdateDto;
import org.secureapp.model.LoginUser;
import org.secureapp.model.Role;
import org.secureapp.model.User;
import org.secureapp.repository.BlackListedTokenRepository;
import org.secureapp.repository.RoleRepository;
import org.secureapp.repository.UserRepository;
import org.secureapp.util.LoggedInUserHelper;
import org.secureapp.util.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude= FlywayAutoConfiguration.class)
public class UserControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    TestUtil testUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    LoggedInUserHelper loggedInUserHelper;

    @Autowired
    BlackListedTokenRepository blackListedTokenRepository;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void init() {

        userRepository.deleteAll();
        roleRepository.deleteAll();
        blackListedTokenRepository.deleteAll();

        Role userRole=new Role();
        userRole.setName("ROLE_USER");
        Role adminRole=new Role();
        adminRole.setName("ROLE_ADMIN");
        roleRepository.save(userRole);
        roleRepository.save(adminRole);

    }

    @Test
    public void shouldBeAbleToRegisterUser() throws Exception {
        UserDto userDto=testUtil.generateUserDtos(1).get(0);
        String content=testUtil.mapToJson(userDto);

        MvcResult result =mockMvc.perform(
                post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                .andReturn();

        assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());

    }

    @Test
    public void validUserShouldBeAbleToLoginSuccessfully() throws Exception {

        User user=testUtil.generateUsers(1).get(0);
        userRepository.save(user);

        LoginUser loginUser=new LoginUser("username_0","welcome123");
        String content=testUtil.mapToJson(loginUser);

        MvcResult result =mockMvc.perform(
                post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                .andReturn();

        assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
        // Should return a token
        assertTrue(result.getResponse().getContentAsString().contains("token"));
    }

    @Test
    public void userLoginShouldFailIfUserDoesNotExist() throws Exception
    {
        LoginUser loginUser=new LoginUser("username_0","welcome123");
        String content=testUtil.mapToJson(loginUser);

        MvcResult result =mockMvc.perform(
                                        post("/user/login")
                                            .contentType(MediaType.APPLICATION_JSON)
                                                .content(content)
                                    )
                                    .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());
    }

    @Test
    public void userLoginShouldFailIfInvalidCredentials() throws Exception {
        User user=testUtil.generateUsers(1).get(0);
        userRepository.save(user);

        LoginUser loginUser=new LoginUser("username_0","INVALID_PASSWORD");
        String content=testUtil.mapToJson(loginUser);

        MvcResult result =mockMvc.perform(
                post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());
    }

    @Test
    public void getUserEndPointShouldNotBeAccessibleIfNotLoggedIn() throws Exception {
        MvcResult result =mockMvc.perform(
                get("/user/getuser").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());
    }

    @Test
    public void getUserEndPointShouldBeAccessibleIfLoggedInWithRoleUser() throws Exception {

        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_USER");


        MvcResult result =mockMvc.perform(
                get("/user/getuser").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
    }

    @Test
    public void getUserEndPointShouldBeAccessibleIfLoggedInWithRoleAdmin() throws Exception {

        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_ADMIN");

        MvcResult result =mockMvc.perform(
                get("/user/getuser").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
    }

    @Test
    public void getAllUsersEndPointShouldNotBeAccessibleIfNotLoggedIn() throws Exception {
        MvcResult result =mockMvc.perform(
                                    get("/user/getallusers")
                                            .contentType(MediaType.APPLICATION_JSON)
                                )
                               .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());
    }

    @Test
    public void getAllUsersEndPointShouldNotBeAccessibleIfLoggedInWithRoleUser() throws Exception {

        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_USER");


        MvcResult result =mockMvc.perform(
                get("/user/getallusers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(),result.getResponse().getStatus());
    }

    @Test
    public void getAllUsersEndPointShouldBeAccessibleIfLoggedInWithRoleAdmin() throws Exception {

        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_ADMIN");


        MvcResult result =mockMvc.perform(
                get("/user/getallusers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
    }

    @Test
    public void updateUserEndPointShouldNotBeAccessibleIfNotLoggedIn() throws Exception {
        UserUpdateDto userUpdateDto=new UserUpdateDto();
        String content=testUtil.mapToJson(userUpdateDto);

        MvcResult result =mockMvc.perform(
                put("/user/update")
                        .contentType(MediaType.APPLICATION_JSON).content(content)
                )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());
    }

    @Test
    public void updateUserEndPointShouldBeAccessibleIfLoggedInWithRoleUser() throws Exception {

        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_USER");

        UserUpdateDto userUpdateDto=UserUpdateDto.builder().username("username_0")
                .firstname("DUMMY_FIRSTNAME")
                .lastname("DUMMY_LASTNAME")
                .email("changed@email.test")
                .build();

        String content=testUtil.mapToJson(userUpdateDto);

        MvcResult result =mockMvc.perform(
                put("/user/update")
                        .contentType(MediaType.APPLICATION_JSON).content(content)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
    }

    @Test
    public void updateUserEndPointShouldBeAccessibleIfLoggedInWithRoleAdmin() throws Exception {

        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_ADMIN");

        UserUpdateDto userUpdateDto=UserUpdateDto.builder().username("username_0")
                .firstname("DUMMY_FIRSTNAME")
                .lastname("DUMMY_LASTNAME")
                .email("changed@email.test")
                .build();

        String content=testUtil.mapToJson(userUpdateDto);

        MvcResult result =mockMvc.perform(
                put("/user/update")
                        .contentType(MediaType.APPLICATION_JSON).content(content)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
    }

    @Test
    public void logoutUserEndPointShouldBeAccessibleIfLoggedInWithRoleUser() throws Exception {
        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_USER");


        MvcResult result =mockMvc.perform(
                delete("/user/logout").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
    }

    @Test
    public void logoutUserEndPointShouldBeAccessibleIfLoggedInWithRoleAdmin() throws Exception {
        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_ADMIN");


        MvcResult result =mockMvc.perform(
                delete("/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals("LOGOUT_SUCCESS",result.getResponse().getContentAsString());
    }

    @Test
    public void logoutUserEndPointShouldNotBeAccessibleIfNotLoggedIn() throws Exception {
        UserUpdateDto userUpdateDto=new UserUpdateDto();
        String content=testUtil.mapToJson(userUpdateDto);

        MvcResult result =mockMvc.perform(
                post("/user/logout").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());
    }

    @Test
    public void deleteUserEndPointShouldNotBeAccessibleIfNotLoggedIn() throws Exception {
        UserUpdateDto userUpdateDto=new UserUpdateDto();
        String content=testUtil.mapToJson(userUpdateDto);

        MvcResult result =mockMvc.perform(
                delete("/user/delete").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());
    }

    @Test
    public void deleteUserEndPointShouldBeAccessibleIfLoggedInWithRoleUser() throws Exception {
        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_USER");


        MvcResult result =mockMvc.perform(
                delete("/user/delete").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals("DELETE_SUCCESS",result.getResponse().getContentAsString());
    }

    @Test
    public void deleteUserEndPointShouldBeAccessibleIfLoggedInWithRoleAdmin() throws Exception {
        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_ADMIN");


        MvcResult result =mockMvc.perform(
                delete("/user/delete").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());
    }

    @Test
    public void getUpdateDeleteEndPointsShouldNotBeAccessibleAfterLogout() throws Exception {
        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_ADMIN");

        MvcResult result =mockMvc.perform(
                delete("/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals("LOGOUT_SUCCESS",result.getResponse().getContentAsString());

        result =mockMvc.perform(
                get("/user/getuser").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());

        result =mockMvc.perform(
                get("/user/getallusers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());

        result =mockMvc.perform(
                put("/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());

        result =mockMvc.perform(
                delete("/user/delete").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());

    }

    @Test
    public void getUpdateLogoutEndPointsShouldNotBeAccessibleAfterLogout() throws Exception {
        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_ADMIN");

        MvcResult result =mockMvc.perform(
                delete("/user/delete").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals("DELETE_SUCCESS",result.getResponse().getContentAsString());


        result =mockMvc.perform(
                get("/user/getuser").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());

        result =mockMvc.perform(
                get("/user/getallusers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());

        result =mockMvc.perform(
                put("/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());

        result =mockMvc.perform(
                delete("/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());

    }


    @Test
    public void expiredTokenShouldNotAllowUserToAccessEndPoints() throws Exception {
        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_ADMIN");
        Thread.sleep(10000);

        MvcResult result =mockMvc.perform(
                get("/user/getuser").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());

    }

    @Test
    public void tamperedTokenShouldNotAllowUserToAccessEndPoints() throws Exception {
        String authToken = performLoginAndGetTokenWithGivenRole("ROLE_ADMIN");

        MvcResult result =mockMvc.perform(
                get("/user/getuser").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.OK.value(),result.getResponse().getStatus());

        authToken=authToken+"ROLE_ADMIN";

        result =mockMvc.perform(
                get("/user/getuser").param("userName","username_0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+authToken)
        )
                .andReturn();

        assertEquals(HttpStatus.UNAUTHORIZED.value(),result.getResponse().getStatus());

    }

    String performLoginAndGetTokenWithGivenRole(String roleName) throws Exception
    {
        User user=testUtil.generateUsers(1).get(0);
        Role role = roleRepository.findByName(roleName);
        Role userRole=new Role(role.getId(),roleName);
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(userRole);

        user.setRoles(roleSet);

        userRepository.save(user);

        LoginUser loginUser=new LoginUser("username_0","welcome123");
        String content=testUtil.mapToJson(loginUser);

        MvcResult result =mockMvc.perform(
                post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
        )
                .andReturn();

        String token=result.getResponse().getContentAsString();
        token=token.substring(10,token.length()-2);
        return token;
    }






}
