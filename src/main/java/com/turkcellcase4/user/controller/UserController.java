package com.turkcellcase4.user.controller;

import com.turkcellcase4.user.dto.UserResponseDTO;
import com.turkcellcase4.user.dto.UserListDTO;
import com.turkcellcase4.user.model.User;
import com.turkcellcase4.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        log.info("GET /users/{} - Getting user by ID", id);
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<UserListDTO> getAllUsers() {
        log.info("GET /users - Getting all users");
        UserListDTO users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/msisdn/{msisdn}")
    public ResponseEntity<UserResponseDTO> getUserByMsisdn(@PathVariable String msisdn) {
        log.info("GET /users/msisdn/{} - Getting user by MSISDN", msisdn);
        UserResponseDTO user = userService.getUserByMsisdn(msisdn);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/type/{userType}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByType(@PathVariable String userType) {
        log.info("GET /users/type/{} - Getting users by type", userType);
        List<UserResponseDTO> users = userService.getUsersByType(userType);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/bills")
    public ResponseEntity<String> getUserBills(@PathVariable Long id) {
        log.info("GET /users/{}/bills - Getting user bills (to be implemented)", id);
        return ResponseEntity.ok("User bills endpoint - to be implemented in billing module");
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody User user) {
        log.info("POST /users - Creating new user: {}", user.getName());
        UserResponseDTO createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("PUT /users/{} - Updating user", id);
        UserResponseDTO updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /users/{} - Deleting user", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
