delimiter //

drop trigger if exists `cfa`.user_role_delete //

CREATE TRIGGER `cfa`.user_role_delete AFTER DELETE on `cfa`.users
FOR EACH ROW
BEGIN
DELETE FROM `cfa`.user_role
    WHERE `cfa`.user_role.user_id = old.id;
END

//

delimiter ;
