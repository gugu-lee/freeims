delete from `impi_impu`;
delete from `impi_impu`;
delete from `preferred_scscf_set`;
delete from `impu_visited_network`;
delete from `visited_network`;
delete from `imsu`;
delete from `impi`;
delete from `impu` ;
delete from `impu_visited_network`;
INSERT INTO `visited_network` VALUES (2,'imsclouds.com');
INSERT INTO `imsu` VALUES (3,'alice','','',1,1),(4,'bob','','',1,1);

INSERT INTO `impi` VALUES \
(5,3,'alice@imsclouds.com','alice',127,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000000','','',0,3600,1,''),\
(6,4,'bob@imsclouds.com','bob',255,1,'\0\0','\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0','000000000000','','',0,3600,1,'');

INSERT INTO `impu` VALUES (3,'sip:alice@imsclouds.com',0,0,0,1,1,1,'','',0,1),(4,'sip:bob@imsclouds.com',0,0,0,1,2,1,'','',0,1);

INSERT INTO `impu_visited_network` VALUES (3,3,2),(4,4,2);

INSERT INTO `impi_impu` VALUES(6,6,4,1);
INSERT INTO `impi_impu` VALUES(5,5,3,1);

INSERT INTO `preferred_scscf_set` VALUES (2,1,'scscf1','sip:scscf.imsclouds.com:6060',0);
