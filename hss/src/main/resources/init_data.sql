
INSERT INTO `application_server` VALUES (1,'default_as','sip:127.0.0.1:5065',0,'','presence.open-ims.test',1024,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);

INSERT INTO `capabilities_set` VALUES (2,1,'cap_set1',1,0);

INSERT INTO `capability` VALUES (1,'cap1'),(2,'cap2');

INSERT INTO `charging_info` VALUES (1,'default_charging_set','','','pri_ccf_address','');

INSERT INTO `dsai` VALUES (1,'default_dsai');

INSERT INTO `dsai_ifc` VALUES (1,1,1);

INSERT INTO `dsai_impu` VALUES (1,1,1,0),(2,1,2,0);

INSERT INTO `ifc` VALUES (1,'default_ifc',1,1,-1);

INSERT INTO `impi` VALUES (4,1,'alice@open-ims.test','alice',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000000','','',0,3600,1),(2,2,'bob@open-ims.test','bob',255,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000000','','',0,3600,1);

INSERT INTO `impi_impu` VALUES (4,4,1,0),(2,2,2,0);

INSERT INTO `impu` VALUES (1,'sip:alice@open-ims.test',0,0,0,1,1,1,'','',0,1),(2,'sip:bob@open-ims.test',0,0,0,1,2,1,'','',0,1);

INSERT INTO `impu_visited_network` VALUES (1,1,1),(2,2,1);

INSERT INTO `imsu` VALUES (1,'alice','','',1,1),(2,'bob','','',1,1);

INSERT INTO `preferred_scscf_set` VALUES (1,1,'scscf1','sip:scscf.open-ims.test:6060',0);


INSERT INTO `shared_ifc_set` VALUES (1,1,'default_shared_set',1,0);

INSERT INTO `sp` VALUES (1,'default_sp',0);

INSERT INTO `sp_ifc` VALUES (1,1,1,0);

INSERT INTO `spt` VALUES (2,1,0,0,1,NULL,'PUBLISH',NULL,NULL,NULL,NULL,NULL,0),(5,1,0,0,2,NULL,NULL,'Event','.*presence.*',NULL,NULL,NULL,0),(7,1,0,1,1,NULL,'PUBLISH',NULL,NULL,NULL,NULL,NULL,0),(6,1,0,0,3,NULL,NULL,NULL,NULL,0,NULL,NULL,0),(8,1,0,1,2,NULL,NULL,'Event','.*presence.*',NULL,NULL,NULL,0),(9,1,0,1,3,NULL,NULL,NULL,NULL,3,NULL,NULL,0),(10,1,0,2,1,NULL,'SUBSCRIBE',NULL,NULL,NULL,NULL,NULL,0),(11,1,0,2,2,NULL,NULL,'Event','.*presence.*',NULL,NULL,NULL,0),(12,1,0,2,3,NULL,NULL,NULL,NULL,1,NULL,NULL,0),(13,1,0,3,1,NULL,'SUBSCRIBE',NULL,NULL,NULL,NULL,NULL,0),(14,1,0,3,2,NULL,NULL,'Event','.*presence.*',NULL,NULL,NULL,0),(15,1,0,3,3,NULL,NULL,NULL,NULL,2,NULL,NULL,0);

INSERT INTO `tp` VALUES (1,'default_tp',0);

INSERT INTO `visited_network` VALUES (1,'open-ims.test');

INSERT INTO `zh_uss` VALUES (4,1,0,0,NULL);
