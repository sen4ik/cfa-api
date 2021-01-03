delimiter //

drop trigger if exists `cfa`.playlist_delete //

-- CREATE TRIGGER `cfa`.playlist_delete AFTER DELETE on `cfa`.playlists
CREATE TRIGGER `cfa`.playlist_delete BEFORE DELETE on `cfa`.playlists
    FOR EACH ROW
BEGIN
    DELETE FROM `cfa`.playlist_file
    WHERE `cfa`.playlist_file.playlist_id = old.id;
END

//

delimiter ;

-- SHOW TRIGGERS;
