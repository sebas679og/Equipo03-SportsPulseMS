package com.sportspulse.auth.controllers;

import com.sportspulse.auth.config.ApiPaths;
import com.sportspulse.auth.dto.requests.LoginRequest;
import com.sportspulse.auth.dto.requests.RegisterRequest;
import com.sportspulse.auth.dto.responses.ErrorResponse;
import com.sportspulse.auth.dto.responses.LoginResponse;
import com.sportspulse.auth.dto.responses.RegisterResponse;
import com.sportspulse.auth.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller responsible for receiving user authentication requests. */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
    name = "Auth",
    description = "Controller responsible for receiving user authentication requests.")
public class AuthController {

  private final UserService userService;

  @Operation(
      summary = "Register a new user",
      description =
          """
           "Endpoint to register a new user account.
           This endpoint accepts user registration data and creates
           a new account if the provided information is valid and does
           not conflict with existing accounts.
           """)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "User registration data. All fields are required.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = RegisterRequest.class)))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description =
            "User registered successfully - Returns the details of the newly created user account.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = RegisterResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error - Invalid input fields",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict - alias already registered",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
  })
  @PostMapping(ApiPaths.Auth.REGISTER)
  public ResponseEntity<RegisterResponse> registerUser(
      @RequestBody @Valid RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
  }

  @PostMapping(ApiPaths.Auth.LOGIN)
  public ResponseEntity<LoginResponse> userAuthentication(
      @RequestBody @Valid LoginRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.loginUser(request));
  }
}
