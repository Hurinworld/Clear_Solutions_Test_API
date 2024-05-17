package com.serhiihurin.clearsolutionsapi.dao;

import com.serhiihurin.clearsolutionsapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserRepository extends JpaRepository <User, String> {
    List<User> getUsersByBirthDateBetween(LocalDate fromDate, LocalDate toDate);
}
