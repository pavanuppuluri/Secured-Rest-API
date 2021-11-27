package org.secureapp.validation;

import org.secureapp.exception.InvalidUserException;
import org.secureapp.exception.RecordNotFoundException;
import org.secureapp.util.LoggedInUserHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidationHelper {

    @Autowired
    LoggedInUserHelper loggedInUserHelper;

    public void throwInvalidUserException() {
        if (loggedInUserHelper.isCurrentLoggedInUserAdmin())
            throw new RecordNotFoundException("User not found");

        // For ROLE_USER scenario
        throw new InvalidUserException("Invalid user name");
    }

}
