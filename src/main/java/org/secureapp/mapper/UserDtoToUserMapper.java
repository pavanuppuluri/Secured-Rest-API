package org.secureapp.mapper;

import org.secureapp.dto.UserDto;
import org.secureapp.model.Role;
import org.secureapp.model.User;
import org.secureapp.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UserDtoToUserMapper {

    @Autowired
    RoleService roleService;
    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    public User mapUserDtoToUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(bcryptEncoder.encode(userDto.getPassword()));
        user.setFirstname(userDto.getFirstname());
        user.setLastname(userDto.getLastname());
        user.setEmail(userDto.getEmail());

        /* For all the new users, assign USER role */
        Role role = roleService.findByName("ROLE_USER");
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(role);

        user.setRoles(roleSet);

        return user;
    }
}
