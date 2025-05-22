package com.itesoft.registree.controller;

import java.util.List;

import jakarta.transaction.Transactional;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.dao.SearchHelper;
import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dao.jpa.UserRepository;
import com.itesoft.registree.dto.CreateUserArgs;
import com.itesoft.registree.dto.DeleteUserArgs;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.UpdateUserArgs;
import com.itesoft.registree.dto.UpdateUserPasswordArgs;
import com.itesoft.registree.dto.User;
import com.itesoft.registree.dto.mapper.UserEntityToUserMapper;
import com.itesoft.registree.exception.ConflictException;
import com.itesoft.registree.exception.ForbiddenException;
import com.itesoft.registree.exception.NotFoundException;
import com.itesoft.registree.exception.UnprocessableException;
import com.itesoft.registree.persistence.WellKnownUsers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;

@Transactional(rollbackOn = Throwable.class)
@Controller
public class UserController {
  @Autowired
  private ConversionService conversionService;

  @Autowired
  private SearchHelper searchHelper;

  @Autowired
  private UserRepository userRepository;

  public List<User> searchUsers(final RequestContext requestContext,
                                final ResponseContext responseContext,
                                final String filter,
                                final String sort,
                                final Integer page,
                                final Integer pageSize) {
    return searchHelper.findAll(responseContext.getExtraProperties(),
                                userRepository,
                                filter,
                                sort,
                                page,
                                pageSize,
                                UserEntityToUserMapper.PROPERTY_MAPPINGS,
                                User.class);
  }

  public User getUser(final RequestContext requestContext,
                      final ResponseContext responseContext,
                      final OneOfLongOrString id) {
    return doGetUser(id);
  }

  public User createUser(final RequestContext requestContext,
                         final ResponseContext responseContext,
                         final CreateUserArgs createUserArgs) {
    final String username = createUserArgs.getUsername();
    if (userRepository.existsByUsername(username)) {
      throw new ConflictException(String.format("User with username [%s] already exists", username));
    }

    final UserEntity userEntity =
      conversionService.convert(createUserArgs, UserEntity.class);

    final UserEntity resultEntity = userRepository.save(userEntity);
    return conversionService.convert(resultEntity, User.class);
  }

  public User updateUser(final RequestContext requestContext,
                         final ResponseContext responseContext,
                         final OneOfLongOrString id,
                         final UpdateUserArgs updateUserArgs) {
    final User originalUser = doGetUser(id);

    final String username = originalUser.getUsername();
    if (WellKnownUsers.ANONYMOUS_USERNAME.equals(username)
      || WellKnownUsers.ADMIN_USERNAME.equals(username)) {
      throw new ForbiddenException(String.format("User with username [%s] cannot be modified", username));
    }

    final String newUsername = updateUserArgs.getUsername();

    if (!username.equalsIgnoreCase(newUsername)) {
      if (userRepository.existsByUsername(newUsername)) {
        throw new ConflictException(String.format("User with username [%s] already exists", newUsername));
      }
    }

    final UserEntity userEntity =
      conversionService.convert(updateUserArgs, UserEntity.class);

    userEntity.setId(originalUser.getId());

    final UserEntity resultEntity = userRepository.save(userEntity);
    return conversionService.convert(resultEntity, User.class);
  }

  public void updateUserPassword(final RequestContext requestContext,
                                 final ResponseContext responseContext,
                                 final OneOfLongOrString id,
                                 final UpdateUserPasswordArgs updateUserPasswordArgs) {
    // FIXME: anyone with WRITE permission on /users can change anyones password, must manage this with some specific routes
    final User originalUser = doGetUser(id);

    final String username = originalUser.getUsername();
    if (WellKnownUsers.ANONYMOUS_USERNAME.equals(username)) {
      throw new ForbiddenException(String.format("A password cannot be set to user [%s]", username));
    }

    userRepository.updatePassword(originalUser.getId(), updateUserPasswordArgs.getNewPassword());
  }

  public void deleteUser(final RequestContext requestContext,
                         final ResponseContext responseContext,
                         final OneOfLongOrString id,
                         final DeleteUserArgs deleteUserArgs) {
    if (id.isLong()) {
      if (!userRepository.existsById(id.getLongValue())) {
        throw new NotFoundException(String.format("User with id [%s] was not found", id));
      }
      userRepository.deleteById(id.getLongValue());
    } else {
      if (id.getStringValue() == null) {
        throw new UnprocessableException("A User id must be provided");
      }

      if (!userRepository.existsByUsername(id.getStringValue())) {
        throw new NotFoundException(String.format("User with id [%s] was not found", id));
      }

      userRepository.deleteByUsername(id.getStringValue());
    }
  }

  private User doGetUser(final OneOfLongOrString id) {
    final UserEntity entity;
    if (id.isLong()) {
      entity =
        userRepository.findById(id.getLongValue()).orElse(null);
    } else {
      entity =
        userRepository.findByUsername(id.getStringValue()).orElse(null);
    }

    if (entity == null) {
      throw new NotFoundException(String.format("User with id [%s] was not found.", id));
    }

    return conversionService.convert(entity, User.class);
  }
}
