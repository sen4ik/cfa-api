package com.sen4ik.cfaapi.controllers;

import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.enums.ErrorMessagesCustom;
import com.sen4ik.cfaapi.entities.AuthenticationRequest;
import com.sen4ik.cfaapi.services.AuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static org.springframework.http.ResponseEntity.ok;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RestController
@Api(tags = "Authentication", description = " ")
public class AuthenticationController {

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping(value = Constants.API_PREFIX + "/auth/signin")
    @ApiOperation(value = "Sign In")
    public ResponseEntity signIn(@RequestBody AuthenticationRequest data) throws UsernameNotFoundException {
        try {
            String username = data.getUsername();
            Map<Object, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("token", authenticationService.getJwtToken(username, data.getPassword()));
            return ok(model);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException(ErrorMessagesCustom.invalid_username_password_supplied.value);
        }
    }
}
