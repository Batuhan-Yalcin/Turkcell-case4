package com.turkcellcase4.user.mapper;

import com.turkcellcase4.user.dto.UserResponseDTO;
import com.turkcellcase4.user.dto.UserListDTO;
import com.turkcellcase4.user.model.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserResponseDTO toUserResponseDTO(User user);
    
    List<UserResponseDTO> toUserResponseDTOList(List<User> users);
    
    default UserListDTO toUserListDTO(List<User> users) {
        return UserListDTO.builder()
                .users(toUserResponseDTOList(users))
                .build();
    }
}
