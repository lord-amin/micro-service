ALTER TABLE tbl_permission ADD service_name VARCHAR(50) NULL;
create table tbl_permission_url
(
	id bigint not null auto_increment
		primary key,
	method varchar(255) null,
	url varchar(255) null,
	permission_id bigint not null,
	constraint `foreign_key_url_to_permission`
		foreign key (permission_id) references tbl_permission (id)
);

create index `foreign_key_url_to_permission`
	on tbl_permission_url (permission_id);

CREATE UNIQUE INDEX tbl_permission_permission_uindex ON auth_server_unit.tbl_permission (permission);
delete from tbl_user_role where user_id in (1,2);
delete from tbl_role_permission where role_id=1;
delete from tbl_user where id=2;
delete from tbl_client where client_id='test-client-id';
