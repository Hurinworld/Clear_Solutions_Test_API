package com.serhiihurin.clearsolutionsapi.service;

import com.serhiihurin.clearsolutionsapi.dao.UserRepository;
import com.serhiihurin.clearsolutionsapi.dto.UserRequestDTO;
import com.serhiihurin.clearsolutionsapi.entity.User;
import com.serhiihurin.clearsolutionsapi.exception.ApiRequestException;
import com.serhiihurin.clearsolutionsapi.exception.UnsatisfiedAgeException;
import com.serhiihurin.clearsolutionsapi.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    private UserService userService;
    private User testUser;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
        ReflectionTestUtils.setField(userService, "MINIMAL_ALLOWED_AGE", 18);
        testUser = User.builder()
                .email("testemail.gmail.com")
                .firstName("testfirstname")
                .lastName("testlastname")
                .birthDate(LocalDate.parse("2003-10-13", formatter))
                .address("Test address street 10")
                .phoneNumber("+380123456789")
                .build();
    }

    @Test
    public void searchUsersByBirthDate_InvalidFromDateFormat_ThrowsException() {
        String searchFrom = "01-012000";
        String searchTo = "2000-12-31";

        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.searchUsersByBirthDate(searchFrom, searchTo));

        assertEquals("Invalid date format. The format should be yyyy-MM-dd", exception.getMessage());
    }

    @Test
    public void searchUsersByBirthDate_InvalidToDateFormat_ThrowsException() {
        String searchFrom = "2000-01-01";
        String searchTo = "31-2000-12";

        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.searchUsersByBirthDate(searchFrom, searchTo));

        assertEquals("Invalid date format. The format should be yyyy-MM-dd", exception.getMessage());
    }

    @Test
    public void searchUsersByBirthDate_FromDateAfterToDate_ThrowsException() {
        String searchFrom = "2000-12-31";
        String searchTo = "2000-01-01";

        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.searchUsersByBirthDate(searchFrom, searchTo));

        assertEquals("Invalid request parameters of date", exception.getMessage());
    }

    @Test
    void searchUsersByBirthDate() {
        String searchFrom = "2003-01-01";
        String searchTo = "2003-12-31";

        LocalDate fromDate = LocalDate.parse(searchFrom, formatter);
        LocalDate toDate = LocalDate.parse(searchTo, formatter);

        List<User> expectedUsers = Collections.singletonList(testUser);
        Mockito.when(userRepository.getUsersByBirthDateBetween(fromDate, toDate)).thenReturn(expectedUsers);

        List<User> result = userService.searchUsersByBirthDate(searchFrom, searchTo);

        assertNotNull(result);
        assertEquals(1, result.size());
        Mockito.verify(userRepository, Mockito.times(1)).getUsersByBirthDateBetween(fromDate, toDate);
    }

    @Test
    void getUser() {
        Mockito.when(userRepository.findById(testUser.getEmail())).thenReturn(Optional.of(testUser));
        User capturedValue = userService.getUser(testUser.getEmail());

        assertThat(capturedValue)
                .usingRecursiveComparison()
                .isEqualTo(testUser);
    }

    @Test
    public void createUser_WithInvalidPhoneNumber_ThrowsException() {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .phoneNumber("12345")
                .birthDate("2000-01-01")
                .build();

        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.createUser(userRequestDTO));

        assertEquals("Invalid phone number", exception.getMessage());
    }

    @Test
    public void testCreateUser_WithInvalidDateFormat_ThrowsException() {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .phoneNumber("+12345678901")
                .birthDate("01-012000")
                .build();

        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.createUser(userRequestDTO));

        assertEquals("Invalid date format. The format should be yyyy-MM-dd", exception.getMessage());
    }

    @Test
    public void createUser_BirthDateLaterThanCurrentDate_ThrowsException() {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .phoneNumber("+12345678901")
                .birthDate(LocalDate.now().plusYears(1).format(formatter))
                .build();

        UnsatisfiedAgeException exception = assertThrows(UnsatisfiedAgeException.class,
                () -> userService.createUser(userRequestDTO));

        assertEquals("birth date cannot be later than current date", exception.getMessage());
    }

    @Test
    public void createUser_UserUnderMinimalAllowedAge_ThrowsException() {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .phoneNumber("+12345678901")
                .birthDate("2007-12-10")
                .build();

        UnsatisfiedAgeException exception = assertThrows(UnsatisfiedAgeException.class,
                () -> userService.createUser(userRequestDTO));

        assertEquals("User age is less than 18", exception.getMessage());
    }

    @Test
    void createUser_Success() {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .email("testemail.gmail.com")
                .firstName("testfirstname")
                .lastName("testlastname")
                .birthDate("2003-10-13")
                .address("Test address street 10")
                .phoneNumber("+380123456789")
                .build();

        userService.createUser(userRequestDTO);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(userArgumentCaptor.capture());
        User capturedValue = userArgumentCaptor.getValue();
        assertThat(capturedValue)
                .usingRecursiveComparison()
                .isEqualTo(testUser);
    }

    @Test
    public void updateUserInfo_UserNotFound_ThrowsException() {
        String email = "nonexistent@example.com";
        UserRequestDTO userRequestDTO = UserRequestDTO.builder().build();

        Mockito.when(userRepository.findById(email)).thenReturn(Optional.empty());

        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.updateUserInfo(email, userRequestDTO));

        assertEquals("Could not find user with email: " + email, exception.getMessage());
    }

    @Test
    public void updateUserInfo_InvalidDateFormat_ThrowsException() {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .birthDate("invalid-date")
                .build();

        Mockito.when(userRepository.findById(testUser.getEmail())).thenReturn(Optional.of(testUser));

        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.updateUserInfo(testUser.getEmail(), userRequestDTO));

        assertEquals("Invalid date format. The format should be yyyy-MM-dd", exception.getMessage());
    }

    @Test
    public void updateUserInfo_InvalidPhoneNumber_ThrowsException() {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .phoneNumber("invalid-phone")
                .build();
        userRequestDTO.setPhoneNumber("invalid-phone");

        Mockito.when(userRepository.findById(testUser.getEmail())).thenReturn(Optional.of(testUser));

        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.updateUserInfo(testUser.getEmail(), userRequestDTO));

        assertEquals("Invalid phone number", exception.getMessage());
    }

    @Test
    void updateUserInfo() {
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .birthDate("2000-01-01")
                .address("123 Street")
                .phoneNumber("+1234567890")
                .build();

        Mockito.when(userRepository.findById(testUser.getEmail())).thenReturn(Optional.of(testUser));
        Mockito.when(userRepository.save(testUser)).thenReturn(testUser);

        User updatedUser = userService.updateUserInfo(testUser.getEmail(), userRequestDTO);

        assertNotNull(updatedUser);
        assertEquals("John", updatedUser.getFirstName());
        assertEquals("Doe", updatedUser.getLastName());
        assertEquals(LocalDate.parse("2000-01-01", formatter), updatedUser.getBirthDate());
        assertEquals("123 Street", updatedUser.getAddress());
        assertEquals("+1234567890", updatedUser.getPhoneNumber());
        Mockito.verify(userRepository, Mockito.times(1)).findById(testUser.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
    }

    @Test
    public void updateUserInfo_EmailChange() {
        String newEmail = "new@example.com";
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .email(newEmail)
                .build();
        Mockito.when(userRepository.findById(testUser.getEmail())).thenReturn(Optional.of(testUser));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateUserInfo(testUser.getEmail(), userRequestDTO);

        assertNotNull(updatedUser);
        assertEquals(newEmail, updatedUser.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    public void deleteUser_UserNotFound_ThrowsException() {
        String email = "nonexistent@example.com";

        Mockito.when(userRepository.findById(email)).thenReturn(Optional.empty());

        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.deleteUser(email));

        assertEquals("Could not find user with email: " + email, exception.getMessage());
        Mockito.verify(userRepository, Mockito.never()).delete(Mockito.any(User.class));
    }

    @Test
    void deleteUser() {
        Mockito.when(userRepository.findById(testUser.getEmail())).thenReturn(Optional.of(testUser));

        userService.deleteUser(testUser.getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).findById(testUser.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).delete(testUser);
    }
}