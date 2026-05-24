insert into usrs (first_name, last_name, username, registration_timestamp)
values
    ('Admin', 'User', 'admin', current_timestamp);

insert into auth_users (user_id, username, password_hash)
select id, 'admin', '$2y$10$xlsrQGRmTn.nYxxq3OntGOI2cIJKUZuiM2xIemdEVe.3DML.69Hna'
from usrs
where username = 'admin';

insert into auth_user_roles (user_id, role)
select user_id, 'USER'
from auth_users
where username = 'admin';

insert into auth_user_roles (user_id, role)
select user_id, 'ADMIN'
from auth_users
where username = 'admin';
