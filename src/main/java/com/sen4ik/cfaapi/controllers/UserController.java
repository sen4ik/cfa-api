package com.sen4ik.cfaapi.controllers;

import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.entities.User;
import com.sen4ik.cfaapi.services.AuthenticationService;
import com.sen4ik.cfaapi.utilities.UserUtility;
import com.sen4ik.cfaapi.base.ResponseHelper;
import com.sen4ik.cfaapi.entities.Role;
import com.sen4ik.cfaapi.entities.UserRole;
import com.sen4ik.cfaapi.exceptions.BadRequestException;
import com.sen4ik.cfaapi.exceptions.RecordNotFoundException;
import com.sen4ik.cfaapi.repositories.RoleRepository;
import com.sen4ik.cfaapi.repositories.UserRepository;
import com.sen4ik.cfaapi.repositories.UserRoleRepository;
import com.sen4ik.cfaapi.utilities.ObjectUtility;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Controller
@RequestMapping(path = Constants.API_PREFIX + "/user")
@Api(tags = "Users", description = " ")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationService authenticationService;

    @GetMapping(path="/all")
    @ApiOperation(value = "Get all users")
    public @ResponseBody Iterable<User> getAll() {
        return userRepository.findAll();
    }

    @GetMapping(path="/me")
    @ApiOperation(value = "Get currently logged in user details")
    public @ResponseBody User me() {
        return UserUtility.getCurrentlyLoggedInUser();
    }

    @GetMapping(path="/{id}")
    @ApiOperation(value = "Get user by id")
    public ResponseEntity<?> getOne(@PathVariable("id") @Min(1) int id) {
        if(!UserUtility.isLoggedInUserAnAdmin()){
            if(UserUtility.getCurrentlyLoggedInUserId() != id){
                return ResponseHelper.actionIsForbidden();
            }
        }
        User user = userRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(id));
        return ResponseHelper.success(user);
    }

    @PostMapping(path="/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Sign up")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    User signUp(@Valid @RequestBody User user) throws Exception {

        // verify username is not taken
        Optional<User> userByUsername = userRepository.findByUsername(user.getUsername());
        if(userByUsername.isPresent()){
            throw new BadRequestException("Username " + user.getUsername() + " is already taken!");
        }

        // verify user with the email does not already exists
        Optional<User> userByEmail = userRepository.findUserByEmail(user.getEmail());
        if(userByEmail.isPresent()){
            throw new BadRequestException("User with email " + user.getEmail() + " already exists!");
        }

        String passwordNotEncoded = user.getPassword();
        String encodedPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        User createdUser = userRepository.save(user);

        Optional<Role> role = roleRepository.findRoleByRoleName("ROLE_USER");
        if (!role.isPresent())
            throw new Exception("There was an issue with assigning the role!");
        int userRoleId = role.get().getRoleId();
        UserRole userRole = new UserRole(createdUser.getId(), userRoleId);
        userRoleRepository.save(userRole);

        // return created user with roles for the user
        Set<Role> roles = new HashSet<Role>();
        roles.add(role.get());
        createdUser.setRoles(roles);

        // include jwtToken in the payload
        String jwtToken = authenticationService.getJwtToken(user.getUsername(), passwordNotEncoded);
        createdUser.setToken(jwtToken);

        return createdUser;
    }

    @DeleteMapping(path="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete user")
    public @ResponseBody ResponseEntity<String> deleteUser(@PathVariable int id) {
        try {
            userRepository.deleteById(id);
            return ResponseHelper.deleteSuccess(id);
        } catch (Exception e) {
            return ResponseHelper.deleteFailed(id, e);
        }
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update user")
    public ResponseEntity<Object> updateUser(@Valid @RequestBody User user, @PathVariable @Min(1) int id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()){
            throw new RecordNotFoundException(id);
        }
        user.setId(id);

        BeanUtils.copyProperties(user, userOptional.get(), ObjectUtility.getNullPropertyNames(user));
        String encodedPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);

        return ResponseHelper.success(userOptional.get());
    }

    // TODO: create endpoint to restore the password for the user
    // TODO: set user as admin
    // TODO: deactivate user
    // TODO: get user roles

}
