package org.secureapp.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.secureapp.dto.UserDto;
import org.secureapp.dto.UserUpdateDto;
import org.secureapp.exception.InvalidUserException;
import org.secureapp.exception.RecordAlreadyExistsException;
import org.secureapp.model.User;
import org.secureapp.repository.UserRepository;
import org.secureapp.service.impl.UserServiceImpl;
import org.secureapp.util.LoggedInUserHelper;
import org.secureapp.util.TestUtil;
import org.secureapp.validation.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration(exclude= FlywayAutoConfiguration.class)
public class UserServiceTest {

    @InjectMocks
    @Autowired
    UserServiceImpl userService;

    @InjectMocks
    @Autowired
    ValidationHelper validationHelper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestUtil testUtil;

    @Mock
    LoggedInUserHelper loggedInUserHelper;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void init() {
        userRepository.deleteAll();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void userRegistrationShouldBeSuccessful()
    {
        UserDto userDto=testUtil.generateUserDtos(1).get(0);
        User user=null;
        user=userService.save(userDto);
        assertNotNull(user);
    }

    @Test
    public void userRegistrationWithDuplicateUserNameOrEmailShouldFail()
    {
        UserDto userDto=testUtil.generateUserDtos(1).get(0);
        userService.save(userDto);

        exceptionRule.expect(RecordAlreadyExistsException.class);
        exceptionRule.expectMessage("User with the given username/email already exists");
        userService.save(userDto);
    }

    @Test
    public void normalUserShouldBeAbleToFindOwnDetails()
    {
        UserDto userDto=testUtil.generateUserDtos(1).get(0);
        User user=userService.save(userDto);
        testUtil.enableNormalUserMode(loggedInUserHelper, user);

        User savedUser = userService.find(user.getUsername());
        assertNotNull(savedUser);
    }

    @Test
    public void normalUserIfTryToFindOtherUserDetailsShouldFail()
    {
        List<UserDto> userDtos=testUtil.generateUserDtos(2);
        User user1=userService.save(userDtos.get(0));
        User user2=userService.save(userDtos.get(1));

        testUtil.enableNormalUserTryUpdatingOtherUserDataMode(loggedInUserHelper, user1);

        exceptionRule.expect(InvalidUserException.class);
        exceptionRule.expectMessage("Invalid user name");

        User savedUser = userService.find(user2.getUsername());

    }

    @Test
    public void adminUserShouldBeAbleToFindOtherUserDetails()
    {
        List<UserDto> userDtos=testUtil.generateUserDtos(2);
        User user1=userService.save(userDtos.get(0));
        User user2=userService.save(userDtos.get(1));

        testUtil.enableAdminMode(loggedInUserHelper);

        User savedUser = userService.find(user1.getUsername());
        assertNotNull(savedUser);
        savedUser = userService.find(user2.getUsername());
        assertNotNull(savedUser);

    }

    @Test
    public void adminUserShouldBeAbleToFindAllUsers()
    {
        List<UserDto> userDtos=testUtil.generateUserDtos(2);
        User user1=userService.save(userDtos.get(0));
        User user2=userService.save(userDtos.get(1));

        testUtil.enableAdminMode(loggedInUserHelper);

        assertEquals(2,userService.findAllUsers().size());
    }

    @Test
    public void normalUserShouldBeAbleToUpdateOwnDetails()
    {
       User user = userService.save(
                                testUtil.generateUserDtos(1).get(0)
                            );
       testUtil.enableNormalUserMode(loggedInUserHelper, user);

       UserUpdateDto userUpdateDto=UserUpdateDto.builder().username(user.getUsername())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email("changed@email.test")
                .build();


        User updatedUser = userService.update(userUpdateDto);

        assertEquals("changed@email.test", updatedUser.getEmail());
    }

    @Test
    public void userUpdateWithDuplicateEmailShouldFail()
    {
        List<UserDto> userDtos=testUtil.generateUserDtos(2);
        User user1=userService.save(userDtos.get(0));
        User user2=userService.save(userDtos.get(1));

        testUtil.enableNormalUserMode(loggedInUserHelper, user1);

        UserUpdateDto userUpdateDto=UserUpdateDto.builder().username(user2.getUsername())
                .firstname(user2.getFirstname())
                .lastname(user2.getLastname())
                .email(user1.getEmail())
                .build();

        exceptionRule.expect(RecordAlreadyExistsException.class);
        exceptionRule.expectMessage("User with same email already exists");

        User updatedUser = userService.update(userUpdateDto);
    }

    @Test
    public void normalUserIfTryToUpdateOtherUserDetailsShouldFail()
    {
        List<UserDto> userDtos=testUtil.generateUserDtos(2);
        User user1=userService.save(userDtos.get(0));
        User user2=userService.save(userDtos.get(1));

        testUtil.enableNormalUserTryUpdatingOtherUserDataMode(loggedInUserHelper, user1);

        UserUpdateDto userUpdateDto=UserUpdateDto.builder().username(user2.getUsername())
                .firstname(user2.getFirstname())
                .lastname(user2.getLastname())
                .email("changed@email.test")
                .build();

        exceptionRule.expect(InvalidUserException.class);
        exceptionRule.expectMessage("Invalid user name");

        User updatedUser = userService.update(userUpdateDto);

    }

    @Test
    public void adminUserShouldBeAbleToUpdateOtherUserDetails()
    {
        List<UserDto> userDtos=testUtil.generateUserDtos(2);
        User user1=userService.save(userDtos.get(0));
        User user2=userService.save(userDtos.get(1));

        testUtil.enableAdminMode(loggedInUserHelper);

        UserUpdateDto userUpdateDto=UserUpdateDto.builder().username(user1.getUsername())
                .firstname(user1.getFirstname())
                .lastname(user1.getLastname())
                .email("changed@email.test1")
                .build();

        User updatedUser = userService.update(userUpdateDto);
        assertEquals("changed@email.test1", updatedUser.getEmail());

        userUpdateDto=UserUpdateDto.builder().username(user2.getUsername())
                .firstname(user2.getFirstname())
                .lastname(user2.getLastname())
                .email("changed@email.test2")
                .build();

        updatedUser = userService.update(userUpdateDto);
        assertEquals("changed@email.test2", updatedUser.getEmail());

    }

    @Test
    public void normalUserShouldBeAbleToDeleteOwnDetails()
    {
        User user = userService.save(
                testUtil.generateUserDtos(1).get(0)
        );
        testUtil.enableNormalUserMode(loggedInUserHelper, user);

        String userDeleteStatus = userService.delete(user.getUsername());

        assertEquals("DELETE_SUCCESS", userDeleteStatus);
    }

    @Test
    public void normalUserIfTryToDeleteOtherUserDetailsShouldFail()
    {
        List<UserDto> userDtos=testUtil.generateUserDtos(2);
        User user1=userService.save(userDtos.get(0));
        User user2=userService.save(userDtos.get(1));

        testUtil.enableNormalUserTryUpdatingOtherUserDataMode(loggedInUserHelper, user1);

        exceptionRule.expect(InvalidUserException.class);
        exceptionRule.expectMessage("Invalid user name");

        String userDeleteStatus = userService.delete(user2.getUsername());

    }

    @Test
    public void adminUserShouldBeAbleToDeleteOtherUserDetails()
    {
        List<UserDto> userDtos=testUtil.generateUserDtos(2);
        User user1=userService.save(userDtos.get(0));
        User user2=userService.save(userDtos.get(1));

        testUtil.enableAdminMode(loggedInUserHelper);

        String userDeleteStatus = userService.delete(user1.getUsername());
        assertEquals("DELETE_SUCCESS", userDeleteStatus);
        userDeleteStatus = userService.delete(user2.getUsername());
        assertEquals("DELETE_SUCCESS", userDeleteStatus);


    }


}
