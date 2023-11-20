package com.example.rest.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rest.entity.Activity;
import com.example.rest.entity.Profile;
import com.example.rest.entity.Role;
import com.example.rest.entity.User;
import com.example.rest.request.UserRequest;
import com.example.rest.response.TokenResponse;
import com.example.rest.service.JwtService;
import com.example.rest.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/token")
public class TokenController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/registration")
    public ResponseEntity<?> token(UserRequest userRequest, HttpServletRequest request) {
        if (this.userService.find(userRequest.getEmail()) != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User user = new User(
            userRequest.getEmail(),         // email
            userRequest.getPassword()       // password
        );

        List<Role> roles = Collections.emptyList();
        user.setRoles(roles);               // roles

        Profile profile = new Profile("", "", true, user);
        user.setProfile(profile);           // profile

        Activity activity = new Activity(request.getRemoteAddr(), LocalDateTime.now(), LocalDateTime.now(), null, user);
        user.setActivity(activity);         // activity

        final User storeUser = this.userService.save(user);

        TokenResponse tokenResponse = new TokenResponse();

        tokenResponse.setEmail(storeUser.getEmail());
        tokenResponse.setToken(
            this.jwtService.create(storeUser.getEmail(), storeUser.getPassword())
        );

        return new ResponseEntity<>(tokenResponse, HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(UserRequest userRequest, HttpServletRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    userRequest.getEmail(), userRequest.getPassword()
                )
            );
        } catch (BadCredentialsException exception) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        TokenResponse tokenResponse = new TokenResponse();

        tokenResponse.setEmail(userRequest.getEmail());
        tokenResponse.setToken(
            this.jwtService.create(userRequest.getEmail(), this.passwordEncoder.encode(userRequest.getPassword()))
        );

        return new ResponseEntity<>(tokenResponse, HttpStatus.CREATED);
    }
}
