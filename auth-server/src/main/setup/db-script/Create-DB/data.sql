INSERT INTO tbl_client (client_id, client_secret, grant_type, access_token_validity_seconds, refresh_token_validity_seconds)
VALUES
  ('portal-client-id', 'portal-client-secret', 'password', 6000, 6000);

INSERT INTO tbl_user (id, first_name, last_name, username, password, creation_date, modified_date, enabled, deleted, client_id, super_admin)
VALUES
  (1, 'admin', 'admin', 'admin', '1', now(), NULL, TRUE, FALSE, 'portal-client-id', 1);

INSERT INTO tbl_role (id, description, name) VALUES
  (1, 'admin', 'admin');

