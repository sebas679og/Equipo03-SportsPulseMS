package com.sportspulse.auth.controllers;

import com.sportspulse.auth.config.ApiPaths;
import com.sportspulse.auth.dto.requests.RegisterRequest;
import com.sportspulse.auth.dto.responses.RegisterResponse;
import com.sportspulse.auth.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping(ApiPaths.Auth.REGISTER)
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody @Valid RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }
}
