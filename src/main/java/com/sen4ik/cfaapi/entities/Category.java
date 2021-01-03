package com.sen4ik.cfaapi.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sen4ik.cfaapi.base.Constants;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories", schema = Constants.SCHEMA)
@NoArgsConstructor
@AllArgsConstructor
// @JsonIgnoreProperties(ignoreUnknown = true)
public class Category {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "The database generated category ID", required = false, hidden = true)
    private Integer id;

    @Basic
    @Column(name = "category_name")
    @Size(max = 100, min = 1)
    @NotEmpty(message = "Please provide a category name")
    @ApiModelProperty(notes = "Category name", example = "Songs")
    // @NotBlank(message = "Please provide a category folder")
    private String categoryName;

    @Basic
    @Column(name = "category_folder")
    @Size(max = 100, min = 1)
    @NotEmpty(message = "Please provide a category folder")
    @ApiModelProperty(notes = "Category folder name in file system which will hold category files", example = "songs")
    private String categoryFolder;

    @Basic
    @Column(name = "parent_id")
    @NotNull(message = "Please provide parent id")
    @ApiModelProperty(notes = "Category id to which category belongs. Set to 0 if you want category to belong to root category.")
    private Integer parentId;

    @Basic
    @Column(name = "order_by")
    @Size(max = 50)
    private String orderBy;

    @Basic
    @Column(name = "zip")
    @Size(max = 100)
    @ApiModelProperty(notes = "Zip that contains all category files", example = "songs.zip")
    private String zip;

    @Basic
    @Column(name = "added_on", updatable = false)
    @CreationTimestamp
    // @Temporal(TemporalType.TIMESTAMP)
    @ColumnDefault("CURRENT_TIMESTAMP")
    // @Transient
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(required = false, hidden = true)
    private LocalDateTime addedOn;

    @Basic
    @Column(name = "hidden")
    @ApiModelProperty(required = false, hidden = true)
    private Boolean hidden = false;

    @Basic
    @Column(name = "added_by", updatable = false)
    // @Transient
    @ApiModelProperty(required = false, hidden = true)
    private Integer addedBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryFolder() {
        return categoryFolder;
    }

    public void setCategoryFolder(String categoryFolder) {
        this.categoryFolder = categoryFolder;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public LocalDateTime getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(LocalDateTime dateAdded) {
        this.addedOn = dateAdded;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Integer getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(Integer addedBy) {
        this.addedBy = addedBy;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", categoryName='" + categoryName + '\'' +
                ", categoryFolder='" + categoryFolder + '\'' +
                ", parentId=" + parentId +
                ", orderBy='" + orderBy + '\'' +
                ", zip='" + zip + '\'' +
                ", addedOn=" + addedOn +
                ", hidden=" + hidden +
                ", addedBy=" + addedBy +
                '}';
    }
}
