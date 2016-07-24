delete from application_server;

update impi set identity=REPLACE(identity,'open-ims.test','imsclouds.com');
update impu  set identity=REPLACE(identity,'open-ims.test','imsclouds.com');
update preferred_scscf_set set scscf_name=REPLACE(scscf_name,'open-ims.test','imsclouds.com');
update visited_network set identity='imsclouds.com';