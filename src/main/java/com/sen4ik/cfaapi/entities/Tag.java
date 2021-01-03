package com.sen4ik.cfaapi.entities;

import com.sen4ik.cfaapi.base.Constants;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tags", schema = Constants.SCHEMA)
public class Tag {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "The database generated category ID", required = false, hidden = true)
    private int id;

    @Basic
    @Column(name = "tag_name")
    @NotNull
    @ApiModelProperty(example = "1990 Songs", required = true)
    private String tagName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", tagName='" + tagName + '\'' +
                '}';
    }
}
