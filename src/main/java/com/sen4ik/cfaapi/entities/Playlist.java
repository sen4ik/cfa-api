package com.sen4ik.cfaapi.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sen4ik.cfaapi.base.Constants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "playlists", schema = Constants.SCHEMA)
@Getter
@Setter
public class Playlist {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(required = false)
    private Integer id;

    @Basic
    @Column(name = "user_id")
    @ApiModelProperty(required = false)
    private Integer userId;

    @Basic
    @Column(name = "playlist_name")
    @Size(max = 80)
    @NotNull
    @NotEmpty(message = "Please provide a playlist name")
    @ApiModelProperty(required = true)
    private String playlistName;

    /*
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name="playlist_file", joinColumns = @JoinColumn(name = "playlist_id"), inverseJoinColumns = @JoinColumn(name = "file_id"))
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FileEntity> files;
    */

    /*
    @OneToMany(mappedBy = "playlist")
    private List<PlaylistFile> playlistFiles = new ArrayList<>();
    */

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name="playlist_file", joinColumns = @JoinColumn(name = "playlist_id"), inverseJoinColumns = @JoinColumn(name = "id"))
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PlaylistFile> playlistFiles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist that = (Playlist) o;
        return id == that.id &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(playlistName, that.playlistName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, playlistName);
    }

}
