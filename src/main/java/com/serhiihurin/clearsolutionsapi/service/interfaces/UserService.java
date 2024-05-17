package com.serhiihurin.clearsolutionsapi.service.interfaces;

import com.serhiihurin.clearsolutionsapi.dto.UserRequestDTO;
import com.serhiihurin.clearsolutionsapi.entity.User;

import java.util.List;

public interface UserService {
    List<User> searchUsersByBirthDate(String fromDate, String toDate);
    User getUser(String email);
    User createUser(UserRequestDTO userRequestDTO);
    User updateUserInfo(String email, UserRequestDTO userRequestDTO);
    void deleteUser(String email);
}
