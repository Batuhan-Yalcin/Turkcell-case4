package com.turkcellcase4.user.service;

import com.turkcellcase4.user.dto.UserResponseDTO;
import com.turkcellcase4.user.dto.UserListDTO;
import com.turkcellcase4.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    
    UserResponseDTO getUserById(Long userId);
    
    UserListDTO getAllUsers();
    
    UserResponseDTO getUserByMsisdn(String msisdn);
    
    List<UserResponseDTO> getUsersByType(String userType);
    
    UserResponseDTO createUser(User user);
    
    UserResponseDTO updateUser(Long userId, User user);
    
    void deleteUser(Long userId);
}
