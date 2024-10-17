-- Insertion de 100 utilisateurs
WITH inserted_users AS (
    INSERT INTO "user" (login, password, email, lang_key, version)
        SELECT 'user' || generate_series || '_' || substr(md5(random()::text), 1, 6),
               'password' || generate_series, -- Mot de passe simple pour test
               'user' || generate_series || '_' || substr(md5(random()::text), 1, 6) || '@example.com',
               CASE (random() * 2)::integer
                   WHEN 0 THEN 'fr'
                   WHEN 1 THEN 'en'
                   ELSE 'es'
                   END,
               1
        FROM generate_series(1, 100)
        RETURNING id)
-- Attribution du rôle USER à chaque utilisateur créé
INSERT
INTO user_authority (user_id, role)
SELECT id, 'USER'
FROM inserted_users;

-- Vérification du nombre d'utilisateurs créés
SELECT COUNT(*) as users_created
FROM "user";

-- Vérification du nombre d'associations user-authority créées
SELECT COUNT(*) as user_authorities_created
FROM user_authority
WHERE role = 'USER';

INSERT INTO "user" (login, password, email, lang_key, version)
VALUES ('user', 'user', 'user@acme.com', 'fr', '0'),
       ('admin', 'admin', 'admin@acme.com', 'en', '0');

insert into "user_authority" (user_id, role)
select u.id, 'USER'
from "user" as u
where u.login = 'user';

insert into "user_authority" (user_id, role)
select u.id, 'USER'
from "user" as u
where u.login = 'admin';

insert into "user_authority" (user_id, role)
select u.id, 'ADMIN'
from "user" as u
where u.login = 'admin';

-- values (select u.id from "user" as u where u.login,'USER');