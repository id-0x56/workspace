package com.example.rest.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.rest.entity.Activity;
import com.example.rest.entity.Profile;
import com.example.rest.entity.Role;
import com.example.rest.entity.User;
import com.example.rest.exception.AlreadyExistException;
import com.example.rest.exception.NotFoundException;
import com.example.rest.repository.RoleRepository;
import com.example.rest.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpServletRequest request;

    @Cacheable(value = "users")
    @Override
    public List<User> all() {
        List<User> users = new ArrayList<>();
        this.userRepository.findAll().forEach(users::add);

        if (users.isEmpty()) {
            throw new NotFoundException("Users not found");
        }

        return users;
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    @Override
    public User save(User user) {
        if (this.userRepository.existsByEmail(user.getEmail())) {
            throw new AlreadyExistException("User with email: \"" + user.getEmail() + "\" already exists");
        }

        user.setPassword(this.passwordEncoder.encode(user.getPassword()));

        Profile profile = new Profile(
            user.getProfile().getFirstName(),
            user.getProfile().getLastName(),
            user.getProfile().isVerify(),
            user
        );
        user.setProfile(profile);

        Set<Role> roles = new HashSet<>();
        Role defaultRole = this.roleRepository.findByName("ROLE_USER").get();
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            roles.add(defaultRole);
        } else {
            roles = user.getRoles().stream().map(
                role -> this.roleRepository
                    .findByName(role.getName())
                    .orElse(defaultRole)
            ).collect(Collectors.toSet());
        }
        user.setRoles(roles);

        Activity activity = new Activity(
            this.request.getRemoteAddr(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            user
        );
        user.setActivity(activity);

        return this.userRepository.save(user);
    }

    @Cacheable(value = "user", key = "#id")
    @Override
    public User find(Long id) {
        User user = this.userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User with id: \"" + id + "\" not found"));

        return user;
    }

    @Override
    public User find(String email) {
        User user = this.userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User with email: \"" + email + "\" not found"));

        return user;
    }

    @Caching(
        evict = {
            @CacheEvict(value = "user", key = "#id"),
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional
    @Override
    public User update(Long id, User user) {
        User updateUser = this.userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User with id: \"" + id + "\" not found"));

        updateUser.setEmail(user.getEmail());
        updateUser.setPassword(this.passwordEncoder.encode(user.getPassword()));

        Profile profile = updateUser.getProfile();
        profile.setFirstName(user.getProfile().getFirstName());
        profile.setLastName(user.getProfile().getLastName());
        profile.setVerify(user.getProfile().isVerify());

        Set<Role> roles = new HashSet<>();
        Role defaultRole = this.roleRepository.findByName("ROLE_USER").get();
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            roles.add(defaultRole);
        } else {
            roles = user.getRoles().stream().map(
                role -> this.roleRepository
                    .findByName(role.getName())
                    .orElse(defaultRole)
            ).collect(Collectors.toSet());
        }
        updateUser.setRoles(roles);

        Activity activity = updateUser.getActivity();
        activity.setLastLoginIP(this.request.getRemoteAddr());
        activity.setLastLoginAt(LocalDateTime.now());

        activity.setCreatedAt(activity.getCreatedAt());
        activity.setUpdatedAt(LocalDateTime.now());

        return this.userRepository.save(updateUser);
    }

    @Caching(
        evict = {
            @CacheEvict(value = "user", key = "#id"),
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Override
    public void delete(Long id) {
        if (this.userRepository.existsById(id)) {
            this.userRepository.deleteById(id);
        } else {
            throw new NotFoundException("User with id: \"" + id + "\" not found");
        }
    }

    @Override
    public User current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null && authentication instanceof AnonymousAuthenticationToken && !authentication.isAuthenticated()) {
            throw new NotFoundException("Authenticated user not found");
        }

        User user = this.userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        return user;
    }
}
