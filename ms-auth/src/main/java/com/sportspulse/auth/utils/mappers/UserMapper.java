package com.sportspulse.auth.utils.mappers;

import com.sportspulse.auth.dto.responses.RegisterResponse;
import com.sportspulse.auth.models.UserEntity;
import org.mapstruct.Mapper;

/** Mapper for converting {@link UserEntity} objects into response DTOs. */
@Mapper(componentModel = "spring")
public interface UserMapper {

  /**
   * Converts a {@link UserEntity} into a {@link RegisterResponse}.
   *
   * @param userEntity the user entity to convert
   * @return the mapped register response
   */
  RegisterResponse toRegisterResponse(UserEntity userEntity);
}
