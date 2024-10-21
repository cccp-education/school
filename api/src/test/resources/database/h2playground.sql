INSERT INTO `user` (login, password, email, lang_key, version)
VALUES ('user', 'user', 'user@acme.com', 'fr', '0'),
       ('admin', 'admin', 'admin@acme.com', 'en', '0');

insert into `user_authority` (user_id, role)
select u.id, 'USER'
from `user` as u
where u.login = 'user';

insert into `user_authority` (user_id, role)
select u.id, 'USER'
from `user` as u
where u.login = 'admin';

insert into `user_authority` (user_id, role)
select u.id, 'ADMIN'
from `user` as u
where u.login = 'admin';

select count(*) from `USER`;

SELECT
    u.id,
    u.email,
    u.login,
    u.password,
    u.lang_key,
    u.version,
    GROUP_CONCAT(DISTINCT a.role) AS user_roles
FROM `user` as u
    LEFT JOIN
    user_authority ua ON u.id = ua.user_id
    LEFT JOIN
    `authority` as a ON ua.role = a.role
WHERE
    lower(u.email) = lower('user') OR lower(u.login) = lower('user')
GROUP BY
    u.id, u.email, u.login;


SELECT
    u.id,
    u.email,
    u.login,
    u.password,
    u.lang_key,
    u.version,
    GROUP_CONCAT(DISTINCT a.role) AS user_roles
FROM `user` as u
         LEFT JOIN
     user_authority ua ON u.id = ua.user_id
         LEFT JOIN
     `authority` as a ON ua.role = a.role
WHERE
    lower(u.email) = lower('admin') OR lower(u.login) = lower('admin')
GROUP BY
    u.id, u.email, u.login;


