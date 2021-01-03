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
import java.util.Set;

@Entity
@Table(name = "files", schema = Constants.SCHEMA)
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "The database generated category ID", required = false, hidden = true)
    private Integer id;

    @Basic
    @Column(name = "file_title")
    @Size(max = 100, min = 1)
    @NotEmpty(message = "Please provide a file title")
    private String fileTitle;

    @Basic
    @Column(name = "file_name")
    @Size(max = 100, min = 1)
    @NotEmpty(message = "Please provide a file name")
    @ApiModelProperty(required = false, hidden = true)
    private String fileName;

    @Basic
    @Column(name = "category_id")
    @NotNull(message = "Please provide category id")
    @ApiModelProperty(required = true)
    private Integer categoryId;

    @Basic
    @Column(name = "file_size_bytes")
    @ApiModelProperty(required = false, hidden = true)
    private Long fileSizeBytes;

    @Basic
    @Column(name = "downloaded")
    @ApiModelProperty(required = false, hidden = true)
    private Integer downloaded = 0;

    @Basic
    @Column(name = "listened")
    @ApiModelProperty(required = false, hidden = true)
    private Integer listened = 0;

    @Basic
    @Column(name = "hidden")
    @ApiModelProperty(required = false, hidden = true)
    private Boolean hidden = false;

    @Basic
    @Column(name = "added_by", updatable = false)
    private Integer addedBy;

    @Basic
    @Column(name = "length_in_seconds")
    @ApiModelProperty(required = false, hidden = true)
    private Integer lengthInSeconds;

    @Basic
    @Column(name = "added_on", updatable = false)
    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    // @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss Z", timezone = "America/Los_Angeles")
    @ApiModelProperty(required = false, hidden = true)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addedOn;

    /*
    @OneToOne(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private PlaylistFile pf;
    */

    @OneToMany(mappedBy = "file")
    private Set<PlaylistFile> pf;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileTitle() {
        return fileTitle;
    }

    public void setFileTitle(String fileTitle) {
        this.fileTitle = fileTitle;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(Integer downloaded) {
        this.downloaded = downloaded;
    }

    public Integer getListened() {
        return listened;
    }

    public void setListened(Integer listened) {
        this.listened = listened;
    }

    public Integer getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(Integer addedBy) {
        this.addedBy = addedBy;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public LocalDateTime getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(LocalDateTime dateAdded) {
        this.addedOn = dateAdded;
    }

    public Integer getLengthInSeconds() {
        return lengthInSeconds;
    }

    public void setLengthInSeconds(Integer lengthInSeconds) {
        this.lengthInSeconds = lengthInSeconds;
    }

    @Override
    public String toString() {
        return "FileEntity{" +
                "id=" + id +
                ", fileTitle='" + fileTitle + '\'' +
                ", fileName='" + fileName + '\'' +
                ", categoryId=" + categoryId +
                ", fileSizeBytes=" + fileSizeBytes +
                ", downloaded=" + downloaded +
                ", listened=" + listened +
                ", hidden=" + hidden +
                ", addedBy=" + addedBy +
                ", lengthInSeconds=" + lengthInSeconds +
                ", addedOn=" + addedOn +
                '}';
    }
}
