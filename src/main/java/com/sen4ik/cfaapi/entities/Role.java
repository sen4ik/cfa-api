package com.sen4ik.cfaapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sen4ik.cfaapi.base.Constants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "role", schema = Constants.SCHEMA)
public class Role {

    @Id
    @Column(name = "role_id")
    @JsonIgnore
    private int roleId;

    @Basic
    @Column(name = "role_name")
    @NotNull
    private String roleName;

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}
