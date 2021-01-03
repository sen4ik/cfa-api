-- MySQL dump 10.13  Distrib 8.0.17, for macos10.14 (x86_64)
--
-- Host: localhost    Database: cfa
-- ------------------------------------------------------
-- Server version	8.0.18

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `category_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `category_folder` varchar(100) NOT NULL,
  `parent_id` int(11) NOT NULL,
  `order_by` varchar(50) DEFAULT NULL,
  `zip` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `added_on` timestamp NOT NULL ON UPDATE CURRENT_TIMESTAMP,
  `hidden` tinyint(1) NOT NULL DEFAULT '0',
  `added_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=736 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (3,'Пение','songs',0,'title ASC',NULL,'2020-03-16 17:57:39',0,1),(4,'Проповеди','sermons',0,'title ASC',NULL,'2020-03-16 17:57:39',0,1),(6,'The Crossroads','the_crossroads',3,'filename','the_crossroads.zip','2019-12-11 22:05:59',0,1),(7,'В руках Великого Мастера','in_the_hands_of_great_master',3,'filename',NULL,'2020-03-16 17:57:39',0,1),(12,'Александр Сенцов','alex_sentsov',4,'title ASC',NULL,'2019-12-11 22:05:59',0,1),(13,'Денис Самарин','denis_samarin',4,'title ASC',NULL,'2019-12-11 22:05:59',0,1),(18,'Молодёжные лагеря','youth_camps',0,'title ASC',NULL,'2019-12-11 22:05:59',0,1),(19,'Калифорнийские молодежные лагеря','california_camps',18,'title ASC',NULL,'2020-03-16 17:57:39',0,1),(20,'Калифорнийский молодежный лагерь для членов церкви - Ноябрь 2015','november_youth_camp_2015',19,'title ASC',NULL,'2019-12-11 22:05:59',0,1),(21,'Калифорнийский летний молодежный лагерь - Июль 2015','california_summer_youth_camp_2015',19,'title ASC',NULL,'2019-12-11 22:05:59',0,1),(23,'Михаил Голубин','mihail_golubin',4,'title ASC',NULL,'2019-12-11 22:05:59',0,1),(24,'Библейские курсы','bible_conferences',0,'title ASC',NULL,'2020-03-16 17:57:39',0,1),(25,'Семинар - Основы текстовой проповеди','osnovy_tekstovoy_propovedi',24,'title ASC',NULL,'2019-12-11 22:05:59',0,1),(26,'Молодежные конференции','youth_conferences',0,'title ASC',NULL,'2019-12-11 22:05:59',0,1),(27,'Пасторские конференции','pastors_conferences',0,'title ASC',NULL,'2019-12-11 22:05:59',0,1),(28,'Послание к Филимону','poslanie_k_filimonu',13,'title ASC','Denis_Samarin_Poslanie_k_Filimonu.zip','2020-03-10 21:21:18',0,1);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `files`
--

