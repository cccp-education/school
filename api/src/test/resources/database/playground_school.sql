select * from "authority";
select count(*) from "user";
select * from "user";
select count(*) from user_authority;

SELECT
    u.*,
    array_agg(DISTINCT a.role) AS user_roles
FROM
    "user" u
        LEFT JOIN
    user_authority ua ON u.id = ua.user_id
        LEFT JOIN
    authority a ON ua.role = a.role
WHERE
    lower(u.email) = lower('user@acme.com') OR lower(u.login) = lower('user@acme.com')
GROUP BY
    u.id, u.email, u.login;
