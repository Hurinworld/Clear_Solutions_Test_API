package com.serhiihurin.clearsolutionsapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRequestDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String birthDate;
    private String address;
    private String phoneNumber;
}
