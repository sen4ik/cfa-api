package com.sen4ik.cfaapi.controllers;

import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.enums.ErrorMessagesCustom;
import com.sen4ik.cfaapi.repositories.UserRepository;
import com.sen4ik.cfaapi.entities.AuthenticationRequest;
import com.sen4ik.cfaapi.configs.security.JwtTokenProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.ResponseEntity.ok;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RestController
// @RequestMapping(path = EnvironmentVariables.apiPrefix + "/auth")
@Api(tags = "Authentication", description = " ")
public class AuthenticationController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @PostMapping(value = Constants.API_PREFIX + "/auth/signin")
    @ApiOperation(value = "Sign In")
    public ResponseEntity signin(@RequestBody AuthenticationRequest data) {
        try {
            String username = data.getUsername();
            String password = data.getPassword();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(authentication);

            /*
            Optional<User> tU = this.userRepository.findByUsername(username);
            Set<Role> tR = tU.orElseThrow(
                    () -> new UsernameNotFoundException("Username " + username + "not found")
            ).getRoles();
            Set<UserRole> tUR = tU.orElseThrow(
                    () -> new UsernameNotFoundException("Username " + username + "not found")
            ).getUserRoles();
            */

            List<String> roles = this.userRepository.findByUsername(username)
                    .orElseThrow(
                            () -> new UsernameNotFoundException("Username " + username + "not found")
                    ).getRoles()
                    .stream()
                    .map(r -> r.getRoleName())
                    .collect(toList());
            String token = jwtTokenProvider.createToken(username, roles);

            Map<Object, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("token", token);
            return ok(model);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException(ErrorMessagesCustom.invalid_username_password_supplied.value);
        }
    }
}
