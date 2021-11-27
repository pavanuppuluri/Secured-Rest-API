package org.secureapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.secureapp.config.JwtTokenProvider;
import org.secureapp.dto.UserDto;
import org.secureapp.dto.UserUpdateDto;
import org.secureapp.model.AuthToken;
import org.secureapp.model.LoginUser;
import org.secureapp.model.User;
import org.secureapp.service.UserService;
import org.secureapp.util.LoggedInUserHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/user")
@Validated
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    LoggedInUserHelper loggedInUserHelper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenUtil;

    @PostMapping(value = "/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginUser loginUser) throws AuthenticationException {

        log.info("In loginUser");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUser.getUsername(),
                        loginUser.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String token = jwtTokenUtil.generateToken(authentication);
        log.info("loginUser complete");
        return ResponseEntity.ok(new AuthToken(token));
    }

    @PostMapping(value = "/register")
    public User registerUser(@Valid @RequestBody UserDto userDto) {
        log.info("In registerUser");
        return userService.save(userDto);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/update")
    public User updateUser(@Valid @RequestBody UserUpdateDto userDto) {
        log.info("In updateUser");
        return userService.update(userDto);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/getuser")
    public User getUser(@NotBlank @RequestParam String userName) {
        log.info("In getUser {}", userName);
        return userService.find(userName);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getallusers")
    public List<User> getAllUsers() {
        log.info("In getAllUsers");
        return userService.findAllUsers();
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/delete")
    public String deleteUser(
            @RequestHeader("Authorization") String authToken,
            @NotBlank @RequestParam String userName
    ) {
        log.info("In deleteUser {}", userName);
        String deleteUser = userService.delete(userName);
        jwtTokenUtil.blackListTokenOnDelete(authToken, userName);
        return deleteUser;
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/logout")
    public String logout(@RequestHeader("Authorization") String authToken) {
        log.info("In logout");
        jwtTokenUtil.blackListTokenOnLogout(authToken);
        return "LOGOUT_SUCCESS";
    }

}
