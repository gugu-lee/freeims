-- MySQL dump 10.13  Distrib 5.1.73, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: hss_db
-- ------------------------------------------------------
-- Server version	5.1.73

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

--
-- Table structure for table `aliases_repository_data`
--

DROP TABLE IF EXISTS `aliases_repository_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `aliases_repository_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sqn` int(11) NOT NULL DEFAULT '0',
  `id_implicit_set` int(11) NOT NULL DEFAULT '0',
  `service_indication` varchar(255) NOT NULL DEFAULT '',
  `rep_data` blob,
  PRIMARY KEY (`id`),
  KEY `idx_id_implicit_set` (`id_implicit_set`),
  KEY `idx_sqn` (`sqn`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Aliases Repository Data';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `aliases_repository_data`
--

LOCK TABLES `aliases_repository_data` WRITE;
/*!40000 ALTER TABLE `aliases_repository_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `aliases_repository_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `application_server`
--

DROP TABLE IF EXISTS `application_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `application_server` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `server_name` varchar(255) NOT NULL DEFAULT '',
  `default_handling` int(11) NOT NULL DEFAULT '0',
  `service_info` varchar(255) NOT NULL DEFAULT '',
  `diameter_address` varchar(255) NOT NULL DEFAULT '',
  `rep_data_size_limit` int(11) NOT NULL DEFAULT '0',
  `udr` tinyint(4) NOT NULL DEFAULT '0',
  `pur` tinyint(4) NOT NULL DEFAULT '0',
  `snr` tinyint(4) NOT NULL DEFAULT '0',
  `udr_rep_data` tinyint(4) NOT NULL DEFAULT '0',
  `udr_impu` tinyint(4) NOT NULL DEFAULT '0',
  `udr_ims_user_state` tinyint(4) NOT NULL DEFAULT '0',
  `udr_scscf_name` tinyint(4) NOT NULL DEFAULT '0',
  `udr_ifc` tinyint(4) NOT NULL DEFAULT '0',
  `udr_location` tinyint(4) NOT NULL DEFAULT '0',
  `udr_user_state` tinyint(4) NOT NULL DEFAULT '0',
  `udr_charging_info` tinyint(4) NOT NULL DEFAULT '0',
  `udr_msisdn` tinyint(4) NOT NULL DEFAULT '0',
  `udr_psi_activation` tinyint(4) NOT NULL DEFAULT '0',
  `udr_dsai` tinyint(4) NOT NULL DEFAULT '0',
  `udr_aliases_rep_data` tinyint(4) NOT NULL DEFAULT '0',
  `pur_rep_data` tinyint(4) NOT NULL DEFAULT '0',
  `pur_psi_activation` tinyint(4) NOT NULL DEFAULT '0',
  `pur_dsai` tinyint(4) NOT NULL DEFAULT '0',
  `pur_aliases_rep_data` tinyint(4) NOT NULL DEFAULT '0',
  `snr_rep_data` tinyint(4) NOT NULL DEFAULT '0',
  `snr_impu` tinyint(4) NOT NULL DEFAULT '0',
  `snr_ims_user_state` tinyint(4) NOT NULL DEFAULT '0',
  `snr_scscf_name` tinyint(4) NOT NULL DEFAULT '0',
  `snr_ifc` tinyint(4) NOT NULL DEFAULT '0',
  `snr_psi_activation` tinyint(4) NOT NULL DEFAULT '0',
  `snr_dsai` tinyint(4) NOT NULL DEFAULT '0',
  `snr_aliases_rep_data` tinyint(4) NOT NULL DEFAULT '0',
  `include_register_request` int(11) DEFAULT NULL,
  `include_register_response` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_diameter_address` (`diameter_address`),
  KEY `idx_server_name` (`server_name`),
  KEY `idx_name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='Application Servers';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `application_server`
--