DROP TABLE IF EXISTS `files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `files` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_title` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `file_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `category_id` int(11) NOT NULL,
  `file_size_bytes` bigint(20) NOT NULL,
  `downloaded` int(11) NOT NULL,
  `listened` int(11) NOT NULL,
  `hidden` tinyint(1) NOT NULL,
  `added_by` int(11) DEFAULT NULL,
  `added_on` datetime DEFAULT NULL,
  `length_in_seconds` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=361 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `files`
--

LOCK TABLES `files` WRITE;
/*!40000 ALTER TABLE `files` DISABLE KEYS */;
INSERT INTO `files` VALUES (2,'О жизни молодежи','Antonuk O jizni molodeji.mp3',4,30010851,0,0,0,1,'2019-12-11 14:05:59',1879),(3,'О практическом целомудрии','Antonuk O prakticheskom celomurdii.mp3',4,35123200,0,0,0,1,'2019-12-11 14:05:59',1757),(4,'В руках великого Мастера','01-V rukah velikogo Mastera.mp3',7,11990644,0,0,0,1,'2019-12-11 14:05:59',299),(5,'Исповедь','02-Ispoved.mp3',7,13479388,0,0,0,1,'2019-12-11 14:05:59',337),(6,'Небо надо мной','03-Nebo nado mnoi.mp3',7,11276548,0,0,0,1,'2019-12-11 14:05:59',282),(7,'Время держит нас','04-Vremia derzhit nas.mp3',7,7570348,0,0,0,1,'2019-12-11 14:05:59',189),(8,'Храни нас, Господи','05-Hrani nas, Gospodi.mp3',7,8813752,0,0,0,1,'2019-12-11 14:05:59',220),(9,'Осенний лист','06-Osenniy list.mp3',7,8793916,0,0,0,1,'2019-12-11 14:05:59',219),(10,'Рождество на пороге','07-Rozhdestvo na poroge.mp3',7,13822864,0,0,0,1,'2019-12-11 14:05:59',345),(11,'Детства дом','08-Detstva dom.mp3',7,12564844,0,0,0,1,'2019-12-11 14:05:59',314),(12,'Дочь Царя','09-Doch Tsaria.mp3',7,11635684,0,0,0,1,'2019-12-11 14:05:59',291),(13,'Из года в год','10-Iz goda v god.mp3',7,9663568,0,0,0,1,'2019-12-11 14:05:59',241),(14,'Ты свободен','11-Ty svoboden.mp3',7,10641796,0,0,0,1,'2019-12-11 14:05:59',266),(15,'Встреча у престола','12-Vstrecha u prestola.mp3',7,11107420,0,0,0,1,'2019-12-11 14:05:59',277),(16,'Wonderful Grace of Jesus','01. Wonderful Grace of Jesus.mp3',6,6740101,0,0,0,1,'2019-12-11 14:05:59',281),(17,'Finding Happiness','02. Finding Happiness.mp3',6,4802846,0,0,0,1,'2019-12-11 14:05:59',200),(18,'How can you not know!','03. How can you not know!.mp3',6,7016575,0,0,0,1,'2019-12-11 14:05:59',292),(19,'Come unto Him my weary Friend','04. Come unto Him my weary Friend.mp3',6,4755223,0,0,0,1,'2019-12-11 14:05:59',198),(20,'What a Friend We have in Jesus','05. What a Friend We have in Jesus.mp3',6,5557706,0,0,0,1,'2019-12-11 14:05:59',231),(21,'Not my will_ but Yours be done','06. Not my will_ but Yours be done.mp3',6,5828544,0,0,0,1,'2019-12-11 14:05:59',242),(22,'God is Glorious and Mighty','07. God is Glorious and Mighty.mp3',6,4358365,0,0,0,1,'2019-12-11 14:05:59',181),(23,'Just One More Hill to Cli','08. Just One More Hill to Climb.mp3',6,7323160,0,0,0,1,'2019-12-11 14:05:59',305),(24,'You have come to this world','09. You have come to this world.mp3',6,4459931,0,0,0,1,'2019-12-11 14:05:59',185),(25,'I long for my savior','10. I long for my savior.mp3',6,5058643,0,0,0,1,'2019-12-11 14:05:59',210),(26,'Repentance','11. Repentance.mp3',6,5982104,0,0,0,1,'2019-12-11 14:05:59',249),(27,'Your Choice','12. Your Choice.mp3',6,6804650,0,0,0,1,'2019-12-11 14:05:59',283),(28,'Love Lifted Me','13. Love Lifted Me.mp3',6,6540714,0,0,0,1,'2019-12-11 14:05:59',272),(29,'Почему нужно пpинимать немощного в веpе','Pochemu_nuzhno_prinimat\'_nemoshnogo_v_vere.mp3',13,30628754,0,0,0,1,'2019-12-11 14:05:59',4396),(30,'Алмаз и графит','Almaz_i_grafit.mp3',12,51165208,0,0,0,1,'2019-12-11 14:05:59',1600),(31,'О злословии','O_zloslovii.mp3',12,23844118,0,0,0,1,'2019-12-11 14:05:59',2994),(32,'Мнимый враг','Mnimyij_vrag.mp3',23,41969544,6,56,0,1,'2019-12-11 14:05:59',1751),(33,'Второе поприще','Vtoroe_popriwe.mp3',23,37993603,3,56,0,1,'2019-12-11 14:05:59',950),(34,'Архив аудиозаписей','november_youth_camp_2015_audio.zip',20,2238523878,0,0,0,1,'2019-12-11 14:05:59',NULL),(35,'Архив фотографий','november_youth_camp_2015_pictures.zip',20,987780247,0,0,0,1,'2019-12-11 14:05:59',NULL),(36,'Видео','california_summer_camp_2015_video.mp4',21,579161601,0,0,0,1,'2019-12-11 14:05:59',NULL),(37,'Герменевтика Ветхого Завета - Денис Самарин','Samarin Denis - Vethiy Zavet.mp4',24,506277600,0,0,0,1,'2019-12-11 14:05:59',NULL),(38,'Беседа 1','Denis_Samarin_Poslanie_k_Filimonu_Beseda_1.mp3',28,82353221,0,0,0,1,'2019-12-11 14:05:59',2576),(39,'Беседа 2','Denis_Samarin_Poslanie_k_Filimonu_Beseda_2.mp3',28,32983595,0,0,0,1,'2019-12-11 14:05:59',4142),(40,'Беседа 3','Denis_Samarin_Poslanie_k_Filimonu_Beseda_3.mp3',28,99297286,0,0,0,1,'2019-12-11 14:05:59',3106);
/*!40000 ALTER TABLE `files` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `playlist_file`
--

DROP TABLE IF EXISTS `playlist_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `playlist_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playlist_id` int(11) DEFAULT NULL,
  `file_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `playlist_file`
--

LOCK TABLES `playlist_file` WRITE;
/*!40000 ALTER TABLE `playlist_file` DISABLE KEYS */;
INSERT INTO `playlist_file` VALUES (1,1,2),(2,1,8),(3,1,21),(4,1,22),(5,2,3),(11,7,22),(13,6,22),(14,6,23);
/*!40000 ALTER TABLE `playlist_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `playlists`
--

