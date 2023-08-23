package com.legends.promiscuous.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import com.legends.promiscuous.config.AppConfig;
import com.legends.promiscuous.dtos.requests.*;
import com.legends.promiscuous.dtos.response.*;
import com.legends.promiscuous.enums.Interest;
import com.legends.promiscuous.exceptions.*;
import com.legends.promiscuous.models.Address;
import com.legends.promiscuous.models.User;
import com.legends.promiscuous.repositories.UserRepository;
import com.legends.promiscuous.services.Cloud.CloudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.JsonPath;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.legends.promiscuous.exceptions.ExceptionMessage.*;
import static com.legends.promiscuous.utils.AppUtil.*;
import static com.legends.promiscuous.utils.JwtUtil.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromiscuousUserService implements UserService{
    private final UserRepository userRepository;
    private final MailService mailService;
    private final AppConfig appConfig;
    private final CloudService cloudService;
    @Override
    public RegisterUserResponse register(RegisterUserRequest registerUserRequest) {
        //1. extract registration details from the registration form(registerUserRequest)
        String email = registerUserRequest.getEmail();
        String password = registerUserRequest.getPassword();
        //2. create a user profile with the registration details
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setAddress(new Address());
        //3. save that users profile in the Database
        User savedUser = userRepository.save(user);
        log.info("saved guy-->{}", savedUser);

        //4. send verification token to the users email
        EmailNotificationRequest request = buildEmailRequest(savedUser);
        mailService.send(request);

        //5. return a response
        RegisterUserResponse registerUserResponse = new RegisterUserResponse();
        registerUserResponse.setMessage(ResponseMessage.USER_REGISTRATION_SUCCESSFUL.name());

        return registerUserResponse;
    }

    @Override
    public ApiResponse<?> activateUserAccount(String token) {
        boolean isTestToken = token.equals(appConfig.getTestToken());
        if (isTestToken ) return activateTestAccount();

        boolean isValidJwt = validateToken(token);
        if (isValidJwt) return activateAccount(token);

        throw new AccountActivationFailedException(ExceptionMessage.ACCOUNT_ACTIVATION_FAILED_EXCEPTION.getMessage());
    }

    @Override
    public GetUserResponse getUserById(long id) {
        Optional<User> foundUser = userRepository.findById(id);
        User user = foundUser.orElseThrow(()-> new UserNotFoundException(USER_NOT_FOUND_EXCEPTION.getMessage()));
        GetUserResponse getUserResponse = buildGetUserResponse(user);
        return getUserResponse;
    }

    @Override
    public List<GetUserResponse> getAllUsers(int page, int pageSize) {
        List<GetUserResponse> users = new ArrayList<>();

        Pageable pageable = buildPageRequest(page,pageSize);
        Page<User> usersPage = userRepository.findAll(pageable);
        List<User> foundUsers =  usersPage.getContent();

//        for(User user : foundUsers){
//            GetUserResponse getUserResponse = buildGetUserResponse(user);
//            users.add(getUserResponse);
//        }
//        return users;

                //OR USE LAMBDA
        return foundUsers.stream()
               // .map(PromiscuousUserService::buildGetUserResponse)
                .map(user -> buildGetUserResponse(user))
                .toList();
    }

    private Pageable buildPageRequest(int page, int pageSize) {
        if(page < 1 && pageSize < 1) return PageRequest.of(0, 10);
        if(page < 1) return PageRequest.of(0, 10);
        if(pageSize < 1) return PageRequest.of(page,pageSize);
        return PageRequest.of(page - 1,pageSize);
    }

    private ApiResponse<?> activateAccount(String token) {
        String email = extractEmailFrom(token);
        Optional<User> user = userRepository.findByEmail(email);
        User foundUser = user.orElseThrow(() ->new UserNotFoundException(
                String.format(ExceptionMessage.USER_WITH_EMAIL_NOT_FOUND_EXCEPTION.getMessage(),email)));
        foundUser.setActive(true);
        User savedUser = userRepository.save(foundUser);
        GetUserResponse userResponse = buildGetUserResponse(savedUser);
        var activateUserResponse = buildActivateUserResponse(userResponse);
        return ApiResponse .builder().data(activateUserResponse).build();
    }


    private static ActivateAccountResponse buildActivateUserResponse(GetUserResponse userResponse) {
        return ActivateAccountResponse.builder()
                .message(ResponseMessage.ACCOUNT_ACTIVATION_SUCCESSFUL.name())
                .user(userResponse)
                .build();
    }

    private static GetUserResponse buildGetUserResponse(User savedUser) {
        return GetUserResponse.builder()
                .id(savedUser.getId())
                .address(savedUser.getAddress().toString())
                .fullName(getFullName(savedUser))
                .phoneNumber(savedUser.getPhoneNumber())
                .email(savedUser.getEmail())
                .build();
    }

    private static String getFullName(User savedUser) {
        return savedUser.getFirstName() + BLANK_SPACE + savedUser.getLastName();
    }

    private static ApiResponse<?> activateTestAccount() {
        ApiResponse<?> activateAccountResponse =
                ApiResponse
                        .builder()
                        .build();
        return activateAccountResponse;
    }

    private EmailNotificationRequest buildEmailRequest(User savedUser) {
        EmailNotificationRequest request = new EmailNotificationRequest();
        List<Recipient> recipients = new ArrayList<>();
        Recipient recipient = new Recipient(savedUser.getEmail());
        recipients.add(recipient);
        request.setRecipients(recipients);
        request.setSubject(WELCOME_MAIL_SUBJECT);
        String activationLink = generateActivationLink(appConfig.getBaseUrl(), savedUser.getEmail());
        String emailTemplate = getMailTemplate();

        String mailContent = String.format(emailTemplate, activationLink);
        request.setMailContent(mailContent);

        return request;
    }
    public void deleteAll(){
        userRepository.deleteAll();
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Optional<User> foundUser = userRepository.readByEmail(email);
        User user = foundUser.orElseThrow(()->new UserNotFoundException(String.format(USER_WITH_EMAIL_NOT_FOUND_EXCEPTION.getMessage(),email)));
        boolean isValidPassword = matches(user.getPassword(),password);

        if (isValidPassword) return buildLoginResponse(email);
        throw new BadCredentialsException(BAD_CREDENTIALS_EXCEPTION.getMessage());
    }

    @Override
    public UpdateUserResponse updateProfile(UpdateUserRequest updateUserRequest) {
        User user = new User();
        return null;
    }
    private JsonPatch buildUpdatePatch(UpdateUserRequest updateUserRequest) {

        List<String> list = List.of("interests","street","houseNumber","country","state","gender","profile_image");
        Field[] fields = updateUserRequest.getClass().getDeclaredFields();
        List <ReplaceOperation> operations = Arrays.stream(fields)
                        .filter(field -> {
                            field.setAccessible(true);
                            try {
                           return field.get(updateUserRequest) != null && !list.contains(field.getName());
                        }
                        catch (IllegalArgumentException | IllegalAccessException e){
                            throw new RuntimeException(e);
                        }
                        })
                                .map(field -> {
                                    field.setAccessible(true);
                                    try {
                                        String path = "/"+field.getName();
                                        JsonPointer pointer = new JsonPointer(path);
                                        String value = field.get(updateUserRequest).toString();
                                        TextNode node = new TextNode(value);
                                        ReplaceOperation operation = new ReplaceOperation(pointer,node);
                                        return operation;
                                    }
                                    catch (Exception e){
                                        throw new RuntimeException(e);
                                    }
                                }).toList();
        List<JsonPatchOperation> patchOperations = new ArrayList<>(operations);
        return new JsonPatch(patchOperations);
    }

    @Override
    public UpdateUserResponse updateProfile(UpdateUserRequest updateUserRequest,Long id){
        ObjectMapper mapper = new ObjectMapper();
        ModelMapper modelMapper = new ModelMapper();
        MultipartFile profileImage = updateUserRequest.getProfileImage();
        boolean isFormWithProfileImage = profileImage != null;
        if(isFormWithProfileImage)  cloudService.upload(profileImage);

        User foundUser = findUserById(id);

        Set<String> userInterests = updateUserRequest.getInterests();
        Set<Interest> interests = parseInterestsFrom(userInterests);

        Address userAddress = foundUser.getAddress();
        modelMapper.map(updateUserRequest, userAddress);
        foundUser.setInterests(interests);
        foundUser.setAddress(userAddress);
        JsonPatch updatePatch = buildUpdatePatch(updateUserRequest);

        // convert user to json node
        JsonNode userNode = mapper.convertValue(foundUser,JsonNode.class);
        try{
            // apply patch to jsonNode from step 1
            JsonNode updatedNode = updatePatch.apply(userNode);
            // convert the updated node to user
            User updatedUser = mapper.convertValue(updatedNode, User.class);
            // save the updated user
            userRepository.save(updatedUser);
            UpdateUserResponse response = new UpdateUserResponse();
            response.setMessage("update successful");
            return response;
        }catch (JsonPatchException exception){
            throw  new PromiscuousBaseException(exception.getMessage());
        }
    }

    private User findUserById(Long id) {
        Optional<User> foundUser = userRepository.findById(id);
        return foundUser.get();
    }


    private static LoginResponse buildLoginResponse(String email) {
        String accessToken = generateToken(email);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(accessToken);
        return loginResponse;
    }
    private static Set<Interest> parseInterestsFrom (Set<String> interests){
        Set <Interest> userInterests = interests.stream()
                .map(interest->Interest.valueOf(interest.toUpperCase()))
                .collect(Collectors.toSet());
        return userInterests;
    }
}
