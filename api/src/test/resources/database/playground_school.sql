select * from "authority";
select count(*) from "user";
select * from "user";
select count(*) from user_authority;

select u.id, ua.role from "user" as u , user_authority as ua;