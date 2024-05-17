package com.serhiihurin.clearsolutionsapi.service;

import com.serhiihurin.clearsolutionsapi.dao.UserRepository;
import com.serhiihurin.clearsolutionsapi.dto.UserRequestDTO;
import com.serhiihurin.clearsolutionsapi.entity.User;
import com.serhiihurin.clearsolutionsapi.exception.ApiRequestException;
import com.serhiihurin.clearsolutionsapi.exception.UnsatisfiedAgeException;
import com.serhiihurin.clearsolutionsapi.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    @Value("${custom.minimal-allowed-age}")
    private int MINIMAL_ALLOWED_AGE;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";
    private final String PHONE_NUMBER_PATTERN = "\\+\\d{10,}";

    @Override
    public List<User> searchUsersByBirthDate(String searchFrom, String searchTo) {
        if(!searchFrom.matches(DATE_PATTERN) || !searchTo.matches(DATE_PATTERN)) {
            throw new ApiRequestException("Invalid date format. The format should be yyyy-MM-dd");
        }
        LocalDate fromDate = LocalDate.parse(searchFrom, formatter);
        LocalDate toDate = LocalDate.parse(searchTo, formatter);
        if (!fromDate.isAfter(toDate)) {
            return userRepository.getUsersByBirthDateBetween(fromDate, toDate);
        } else {
            throw new ApiRequestException("Invalid request parameters of date");
        }
    }

    @Override
    public User getUser(String email) {
        return userRepository.findById(email)
                .orElseThrow(() -> new ApiRequestException("Could not find user with email: " + email));
    }

    @Override
    public User createUser(UserRequestDTO userRequestDTO) {
        if (userRequestDTO.getPhoneNumber() != null && !userRequestDTO.getPhoneNumber().isBlank()
                && !userRequestDTO.getPhoneNumber().matches(PHONE_NUMBER_PATTERN)) {
            throw new ApiRequestException("Invalid phone number");
        }
        if (!userRequestDTO.getBirthDate().matches(DATE_PATTERN)) {
            throw new ApiRequestException("Invalid date format. The format should be yyyy-MM-dd");
        }
        LocalDate birthDate = LocalDate.parse(userRequestDTO.getBirthDate(), formatter);
        LocalDate currentDate = LocalDate.now();
        int userAge = Period.between(birthDate, currentDate).getYears();

        if (userAge < 0) {
            throw new UnsatisfiedAgeException("birth date cannot be later than current date");
        } else if (userAge < MINIMAL_ALLOWED_AGE) {
            throw new UnsatisfiedAgeException("User age is less than 18");
        } else {
            return userRepository.save(
                    User.builder()
                            .email(userRequestDTO.getEmail())
                            .firstName(userRequestDTO.getFirstName())
                            .lastName(userRequestDTO.getLastName())
                            .birthDate(birthDate)
                            .address(userRequestDTO.getAddress())
                            .phoneNumber(userRequestDTO.getPhoneNumber())
                            .build()
            );
        }
    }

    @Override
    public User updateUserInfo(String email, UserRequestDTO userRequestDTO) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new ApiRequestException("Could not find user with email: " + email));
        if (userRequestDTO.getFirstName() != null) {
            user.setFirstName(userRequestDTO.getFirstName());
        }
        if (userRequestDTO.getLastName() != null) {
            user.setLastName(userRequestDTO.getLastName());
        }
        if (userRequestDTO.getBirthDate() != null) {
            if (!userRequestDTO.getBirthDate().matches(DATE_PATTERN)) {
                throw new ApiRequestException("Invalid date format. The format should be yyyy-MM-dd");
            }
            user.setBirthDate(LocalDate.parse(userRequestDTO.getBirthDate(), formatter));
        }
        if (userRequestDTO.getAddress() != null) {
            user.setAddress(userRequestDTO.getAddress());
        }
        if (userRequestDTO.getPhoneNumber() != null) {
            if (!userRequestDTO.getPhoneNumber().isBlank()
                    && !userRequestDTO.getPhoneNumber().matches(PHONE_NUMBER_PATTERN)) {
                throw new ApiRequestException("Invalid phone number");
            }
            user.setPhoneNumber(userRequestDTO.getPhoneNumber());
        }
        if (userRequestDTO.getEmail() != null && !userRequestDTO.getEmail().isBlank()) {
            userRepository.deleteById(email);
            user.setEmail(userRequestDTO.getEmail());
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(String email) {
        User userForDeletion = userRepository.findById(email)
                .orElseThrow(() -> new ApiRequestException("Could not find user with email: " + email));
        userRepository.delete(userForDeletion);
    }
}
