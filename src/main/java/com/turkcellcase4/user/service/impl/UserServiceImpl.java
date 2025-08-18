package com.turkcellcase4.user.service.impl;

import com.turkcellcase4.common.enums.UserType;
import com.turkcellcase4.user.dto.UserResponseDTO;
import com.turkcellcase4.user.dto.UserListDTO;
import com.turkcellcase4.user.mapper.UserMapper;
import com.turkcellcase4.user.model.User;
import com.turkcellcase4.user.repository.UserRepository;
import com.turkcellcase4.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.turkcellcase4.common.exception.BusinessLogicException;
import com.turkcellcase4.common.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userId));
        return userMapper.toUserResponseDTO(user);
    }

    @Override
    public UserListDTO getAllUsers() {
        log.info("Getting all users");
        List<User> users = userRepository.findAll();
        return userMapper.toUserListDTO(users);
    }

    @Override
    public UserResponseDTO getUserByMsisdn(String msisdn) {
        log.info("Getting user by MSISDN: {}", msisdn);
        User user = userRepository.findByMsisdn(msisdn)
                .orElseThrow(() -> new ResourceNotFoundException("MSISDN ile kullanıcı bulunamadı: " + msisdn));
        return userMapper.toUserResponseDTO(user);
    }

    @Override
    public List<UserResponseDTO> getUsersByType(String userType) {
        log.info("Getting users by type: {}", userType);
        UserType type = UserType.valueOf(userType.toUpperCase());
        List<User> users = userRepository.findByType(type);
        return userMapper.toUserResponseDTOList(users);
    }

    @Override
    public UserResponseDTO createUser(User user) {
        log.info("Creating new user: {}", user.getName());
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO updateUser(Long userId, User user) {
        log.info("Updating user with ID: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek kullanıcı bulunamadı: " + userId));
        
        try {
            existingUser.setName(user.getName());
            existingUser.setCurrentPlanId(user.getCurrentPlanId());
            existingUser.setType(user.getType());
            existingUser.setMsisdn(user.getMsisdn());
            
            User updatedUser = userRepository.save(existingUser);
            return userMapper.toUserResponseDTO(updatedUser);
        } catch (Exception e) {
            throw new BusinessLogicException("Kullanıcı güncelleme hatası: " + e.getMessage());
        }
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Silinecek kullanıcı bulunamadı: " + userId);
        }
        try {
            userRepository.deleteById(userId);
        } catch (Exception e) {
            throw new BusinessLogicException("Kullanıcı silme hatası: " + e.getMessage());
        }
    }
}