DROP TABLE IF EXISTS `playlists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `playlists` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `playlist_name` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `playlists`
--

LOCK TABLES `playlists` WRITE;
/*!40000 ALTER TABLE `playlists` DISABLE KEYS */;
INSERT INTO `playlists` VALUES (1,1,'first_playlist'),(2,1,'second_playlist'),(3,1,'third_playlist'),(6,2,'fourth_playlist'),(7,1,'fifth_playlist');
/*!40000 ALTER TABLE `playlists` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`sen4ik`@`localhost`*/ /*!50003 TRIGGER `playlist_delete` AFTER DELETE ON `playlists` FOR EACH ROW BEGIN
DELETE FROM `cfa`.playlist_file
    WHERE `cfa`.playlist_file.playlist_id = old.id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `role_id` int(11) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'ROLE_ADMIN'),(2,'ROLE_USER');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tags` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tags`
--

LOCK TABLES `tags` WRITE;
/*!40000 ALTER TABLE `tags` DISABLE KEYS */;
INSERT INTO `tags` VALUES (1,'Денис Самарин');
/*!40000 ALTER TABLE `tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `user_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=90 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,1,1),(2,2,3),(3,2,5),(32,2,18);
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `lastname` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `firstname` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'sen4ik','{bcrypt}$2a$10$AxV0AannJknFAo9juaa07urYOxgEbTMxiB9qBJjilW2Yw4rOyHkl6',1,'Sentsov','Artur','asentsov@test.com'),(2,'user','{bcrypt}$2a$10$AxV0AannJknFAo9juaa07urYOxgEbTMxiB9qBJjilW2Yw4rOyHkl6',1,'Pupkin','Vasia','user@test.com'),(3,'muser','{bcrypt}$2a$10$AxV0AannJknFAo9juaa07urYOxgEbTMxiB9qBJjilW2Yw4rOyHkl6',1,'Another','User','user@test.com'),(32,'user2','{bcrypt}$2a$10$RmnyDylzuZOiObg4DfJO..T8eNA9f3d7hsCc1rSQfuQaWlSBLNHVq',1,'Lname2','Fname2','user@test.com');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`sen4ik`@`localhost`*/ /*!50003 TRIGGER `user_role_delete` AFTER DELETE ON `users` FOR EACH ROW BEGIN
DELETE FROM user_role
    WHERE user_role.user_id = old.id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-12-23 23:15:38
