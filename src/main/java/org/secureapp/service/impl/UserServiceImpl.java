package org.secureapp.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.secureapp.dto.UserDto;
import org.secureapp.dto.UserUpdateDto;
import org.secureapp.exception.RecordAlreadyExistsException;
import org.secureapp.mapper.UserDtoToUserMapper;
import org.secureapp.model.User;
import org.secureapp.repository.UserRepository;
import org.secureapp.service.UserService;
import org.secureapp.util.LoggedInUserHelper;
import org.secureapp.validation.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserDtoToUserMapper userDtoToUserMapper;

    @Autowired
    LoggedInUserHelper loggedInUserHelper;

    @Autowired
    ValidationHelper validationHelper;

    @Override
    public User find(String userName) {
        log.info("In find {}", userName);
        User user = null;
        if (isCurrentLoggedInUserAdmin()) {
            user = userRepository.findByUsernameIgnoreCase(userName);
        }
        if (!isCurrentLoggedInUserAdmin() && checkCurrentUserEqualUserNameParam(userName)) {
            user = userRepository.findByUsernameIgnoreCase(loggedInUserHelper.getCurrentLoggedInUserName());
        }

        if (user == null) {
            log.warn("Invalid user");
            validationHelper.throwInvalidUserException();
        }

        return user;
    }

    @Override
    public String delete(String userName) {
        log.info("In delete {}", userName);
        User user = null;
        if (isCurrentLoggedInUserAdmin()) {
            user = userRepository.findByUsernameIgnoreCase(userName);
        }
        if (!isCurrentLoggedInUserAdmin() && checkCurrentUserEqualUserNameParam(userName)) {
            user = userRepository.findByUsernameIgnoreCase(loggedInUserHelper.getCurrentLoggedInUserName());
        }
        if (user == null) {
            log.warn("Invalid user");
            validationHelper.throwInvalidUserException();
        }

        userRepository.delete(user);
        return "DELETE_SUCCESS";

    }

    @Override
    public List<User> findAllUsers() {
        log.info("In findAllUsers");
        List<User> userList = (List<User>) userRepository.findAll();
        return userList;
    }

    @Override
    public User save(UserDto userDto) {
        log.info("In save");
        // Check if any user with same name/email exists
        List<User> existingUsers = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(
                userDto.getUsername(),
                userDto.getEmail()
        );

        if (existingUsers.size() > 0) {
            log.warn("User with the given username/email already exists");
            throw new RecordAlreadyExistsException("User with the given username/email already exists");
        }

        User user = userDtoToUserMapper.mapUserDtoToUser(userDto);
        return userRepository.save(user);
    }

    @Override
    public User update(UserUpdateDto userDto) {
        log.info("In update");
        User user = null;
        if (isCurrentLoggedInUserAdmin()) {
            user = userRepository.findByUsernameIgnoreCase(userDto.getUsername());
        }
        if (!isCurrentLoggedInUserAdmin() && checkCurrentUserEqualUserNameParam(userDto.getUsername())) {
            user = userRepository.findByUsernameIgnoreCase(loggedInUserHelper.getCurrentLoggedInUserName());
        }

        if (user == null) {
            log.warn("Invalid user");
            validationHelper.throwInvalidUserException();
        }

        /* check if email already exists for any other user */
        User anotherUserWithSameEmailExists = userRepository
                .findByUsernameNotIgnoreCaseAndEmailIgnoreCase(
                        userDto.getUsername(),
                        userDto.getEmail()
                );

        if (anotherUserWithSameEmailExists != null) {
            log.warn("User with the given username/email already exists");
            throw new RecordAlreadyExistsException("User with same email already exists");
        }

        user.setFirstname(userDto.getFirstname());
        user.setLastname(userDto.getLastname());
        user.setEmail(userDto.getEmail());
        return userRepository.save(user);
    }

    public boolean isCurrentLoggedInUserAdmin() {
        return loggedInUserHelper.isCurrentLoggedInUserAdmin();
    }

    public boolean checkCurrentUserEqualUserNameParam(String userName) {
        return loggedInUserHelper.checkCurrentUserEqualUserNameParam(userName);
    }

}
