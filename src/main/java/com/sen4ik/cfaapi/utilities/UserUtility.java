package com.sen4ik.cfaapi.utilities;

import com.sen4ik.cfaapi.entities.Role;
import com.sen4ik.cfaapi.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

public class UserUtility {

    public static int getCurrentlyLoggedInUserId(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int loggedInUserId = user.getId();
        // UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // String username = userDetails.getUsername();
        return loggedInUserId;
    }

    public static User getCurrentlyLoggedInUser(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user;
    }

    public static boolean isLoggedInUserAnAdmin(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Set<Role> roles = user.getRoles();
        for (Role role : roles) {
            if(role.getRoleName().equals("ROLE_ADMIN")){
                return true;
            }
        }
        return false;
    }

}
