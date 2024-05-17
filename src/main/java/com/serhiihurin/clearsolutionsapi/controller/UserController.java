package com.serhiihurin.clearsolutionsapi.controller;

import com.serhiihurin.clearsolutionsapi.dto.UserRequestDTO;
import com.serhiihurin.clearsolutionsapi.dto.UserResponseDTO;
import com.serhiihurin.clearsolutionsapi.entity.User;
import com.serhiihurin.clearsolutionsapi.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<UserResponseDTO> getUser(@RequestParam String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        modelMapper.map(
                                userService.getUser(email),
                                UserResponseDTO.class
                        )
                );
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers (
            @RequestParam String fromDate,
            @RequestParam String toDate
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        modelMapper.map(
                                userService.searchUsersByBirthDate(fromDate, toDate),
                                new TypeToken<List<UserResponseDTO>>(){
                                }.getType()
                        )
                );
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                modelMapper.map(
                        userService.createUser(userRequestDTO),
                        UserResponseDTO.class
                )
        );
    }

    @PatchMapping
    public ResponseEntity<User> updateUser(@RequestParam String email, @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userService.updateUserInfo(email, userRequestDTO));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestParam String email) {
        userService.deleteUser(email);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).build();
    }
}
