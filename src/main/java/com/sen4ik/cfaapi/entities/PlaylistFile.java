package com.sen4ik.cfaapi.entities;

import com.sen4ik.cfaapi.base.Constants;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "playlist_file", schema = Constants.SCHEMA)
@Getter
@Setter
public class PlaylistFile {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @JsonIgnore
    private Integer playlistFileId;

    @Basic
    @Column(name = "playlist_id") // insertable = false, updatable = false
    private Integer playlistId;

    @Basic
    @Column(name = "file_id")
    private Integer fileId;

    /*
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;
    */

    /*
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "file_id", insertable = false, updatable = false)
    private FileEntity file;
    */

    @ManyToOne
    @JoinColumn(name="file_id", nullable = false, insertable = false, updatable = false)
    private FileEntity file;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaylistFile that = (PlaylistFile) o;
        return playlistFileId == that.playlistFileId &&
                Objects.equals(playlistId, that.playlistId) &&
                Objects.equals(fileId, that.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistFileId, playlistId, fileId);
    }
}
