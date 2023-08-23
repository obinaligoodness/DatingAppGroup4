package com.legends.promiscuous.services;

import com.legends.promiscuous.dtos.requests.LoginRequest;
import com.legends.promiscuous.dtos.requests.RegisterUserRequest;
import com.legends.promiscuous.dtos.requests.UpdateUserRequest;
import com.legends.promiscuous.dtos.response.*;
import com.legends.promiscuous.exceptions.BadCredentialsException;
import com.legends.promiscuous.exceptions.PromiscuousBaseException;
import com.legends.promiscuous.exceptions.UserNotFoundException;
import com.legends.promiscuous.repositories.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/db/insert.sql"})
public class UserServiceTest {

    @Autowired
    private UserService userService;


    @Test
    public void testThatUserCanRegister(){
       RegisterUserRequest registerUserRequest = new RegisterUserRequest();
        registerUserRequest.setEmail("xogov34623@vreaa.com");
        registerUserRequest.setPassword("password");
        var registerUserResponse = userService.register(registerUserRequest);
        assertNotNull(registerUserResponse);
        assertNotNull(registerUserResponse.getMessage());
    }

    @Test
    public void testActivateUserAccount(){
        ApiResponse<?> activateUserAccountResponse =
                userService.activateUserAccount("abc1234.erhlvslkhsfl25r3");
        assertThat(activateUserAccountResponse).isNotNull();
    }

    @Test
    public void getUserByIdTest(){
        GetUserResponse response = userService.getUserById(500L);
        assertThat(response).isNotNull();
    }
    @Test
    public void getAllUsers(){
        List<GetUserResponse> users = userService.getAllUsers(1,5);
        assertThat(users).isNotNull();
        assertThat(users.size()).isEqualTo(5);
    }

    private void registerTestUsers() {
        RegisterUserRequest firstRequest = new RegisterUserRequest();
        firstRequest.setEmail("john@gmail.com");
        firstRequest.setPassword("password");
        userService.register(firstRequest);

        firstRequest.setEmail("jane@gmail.com");
        firstRequest.setPassword("password");
        userService.register(firstRequest);

        firstRequest.setEmail("jerry@gmail.com");
        firstRequest.setPassword("password");
        userService.register(firstRequest);

        firstRequest.setEmail("johnny@gmail.com");
        firstRequest.setPassword("password");
        userService.register(firstRequest);

        firstRequest.setEmail("jeoy@gmail.com");
        firstRequest.setPassword("password");
        userService.register(firstRequest);

        firstRequest.setEmail("zaza@gmail.com");
        firstRequest.setPassword("password");
        userService.register(firstRequest);

    }
    @Test
    public void testThatUsersCanLogin(){
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("password");

        LoginResponse loginResponse = userService.login(loginRequest);
        assertThat(loginResponse).isNotNull();
        String accessToken = loginResponse.getAccessToken();
        assertThat(loginResponse.getAccessToken()).isNotNull();
    }
    @Test
    public void testThatExceptionIsThrownWhenUserAuthenticateWithBadCredentials(){
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("wrongpassword");
        assertThatThrownBy(()->userService.login(loginRequest)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    public void testThatExceptionIsThrownWhenUserAuthenticateWithBadCredentialsEmail(){
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrongEmail.com");
        loginRequest.setPassword("password");
        assertThatThrownBy(()->userService.login(loginRequest)).isInstanceOf(UserNotFoundException.class);
    }
    @Test
    public void testThatUserCanUpdateAccount(){
        UpdateUserRequest updateUserRequest = buildUpdatRequest();
        UpdateUserResponse response = userService.updateProfile(updateUserRequest,500L);
        assertThat(response).isNotNull();
        GetUserResponse userResponse = userService.getUserById(500L);

        String fullname = userResponse.getFullName();
        String expectedFullName = new StringBuilder()
                .append(updateUserRequest.getFirstName())
                .append(" ")
                .append(updateUserRequest.getLastName())
                .toString();
        assertThat(fullname).isEqualTo(expectedFullName);
    }

    private UpdateUserRequest buildUpdatRequest(){
        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setId(500L);
        updateUserRequest.setDateOfBirth(LocalDate.of(2005, Month.NOVEMBER.ordinal(), 25));
        updateUserRequest.setFirstName("sheriff");
        updateUserRequest.setLastName("Awofiranye");
//        MultipartFile testImage = getTestImage();
//        updateUserRequest.setProfileImage(testImage);
        updateUserRequest.setCountry("Ghana");
        Set<String> interests = Set.of("swimming","sports","cooking");
        updateUserRequest.setInterests(interests);
        return updateUserRequest;
    }









    private MultipartFile getTestImage(){
        // obtaining a path that points to the image
        Path path = Paths.get("C:\\Users\\DELL\\Desktop\\DatingApp_with_Femi\\src\\test\\resources\\images\\figmapic.PNG");

        // create a stream that can read from file pointed to by path
        try (InputStream inputStream = Files.newInputStream(path)){

            // create a multipartfile using bytes from file pointed to by path
            MultipartFile image = new MockMultipartFile("testImage",inputStream);
            return image;
        }
        catch (Exception e) {
            throw  new PromiscuousBaseException(e.getMessage());
        }
    }
}
