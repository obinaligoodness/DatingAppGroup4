package com.legends.promiscuous.services;

import com.github.fge.jsonpatch.JsonPatch;
import com.legends.promiscuous.dtos.requests.LoginRequest;
import com.legends.promiscuous.dtos.requests.RegisterUserRequest;
import com.legends.promiscuous.dtos.requests.UpdateUserRequest;
import com.legends.promiscuous.dtos.response.*;
import com.legends.promiscuous.models.User;

import java.util.List;

public interface UserService {
    RegisterUserResponse register(RegisterUserRequest service);

    ApiResponse<?> activateUserAccount(String token);

    GetUserResponse getUserById(long id);

    List<GetUserResponse> getAllUsers(int page, int pageSize);
    void deleteAll();
    LoginResponse login(LoginRequest loginRequest);

    UpdateUserResponse updateProfile(UpdateUserRequest updateUserRequest);

    UpdateUserResponse updateProfile(UpdateUserRequest updateUserRequest, Long id);
}
