use auth_server;
# INSERT INTO tbl_client (client_id, access_token_validity_seconds, client_secret, grant_type,
#                         refresh_token_validity_seconds) VALUES ('b2b-client-id', 8000, 'rGI8Sc6OfZO6uNugczscM2F46imUl4ux4KhqWDAndAM=', 'password', 7000);
# INSERT INTO tbl_client (client_id, access_token_validity_seconds, client_secret, grant_type,
#                         refresh_token_validity_seconds) VALUES ('int-road-client-id', 9000, 'R/1cn9aHrKQuK4EQ/vamqCYo3Gjs1yvveKX0ohLml6Q=', 'password', 8000);
INSERT INTO tbl_client (client_id, access_token_validity_seconds, client_secret, grant_type,
                        refresh_token_validity_seconds) VALUES ('portal-client-id', 6000, 'portal-client-secret', 'password', 6000);

INSERT INTO tbl_user (id, creation_date, deleted, enabled, first_name, last_name, modified_date,
                                         password, username, client_id, pass_expiration_date, last_login_attempt_time,
                                         block_date, block_count, super_admin) VALUES (1, now(), false,
                                                                                       true, 'admin', 'admin', null,
                                                                                       '1', 'admin',
                                                                                       'portal-client-id', null,
                                                                                       now(), null,
                                                                                       null, true);
INSERT INTO tbl_user (id, creation_date, deleted, enabled, first_name, last_name, modified_date,
                                         password, username, client_id, pass_expiration_date, last_login_attempt_time,
                                         block_date, block_count, super_admin) VALUES (2, now(), false,
                                                                                       true, 'user', 'user',
                                                                                       null, '1',
                                                                                       'user', 'portal-client-id',
                                                                                       null, null,
                                                                                       null, null, false);
# INSERT INTO tbl_user (id, creation_date, deleted, enabled, first_name, last_name, modified_date,
#                                          password, username, client_id, pass_expiration_date, last_login_attempt_time,
#                                          block_date, block_count, super_admin) VALUES (3, now(), false,
#                                                                                        true, 'int-road-user',
#                                                                                        'int-road-user', null,
#                                                                                        'rz4PbEOID0frxyxiyyjClgJ8EBTQrEFuBnamAvhOGeE=',
#                                                                                        'int-road-user',
#                                                                                        'int-road-client-id', null,
#                                                                                        null, null,
#                                                                                        null, true);