LOCK TABLES `application_server` WRITE;
/*!40000 ALTER TABLE `application_server` DISABLE KEYS */;
INSERT INTO `application_server` VALUES (1,'default_as','sip:158.85.12.36:5080',0,'','as.saygreet.com',1024,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
/*!40000 ALTER TABLE `application_server` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `capabilities_set`
--

DROP TABLE IF EXISTS `capabilities_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `capabilities_set` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_set` int(11) NOT NULL DEFAULT '0',
  `name` varchar(255) NOT NULL DEFAULT '',
  `id_capability` int(11) NOT NULL DEFAULT '0',
  `is_mandatory` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_capability` (`id_capability`),
  KEY `idx_id_set` (`id_set`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='Capabilities Sets';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `capabilities_set`
--

LOCK TABLES `capabilities_set` WRITE;
/*!40000 ALTER TABLE `capabilities_set` DISABLE KEYS */;
INSERT INTO `capabilities_set` VALUES (2,1,'cap_set1',1,0);
/*!40000 ALTER TABLE `capabilities_set` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `capability`
--

DROP TABLE IF EXISTS `capability`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `capability` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='Capabilities Definition';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `capability`
--

LOCK TABLES `capability` WRITE;
/*!40000 ALTER TABLE `capability` DISABLE KEYS */;
INSERT INTO `capability` VALUES (1,'cap1'),(2,'cap2');
/*!40000 ALTER TABLE `capability` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `charging_info`
--

DROP TABLE IF EXISTS `charging_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `charging_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `pri_ecf` varchar(255) NOT NULL DEFAULT '',
  `sec_ecf` varchar(255) NOT NULL DEFAULT '',
  `pri_ccf` varchar(255) NOT NULL DEFAULT '',
  `sec_ccf` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='Charging Information Templates';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `charging_info`
--

LOCK TABLES `charging_info` WRITE;
/*!40000 ALTER TABLE `charging_info` DISABLE KEYS */;
INSERT INTO `charging_info` VALUES (1,'default_charging_set','','','pri_ccf_address','');
/*!40000 ALTER TABLE `charging_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cx_events`
--

DROP TABLE IF EXISTS `cx_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cx_events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `hopbyhop` bigint(20) DEFAULT NULL,
  `endtoend` bigint(20) DEFAULT NULL,
  `id_impu` int(11) DEFAULT NULL,
  `id_impi` int(11) DEFAULT NULL,
  `id_implicit_set` int(11) DEFAULT NULL,
  `type` tinyint(1) NOT NULL DEFAULT '0',
  `subtype` tinyint(4) NOT NULL DEFAULT '0',
  `grp` int(11) DEFAULT '0',
  `reason_info` varchar(255) DEFAULT '',
  `trials_cnt` int(11) DEFAULT '0',
  `diameter_name` varchar(255) DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_hopbyhop` (`hopbyhop`) USING BTREE,
  KEY `idx_endtoend` (`endtoend`),
  KEY `idx_type` (`type`),
  KEY `idx_grp` (`grp`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='Cx interface RTR and PPR events';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cx_events`
--

LOCK TABLES `cx_events` WRITE;
/*!40000 ALTER TABLE `cx_events` DISABLE KEYS */;
/*!40000 ALTER TABLE `cx_events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dsai`
--

DROP TABLE IF EXISTS `dsai`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dsai` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dsai_tag` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='DSAI table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dsai`
--

LOCK TABLES `dsai` WRITE;
/*!40000 ALTER TABLE `dsai` DISABLE KEYS */;
INSERT INTO `dsai` VALUES (1,'default_dsai');
/*!40000 ALTER TABLE `dsai` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dsai_ifc`
--

DROP TABLE IF EXISTS `dsai_ifc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dsai_ifc` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_dsai` int(11) NOT NULL DEFAULT '0',
  `id_ifc` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_id_dsai` (`id_dsai`),
  KEY `idx_id_ifc` (`id_ifc`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED COMMENT='DSAI - iFC Mappings';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dsai_ifc`
--

LOCK TABLES `dsai_ifc` WRITE;
/*!40000 ALTER TABLE `dsai_ifc` DISABLE KEYS */;
INSERT INTO `dsai_ifc` VALUES (1,1,1);
/*!40000 ALTER TABLE `dsai_ifc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dsai_impu`
--

DROP TABLE IF EXISTS `dsai_impu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dsai_impu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_dsai` int(11) NOT NULL DEFAULT '0',
  `id_impu` int(11) NOT NULL DEFAULT '0',
  `dsai_value` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_id_dsai` (`id_dsai`),
  KEY `idx_id_impu` (`id_impu`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED COMMENT='DSAI - IMPU/PSI Mappings';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dsai_impu`
--

LOCK TABLES `dsai_impu` WRITE;
/*!40000 ALTER TABLE `dsai_impu` DISABLE KEYS */;
INSERT INTO `dsai_impu` VALUES (1,1,1,0),(2,1,2,0);
/*!40000 ALTER TABLE `dsai_impu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ifc`
--

DROP TABLE IF EXISTS `ifc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ifc` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `id_application_server` int(11) DEFAULT NULL,
  `id_tp` int(11) DEFAULT NULL,
  `profile_part_ind` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='Initial Filter Criteria';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ifc`
--

LOCK TABLES `ifc` WRITE;
/*!40000 ALTER TABLE `ifc` DISABLE KEYS */;
INSERT INTO `ifc` VALUES (1,'default_ifc',1,1,-1);
/*!40000 ALTER TABLE `ifc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `impi`
--

DROP TABLE IF EXISTS `impi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `impi` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_imsu` int(11) DEFAULT NULL,
  `identity` varchar(255) NOT NULL DEFAULT '',
  `k` tinyblob NOT NULL,
  `auth_scheme` int(11) NOT NULL DEFAULT '0',
  `default_auth_scheme` int(11) NOT NULL DEFAULT '1',
  `amf` tinyblob NOT NULL,
  `op` tinyblob NOT NULL,
  `sqn` varchar(64) NOT NULL DEFAULT '000000000000',
  `ip` varchar(64) NOT NULL DEFAULT '',
  `line_identifier` varchar(64) NOT NULL DEFAULT '',
  `zh_uicc_type` int(11) DEFAULT '0',
  `zh_key_life_time` int(11) DEFAULT '3600',
  `zh_default_auth_scheme` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_identity` (`identity`),
  KEY `idx_id_imsu` (`id_imsu`)
) ENGINE=MyISAM AUTO_INCREMENT=19 DEFAULT CHARSET=utf8 COMMENT='IM Private Identities table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `impi`
--

LOCK TABLES `impi` WRITE;
/*!40000 ALTER TABLE `impi` DISABLE KEYS */;
INSERT INTO `impi` VALUES (4,1,'alice@saygreet.com','alice',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','0000000080c5','','',0,3600,1),(2,2,'bob@saygreet.com','bob',255,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','00000000a2f6','','',0,3600,1),(5,3,'1001@saygreet.com','1001',1,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000c41','','',0,3600,1),(6,4,'1002@saygreet.com','1002',1,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','0000000000e6','','',0,3600,1),(7,5,'ibm1@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000041','','',0,3600,1),(8,6,'ibm2@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000041','','',0,3600,1),(9,7,'ibm3@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000041','','',0,3600,1),(10,8,'ibm4@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000041','','',0,3600,1),(11,9,'intel1@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000000','','',0,3600,1),(12,10,'intel2@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000000','','',0,3600,1),(13,11,'intel3@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000041','','',0,3600,1),(14,12,'intel4@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000000','','',0,3600,1),(15,13,'at_t1@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000041','','',0,3600,1),(16,14,'at_t2@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000000','','',0,3600,1),(17,15,'at_t3@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000000','','',0,3600,1),(18,16,'at_t4@saygreet.com','1234',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000000','','',0,3600,1);
/*!40000 ALTER TABLE `impi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `impi_impu`
--

DROP TABLE IF EXISTS `impi_impu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `impi_impu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_impi` int(11) NOT NULL DEFAULT '0',
  `id_impu` int(11) NOT NULL DEFAULT '0',
  `user_state` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_id_impi` (`id_impi`),
  KEY `idx_id_impu` (`id_impu`)
) ENGINE=MyISAM AUTO_INCREMENT=19 DEFAULT CHARSET=utf8 COMMENT='IM Private/Public Identities Mappings';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `impi_impu`
--

LOCK TABLES `impi_impu` WRITE;
/*!40000 ALTER TABLE `impi_impu` DISABLE KEYS */;
INSERT INTO `impi_impu` VALUES (4,4,1,1),(2,2,2,2),(5,5,3,2),(6,6,4,3),(7,7,5,2),(8,8,6,2),(9,9,7,2),(10,10,8,2),(11,11,9,0),(12,12,10,0),(13,13,11,2),(14,14,12,0),(15,15,13,2),(16,16,14,0),(17,17,15,0),(18,18,16,0);
/*!40000 ALTER TABLE `impi_impu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `impu`
--

DROP TABLE IF EXISTS `impu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `impu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `identity` varchar(255) NOT NULL DEFAULT '',
  `type` tinyint(4) NOT NULL DEFAULT '0',
  `barring` tinyint(4) NOT NULL DEFAULT '0',
  `user_state` tinyint(4) NOT NULL DEFAULT '0',
  `id_sp` int(11) DEFAULT NULL,
  `id_implicit_set` int(11) NOT NULL DEFAULT '0',
  `id_charging_info` int(11) DEFAULT NULL,
  `wildcard_psi` varchar(255) NOT NULL DEFAULT '',
  `display_name` varchar(255) NOT NULL DEFAULT '',
  `psi_activation` tinyint(4) NOT NULL DEFAULT '0',
  `can_register` tinyint(4) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_identity` (`identity`),
  KEY `idx_id_impu_implicitset` (`id_implicit_set`),
  KEY `idx_type` (`type`),
  KEY `idx_sp` (`id_sp`),
  KEY `idx_wildcard_psi` (`wildcard_psi`)
) ENGINE=MyISAM AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COMMENT='IM Public Identities';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `impu`
--

LOCK TABLES `impu` WRITE;
/*!40000 ALTER TABLE `impu` DISABLE KEYS */;
INSERT INTO `impu` VALUES (1,'sip:alice@saygreet.com',0,0,1,1,1,1,'','',0,1),(2,'sip:bob@saygreet.com',0,0,2,1,2,1,'','',0,1),(3,'sip:1001@saygreet.com',0,0,2,1,3,1,'','1001',0,1),(4,'sip:1002@saygreet.com',0,0,3,1,4,1,'','1002',0,1),(5,'sip:ibm1@saygreet.com',0,0,2,1,5,1,'','',0,1),(6,'sip:ibm2@saygreet.com',0,0,2,1,6,1,'','',0,1),(7,'sip:ibm3@saygreet.com',0,0,2,1,7,1,'','',0,1),(8,'sip:ibm4@saygreet.com',0,0,2,1,8,1,'','',0,1),(9,'sip:intel1@saygreet.com',0,0,0,1,9,1,'','',0,1),(10,'sip:intel2@saygreet.com',0,0,0,1,10,1,'','',0,1),(11,'sip:intel3@saygreet.com',0,0,2,1,11,1,'','',0,1),(12,'sip:intel4@saygreet.com',0,0,0,1,12,1,'','',0,1),(13,'sip:at_t1@saygreet.com',0,0,2,1,13,1,'','',0,1),(14,'sip:at_t2@saygreet.com',0,0,0,1,14,1,'','',0,1),(15,'sip:at_t3@saygreet.com',0,0,0,1,15,1,'','',0,1),(16,'sip:at_t4@saygreet.com',0,0,0,1,16,1,'','',0,1);
/*!40000 ALTER TABLE `impu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `impu_visited_network`
--

DROP TABLE IF EXISTS `impu_visited_network`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `impu_visited_network` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_impu` int(11) NOT NULL DEFAULT '0',
  `id_visited_network` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_id_impu` (`id_impu`),
  KEY `idx_visited_network` (`id_visited_network`)
) ENGINE=MyISAM AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COMMENT='Public Identity - Visited Network mappings';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `impu_visited_network`
--

LOCK TABLES `impu_visited_network` WRITE;
/*!40000 ALTER TABLE `impu_visited_network` DISABLE KEYS */;
INSERT INTO `impu_visited_network` VALUES (1,1,1),(2,2,1),(3,3,1),(4,4,1),(5,5,1),(6,6,1),(7,7,1),(8,8,1),(9,9,1),(10,10,1),(11,11,1),(12,12,1),(13,13,1),(14,14,1),(15,15,1),(16,16,1);
/*!40000 ALTER TABLE `impu_visited_network` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `imsu`
--

DROP TABLE IF EXISTS `imsu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `imsu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `scscf_name` varchar(255) DEFAULT NULL,
  `diameter_name` varchar(255) DEFAULT NULL,
  `id_capabilities_set` int(11) DEFAULT NULL,
  `id_preferred_scscf_set` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_capabilities_set` (`id_capabilities_set`),
  KEY `idx_preferred_scscf` (`id_preferred_scscf_set`),
  KEY `idx_name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COMMENT='IMS Subscription';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `imsu`
--

LOCK TABLES `imsu` WRITE;
/*!40000 ALTER TABLE `imsu` DISABLE KEYS */;
INSERT INTO `imsu` VALUES (1,'alice','sip:scscf.saygreet.com:6060','scscf.saygreet.com',1,1),(2,'bob','sip:scscf.saygreet.com:6060','scscf.saygreet.com',1,1),(3,'1001','sip:scscf.saygreet.com:6060','scscf.saygreet.com',1,1),(4,'1002','sip:scscf.saygreet.com:6060','scscf.saygreet.com',1,1),(5,'ibm1','sip:scscf.saygreet.com:6060','scscf.saygreet.com',1,1),(6,'ibm2','sip:scscf.saygreet.com:6060','scscf.saygreet.com',1,1),(7,'ibm3','sip:scscf.saygreet.com:6060','scscf.saygreet.com',1,1),(8,'ibm4','sip:scscf.saygreet.com:6060','scscf.saygreet.com',1,1),(9,'intel1','','',1,1),(10,'intel2','','',1,1),(11,'intel3','sip:scscf.saygreet.com:6060','scscf.saygreet.com',1,1),(12,'intel4','','',1,1),(13,'at_t1','sip:scscf.saygreet.com:6060','scscf.saygreet.com',1,1),(14,'at_t2','','',1,1),(15,'at_t3','','',1,1),(16,'at_t4','','',1,1);
/*!40000 ALTER TABLE `imsu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `preferred_scscf_set`
--

DROP TABLE IF EXISTS `preferred_scscf_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `preferred_scscf_set` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_set` int(11) NOT NULL DEFAULT '0',
  `name` varchar(255) NOT NULL DEFAULT '',
  `scscf_name` varchar(255) NOT NULL DEFAULT '',
  `priority` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_priority` (`priority`),
  KEY `idx_set` (`id_set`) USING BTREE,
  KEY `idx_name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='Preferred S-CSCF sets';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `preferred_scscf_set`
--

LOCK TABLES `preferred_scscf_set` WRITE;
/*!40000 ALTER TABLE `preferred_scscf_set` DISABLE KEYS */;
INSERT INTO `preferred_scscf_set` VALUES (2,1,'scscf1','sip:scscf.saygreet.com:6060',1);
/*!40000 ALTER TABLE `preferred_scscf_set` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `repository_data`
--

DROP TABLE IF EXISTS `repository_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `repository_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sqn` int(11) NOT NULL DEFAULT '0',
  `id_impu` int(11) NOT NULL DEFAULT '0',
  `service_indication` varchar(255) NOT NULL DEFAULT '',
  `rep_data` blob,
  PRIMARY KEY (`id`),
  KEY `idx_id_impu` (`id_impu`),
  KEY `idx_sqn` (`sqn`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Repository Data';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `repository_data`
--

LOCK TABLES `repository_data` WRITE;
/*!40000 ALTER TABLE `repository_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `repository_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sh_notification`
--

DROP TABLE IF EXISTS `sh_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sh_notification` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_impu` int(11) NOT NULL DEFAULT '0',
  `id_application_server` int(11) NOT NULL DEFAULT '0',
  `data_ref` int(11) NOT NULL DEFAULT '0',
  `rep_data` blob,
  `sqn` int(11) DEFAULT '0',
  `service_indication` varchar(255) DEFAULT NULL,
  `id_ifc` int(11) DEFAULT '0',
  `server_name` varchar(255) DEFAULT NULL,
  `scscf_name` varchar(255) DEFAULT NULL,
  `reg_state` int(11) DEFAULT '0',
  `psi_activation` int(11) DEFAULT '0',
  `dsai_tag` varchar(255) DEFAULT NULL,
  `dsai_value` int(11) DEFAULT '0',
  `hopbyhop` bigint(20) DEFAULT '0',
  `endtoend` bigint(20) DEFAULT '0',
  `grp` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_id_impu` (`id_impu`),
  KEY `idx_as` (`id_application_server`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Sh Interface Notifications';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sh_notification`
--

LOCK TABLES `sh_notification` WRITE;
/*!40000 ALTER TABLE `sh_notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `sh_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sh_subscription`
--

DROP TABLE IF EXISTS `sh_subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sh_subscription` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_application_server` int(11) NOT NULL DEFAULT '0',
  `id_impu` int(11) NOT NULL,
  `data_ref` int(11) NOT NULL DEFAULT '0',
  `service_indication` varchar(255) DEFAULT NULL,
  `dsai_tag` varchar(255) DEFAULT NULL,
  `server_name` varchar(255) DEFAULT NULL,
  `expires` bigint(20) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`),
  KEY `idx_id_impu` (`id_impu`),
  KEY `idx_id_as` (`id_application_server`) USING BTREE,
  KEY `idx_expires` (`expires`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Sh Interface Subscriptions';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sh_subscription`
--

LOCK TABLES `sh_subscription` WRITE;
/*!40000 ALTER TABLE `sh_subscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `sh_subscription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shared_ifc_set`
--

DROP TABLE IF EXISTS `shared_ifc_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shared_ifc_set` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_set` int(11) NOT NULL DEFAULT '0',
  `name` varchar(255) NOT NULL DEFAULT '',
  `id_ifc` int(11) DEFAULT NULL,
  `priority` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_id_set` (`id_set`),
  KEY `idx_priority` (`priority`),
  KEY `idx_name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='Shared IFC Sets';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shared_ifc_set`
--

LOCK TABLES `shared_ifc_set` WRITE;
/*!40000 ALTER TABLE `shared_ifc_set` DISABLE KEYS */;
INSERT INTO `shared_ifc_set` VALUES (1,1,'default_shared_set',1,0);
/*!40000 ALTER TABLE `shared_ifc_set` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sp`
--

DROP TABLE IF EXISTS `sp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sp` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(16) NOT NULL DEFAULT '',
  `cn_service_auth` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='Service Profiles';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sp`
--

LOCK TABLES `sp` WRITE;
/*!40000 ALTER TABLE `sp` DISABLE KEYS */;
INSERT INTO `sp` VALUES (1,'default_sp',0);
/*!40000 ALTER TABLE `sp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sp_ifc`
--

DROP TABLE IF EXISTS `sp_ifc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sp_ifc` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_sp` int(11) NOT NULL DEFAULT '0',
  `id_ifc` int(11) NOT NULL DEFAULT '0',
  `priority` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `id_sp` (`id_sp`),
  KEY `id_ifc` (`id_ifc`),
  KEY `idx_priority` (`priority`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='Service Profile - IFC mappings';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sp_ifc`
--

LOCK TABLES `sp_ifc` WRITE;
/*!40000 ALTER TABLE `sp_ifc` DISABLE KEYS */;
INSERT INTO `sp_ifc` VALUES (1,1,1,0);
/*!40000 ALTER TABLE `sp_ifc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sp_shared_ifc_set`
--

DROP TABLE IF EXISTS `sp_shared_ifc_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sp_shared_ifc_set` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_sp` int(11) NOT NULL DEFAULT '0',
  `id_shared_ifc_set` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_id_sp` (`id_sp`),
  KEY `idx_id_shared_ifc_set` (`id_shared_ifc_set`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Service Profile - Shared IFC Sets mappings';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sp_shared_ifc_set`
--

LOCK TABLES `sp_shared_ifc_set` WRITE;
/*!40000 ALTER TABLE `sp_shared_ifc_set` DISABLE KEYS */;
/*!40000 ALTER TABLE `sp_shared_ifc_set` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spt`
--

DROP TABLE IF EXISTS `spt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spt` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_tp` int(11) NOT NULL DEFAULT '0',
  `condition_negated` int(11) NOT NULL DEFAULT '0',
  `grp` int(11) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL DEFAULT '0',
  `requesturi` varchar(255) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  `header` varchar(255) DEFAULT NULL,
  `header_content` varchar(255) DEFAULT NULL,
  `session_case` int(11) DEFAULT NULL,
  `sdp_line` varchar(255) DEFAULT NULL,
  `sdp_line_content` varchar(255) DEFAULT NULL,
  `registration_type` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_trigger_point` (`id_tp`),
  KEY `idx_grp` (`grp`)
) ENGINE=MyISAM AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 COMMENT='Service Point Trigger';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spt`
--

LOCK TABLES `spt` WRITE;
/*!40000 ALTER TABLE `spt` DISABLE KEYS */;
INSERT INTO `spt` VALUES (2,1,0,0,1,NULL,'REGISTER',NULL,NULL,NULL,NULL,NULL,0),(10,1,0,1,1,NULL,'MESSAGE',NULL,NULL,NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `spt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tp`
--

DROP TABLE IF EXISTS `tp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tp` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `condition_type_cnf` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='Trigger Points';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tp`
--

LOCK TABLES `tp` WRITE;
/*!40000 ALTER TABLE `tp` DISABLE KEYS */;
INSERT INTO `tp` VALUES (1,'default_tp',0);
/*!40000 ALTER TABLE `tp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visited_network`
--

DROP TABLE IF EXISTS `visited_network`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `visited_network` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `identity` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_identity` (`identity`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='Visited Networks';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visited_network`
--

LOCK TABLES `visited_network` WRITE;
/*!40000 ALTER TABLE `visited_network` DISABLE KEYS */;
INSERT INTO `visited_network` VALUES (1,'saygreet.com');
/*!40000 ALTER TABLE `visited_network` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `zh_uss`
--

DROP TABLE IF EXISTS `zh_uss`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zh_uss` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_impi` int(11) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL DEFAULT '1',
  `flags` int(11) NOT NULL DEFAULT '0',
  `naf_group` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_impi` (`id_impi`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='Zh-User Security Settings';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zh_uss`
--

LOCK TABLES `zh_uss` WRITE;
/*!40000 ALTER TABLE `zh_uss` DISABLE KEYS */;
INSERT INTO `zh_uss` VALUES (4,1,0,0,NULL);
/*!40000 ALTER TABLE `zh_uss` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-06-23  3:50:40
