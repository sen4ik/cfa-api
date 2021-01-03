package com.sen4ik.cfaapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sen4ik.cfaapi.base.Constants;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

// @Data
// @Builder
@Entity
@Table(name = "users", schema = Constants.SCHEMA)
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "The database generated category ID", required = false, hidden = true)
    private int id;

    @Basic
    @Column(name = "username")
    @NotNull
    @Size(max = 30)
    @ApiModelProperty(example = "testUser", required = true)
    private String username;

    @Basic
    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    @Length(max = 200)
    @ApiModelProperty(example = "password123", required = true)
    private String password;

    @Basic
    @Column(name = "active")
    @ApiModelProperty(required = false, hidden = true)
    private Boolean active = true;

    @Basic
    @Column(name = "email")
    @NotNull
    @Size(max=50)
    @ApiModelProperty(example = "testUser@test.com", required = true)
    private String email;

    @Basic
    @Column(name = "lastname")
    @NotNull
    @Size(max = 50)
    @ApiModelProperty(example = "Brown", required = true)
    private String lastname;

    @Basic
    @Column(name = "firstname")
    @NotNull
    @Size(max = 50)
    @ApiModelProperty(example = "John", required = true)
    private String firstname;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /*
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false) // , nullable = false
    private Set<UserRole> userRoles;

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }
    */

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name="user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonInclude(JsonInclude.Include.NON_NULL)
    // @JsonIgnore
    @ApiModelProperty(required = false, hidden = true)
    private Set<Role> roles;

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        try{
            List<String> rolesConverted = roles.stream().map(r -> r.getRoleName()).collect(toList());
            return rolesConverted
                    .stream()
                    .map(s -> new SimpleGrantedAuthority(s))
                    .collect(toList());
        }
        catch(Exception e){
            return null;
        }
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", active=" + active +
                ", email='" + email + '\'' +
                ", lastname='" + lastname + '\'' +
                ", firstname='" + firstname + '\'' +
                ", roles=" + roles +
                '}';
    }
}
