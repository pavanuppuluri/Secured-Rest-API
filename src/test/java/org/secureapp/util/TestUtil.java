package org.secureapp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.secureapp.dto.UserDto;
import org.secureapp.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Component
public class TestUtil {


    public List<User> generateUsers(int numberOfUsers)
    {
        List<User> users=new ArrayList<>(numberOfUsers);

        for(int i=0;i<numberOfUsers;i++) {
            users.add(User.builder()
                    .username("username_" + i)
                    .password("$2a$10$pucWsSNzuh1.2LgddoCHkeZhT19R5Ku3b2GK5Aw6XDMwd665vsrRi")
                    .email("test_usr@tz.com"+i)
                    .firstname("firstname"+i)
                    .lastname("lastname"+i)
                    .build()
            );
        }

        return users;

    }

    public List<UserDto> generateUserDtos(int numberOfUsers)
    {
        List<UserDto> userDtos=new ArrayList<>(numberOfUsers);

        for(int i=0;i<numberOfUsers;i++) {
            userDtos.add(UserDto.builder()
                    .username("username_" + i)
                    .password("$2a$10$pucWsSNzuh1.2LgddoCHkeZhT19R5Ku3b2GK5Aw6XDMwd665vsrRi")
                    .email("test_usr@tz.com"+i)
                    .firstname("firstname"+i)
                    .lastname("lastname"+i)
                    .build()
            );
        }

        return userDtos;

    }




    public void enableNormalUserMode(LoggedInUserHelper loggedInUserHelper, User user)
    {
        when(loggedInUserHelper.getCurrentLoggedInUserID()).thenReturn(user.getId());
        when(loggedInUserHelper.getCurrentLoggedInUserName()).thenReturn(user.getUsername());
        when(loggedInUserHelper.checkCurrentUserEqualUserNameParam(any())).thenReturn(true);
        when(loggedInUserHelper.isCurrentLoggedInUserAdmin()).thenReturn(false);
    }

    public void enableNormalUserTryUpdatingOtherUserDataMode(LoggedInUserHelper loggedInUserHelper, User user)
    {
        when(loggedInUserHelper.getCurrentLoggedInUserID()).thenReturn(user.getId());
        when(loggedInUserHelper.getCurrentLoggedInUserName()).thenReturn(user.getUsername());
        when(loggedInUserHelper.checkCurrentUserEqualUserNameParam(any())).thenReturn(false);
        when(loggedInUserHelper.isCurrentLoggedInUserAdmin()).thenReturn(false);
    }

    public void enableAdminMode(LoggedInUserHelper loggedInUserHelper)
    {
        when(loggedInUserHelper.isCurrentLoggedInUserAdmin()).thenReturn(true);
    }

    public String mapToJson(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

}
