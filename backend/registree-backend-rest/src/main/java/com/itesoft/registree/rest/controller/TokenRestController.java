package com.itesoft.registree.rest.controller;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.CreateTokenArgs;
import com.itesoft.registree.dto.CreateTokenResult;
import com.itesoft.registree.dto.DeleteTokenArgs;
import com.itesoft.registree.dto.GetTokenResult;
import com.itesoft.registree.security.auth.AuthenticationService;
import com.itesoft.registree.security.token.TokenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tokens")
public class TokenRestController {
  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private TokenService tokenService;

  @RequestMapping(value = "/{token}",
                  method = RequestMethod.GET,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<GetTokenResult> getToken(final HttpServletRequest request,
                                                 @PathVariable("token") final String token) {
    // TODO: check given token matches actual authentication
    final String actualToken = tokenService.getToken(token);
    if (actualToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    final GetTokenResult getTokenResult = GetTokenResult.builder()
      .token(actualToken)
      .build();
    return ResponseEntity.ok(getTokenResult);
  }

  @RequestMapping(method = RequestMethod.POST,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<CreateTokenResult> createToken(final HttpServletRequest request,
                                                       @RequestBody final CreateTokenArgs createTokenArgs) {
    final String token = tokenService.createToken(createTokenArgs.getUsername(),
                                                  createTokenArgs.getPassword());
    if (token == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    final CreateTokenResult createTokenResult = CreateTokenResult.builder()
      .token(token)
      .build();
    return ResponseEntity.created(null)
      .body(createTokenResult);
  }

  @RequestMapping(value = "/{token}",
                  method = RequestMethod.DELETE,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Void> deleteToken(@PathVariable("token") final String token,
                                          @RequestBody(required = false) final DeleteTokenArgs deleteTokenArgs) {
    // TODO: check given token matches actual authentication
    tokenService.deleteToken(token);
    authenticationService.clearAuthentication();
    return ResponseEntity.noContent()
      .build();
  }
}
