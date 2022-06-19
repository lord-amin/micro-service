-- MySQL dump 10.13  Distrib 5.5.60, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: auth_server_unit
-- ------------------------------------------------------
-- Server version	5.5.60-0+deb8u1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
use `auth_server` ;

--
-- Table structure for table `tbl_client`
--

DROP TABLE IF EXISTS `tbl_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_client` (
  `client_id` varchar(255) NOT NULL,
  `access_token_validity_seconds` int(11) DEFAULT NULL,
  `client_secret` varchar(255) NOT NULL,
  `grant_type` varchar(255) NOT NULL,
  `refresh_token_validity_seconds` int(11) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_permission`
--

DROP TABLE IF EXISTS `tbl_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `permission` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `service_name` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tbl_permission_permission_uindex` (`permission`),
  KEY `K_PERMISSION_PARENT_ID` (`parent_id`),
  CONSTRAINT `FK9ml1gbcir3dt7gkovv2jhe583` FOREIGN KEY (`parent_id`) REFERENCES `tbl_permission` (`id`),
  CONSTRAINT `FK_PERMISSION_TO_PERMISSION_ID` FOREIGN KEY (`parent_id`) REFERENCES `tbl_permission` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_permission_url`
--

DROP TABLE IF EXISTS `tbl_permission_url`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_permission_url` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `method` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `foreign_key_url_to_permission_index` (`permission_id`),
  CONSTRAINT `FKnqeaq7bdlcnknyvek72b6a2id` FOREIGN KEY (`permission_id`) REFERENCES `tbl_permission` (`id`),
  CONSTRAINT `foreign_key_url_to_permission_const` FOREIGN KEY (`permission_id`) REFERENCES `tbl_permission` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_role`
--

DROP TABLE IF EXISTS `tbl_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ROLE_NAME` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_role_permission`
--

DROP TABLE IF EXISTS `tbl_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_role_permission` (
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `K_ROLE_PERMISSION_PERMISSION_ID` (`permission_id`),
  CONSTRAINT `FKey6ucxnb0cg80jar6tje0k82e` FOREIGN KEY (`role_id`) REFERENCES `tbl_role` (`id`),
  CONSTRAINT `FKederj3igpg2d7b1llsoibhb2x` FOREIGN KEY (`permission_id`) REFERENCES `tbl_permission` (`id`),
  CONSTRAINT `FK_ROLE_PERMISSION_TO_PERMISSION_ID` FOREIGN KEY (`permission_id`) REFERENCES `tbl_permission` (`id`),
  CONSTRAINT `FK_ROLE_PERMISSION_TO_ROLE_ID` FOREIGN KEY (`role_id`) REFERENCES `tbl_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_user`
--

DROP TABLE IF EXISTS `tbl_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `modified_date` datetime DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `client_id` varchar(255) NOT NULL,
  `pass_expiration_date` datetime DEFAULT NULL,
  `last_login_attempt_time` datetime DEFAULT NULL,
  `block_date` datetime DEFAULT NULL,
  `block_count` int(11) DEFAULT NULL,
  `super_admin` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_USER_USERNAME` (`username`),
  KEY `K_USER_CLIENT_ID` (`client_id`),
  CONSTRAINT `FKexmbss5iveb9qvqiwouojmm8t` FOREIGN KEY (`client_id`) REFERENCES `tbl_client` (`client_id`),
  CONSTRAINT `FK_USER_TO_CLIENT_ID` FOREIGN KEY (`client_id`) REFERENCES `tbl_client` (`client_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_user_role`
--

DROP TABLE IF EXISTS `tbl_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_user_role` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `FK6phlytlf1w3h9vutsu019xor5` (`role_id`),
  CONSTRAINT `FKggc6wjqokl2vlw89y22a1j2oh` FOREIGN KEY (`user_id`) REFERENCES `tbl_user` (`id`),
  CONSTRAINT `FK6phlytlf1w3h9vutsu019xor5` FOREIGN KEY (`role_id`) REFERENCES `tbl_role` (`id`),
  CONSTRAINT `FK_USER_ROLE_TO_ROLE_ID` FOREIGN KEY (`role_id`) REFERENCES `tbl_role` (`id`),
  CONSTRAINT `FK_USER_ROLE_TO_USER_ID` FOREIGN KEY (`user_id`) REFERENCES `tbl_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-12-30 11:32:21
