package com.legends.promiscuous.dtos.requests;

import com.legends.promiscuous.enums.Interest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;

@Setter
@Getter
public class UpdateUserRequest {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String password;
    private String gender;
    private Set<String> interests;
    private MultipartFile profileImage;
    private String phoneNumber;
    private String street;
    private String houseNumber;
    private String state;
    private String country;
}
