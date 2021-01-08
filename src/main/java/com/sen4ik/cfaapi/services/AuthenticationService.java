package com.sen4ik.cfaapi.services;

import com.sen4ik.cfaapi.configs.security.JwtTokenProvider;
import com.sen4ik.cfaapi.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Component
@Slf4j
public class AuthenticationService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    UserRepository userRepository;

    public String getJwtToken(String username, String password) throws UsernameNotFoundException{
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

        List<String> roles = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("Username " + username + "not found")
                ).getRoles()
                .stream()
                .map(r -> r.getRoleName())
                .collect(toList());
        String token = jwtTokenProvider.createToken(username, roles);
        return token;
    }

}
