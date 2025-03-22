package com.example.user_product_api.service;

import com.example.user_product_api.dto.PagedResponse;
import com.example.user_product_api.dto.user.UserCreateDto;
import com.example.user_product_api.dto.user.UserDto;
import com.example.user_product_api.dto.user.UserUpdateDto;
import com.example.user_product_api.entity.Role;
import com.example.user_product_api.entity.User;
import com.example.user_product_api.exception.ResourceNotFoundException;
import com.example.user_product_api.repository.RoleRepository;
import com.example.user_product_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAllActiveUsers(search != null ? search : "", pageable);

        List<UserDto> userDtos = userPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                userDtos,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return mapToDto(user);
    }

    @Transactional
    public UserDto createUser(UserCreateDto userCreateDto) {
        // Encode password
        User user = new User();
        user.setName(userCreateDto.getName());
        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));

        // Set roles
        Set<Role> roles = new HashSet<>();
        if (userCreateDto.getRoles() == null || userCreateDto.getRoles().isEmpty()) {
            roleRepository.findByName("ROLE_USER")
                    .ifPresent(roles::add);
        } else {
            userCreateDto.getRoles().forEach(roleName -> {
                roleRepository.findByName(roleName)
                        .ifPresent(roles::add);
            });

            // If no valid roles provided, assign USER role
            if (roles.isEmpty()) {
                roleRepository.findByName("ROLE_USER")
                        .ifPresent(roles::add);
            }
        }

        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setName(userUpdateDto.getName());
        user.setEmail(userUpdateDto.getEmail());

        // Update roles if provided
        if (userUpdateDto.getRoles() != null && !userUpdateDto.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            userUpdateDto.getRoles().forEach(roleName -> {
                roleRepository.findByName(roleName)
                        .ifPresent(roles::add);
            });

            // Only update if at least one valid role was found
            if (!roles.isEmpty()) {
                user.setRoles(roles);
            }
        }

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Implement soft delete
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private UserDto mapToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        userDto.setRoles(roleNames);
        return userDto;
    }
}
