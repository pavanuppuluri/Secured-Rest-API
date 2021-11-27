package org.secureapp.service;

import org.secureapp.dto.UserDto;
import org.secureapp.dto.UserUpdateDto;
import org.secureapp.model.User;

import java.util.List;

public interface UserService {
    User save(UserDto user);

    User update(UserUpdateDto user);

    User find(String userName);

    String delete(String userName);

    List<User> findAllUsers();


}
