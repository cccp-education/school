DROP TABLE IF EXISTS `user_authority`;
--
DROP TABLE IF EXISTS `user`;
--
DROP TABLE IF EXISTS `authority`;
--
CREATE TABLE IF NOT EXISTS `authority` ( `role` VARCHAR(50) PRIMARY KEY);
--
MERGE INTO `authority`(`role`) VALUES ('ADMIN'), ('USER'), ('ANONYMOUS');
--
-- CREATE TABLE IF NOT EXISTS `telephone` (
--     id                 UUID default random_uuid() PRIMARY KEY,
--     `value`            VARCHAR
-- );

--
CREATE TABLE IF NOT EXISTS `user` (
    `id`                 UUID default random_uuid() PRIMARY KEY,
    `login`            VARCHAR,
    `password`         VARCHAR,
    `first_name`         VARCHAR,
    `last_name`          VARCHAR,
    `created_by`         VARCHAR,
    `created_date`       datetime,
    `last_modified_by`   VARCHAR,
    `last_modified_date` datetime,
    `email`            VARCHAR,
    `activated`          boolean,
    `lang_key`           VARCHAR,
    `image_url`          VARCHAR,
    `activation_key`     VARCHAR,
    `reset_key`          VARCHAR,
    `reset_date`         datetime,
    `version`          bigint
);
--
CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_login`
ON `user` (`login`);
CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_email`
ON `user` (`email`);
-- CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_activation_key`
-- ON `user` (`activation_key`);
-- CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_reset_key`
-- ON `user` (`reset_key`);
--

CREATE TABLE IF NOT EXISTS `user_authority`
(
    `id`      IDENTITY NOT NULL PRIMARY KEY,
    `user_id` UUID,
    `role`  VARCHAR,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (`role`) REFERENCES `authority` (`role`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
--
CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_authority`
ON `user_authority` (`role`, `user_id`);

-- insert into `user_authority` (`user_id`, `role`) values ('3c65bfc9-f933-4eea-ab80-c7bc3e713060', 'USER');
--
-- CREATE TABLE IF NOT EXISTS `user_reset` (
--     `id`                      UUID default random_uuid() PRIMARY KEY,
--     `user_id`                 UUID,
--     `reset_key`             VARCHAR(20),
--     `reset_date`            datetime,
--     FOREIGN KEY (user_id)   REFERENCES `user` (id)
--                             ON DELETE CASCADE
--                             ON UPDATE CASCADE
-- );
--
-- CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_reset_key` ON `user_reset` (`reset_key`);
--
-- CREATE TABLE IF NOT EXISTS `user_email`
-- (
--     `id` UUID default random_uuid() PRIMARY KEY,
--     `user_id` UUID,
--     `email` varchar(255),
--     `date` datetime
--     FOREIGN KEY (`user_id`)   REFERENCES `user` (`id`)
--     ON DELETE CASCADE
--     ON UPDATE CASCADE
-- );
--
-- CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_email` ON `user_email` (`email`);
--
-- data class UserActivation(
--     val id: UUID,
--     val userId: UUID,
--     @JsonIgnore
--     @field:Size(max = 20)
--     val activationKey: String,
--     val activated: Boolean,
--     val activationDate: Instant,
--     @JsonIgnore
--     val createdDate: Instant,
--     @JsonIgnore
--     val createdBy: UUID,
-- )
--
-- data class UserProfile(
--     @field:Size(max = 50)
--     val firstName: String = EMPTY_STRING,
--     @field:Size(max = 50)
--     val lastName: String = EMPTY_STRING,
--     @field:Size(max = 256)
--     val imageUrl: String? = null,
-- )
--
-- data class UserEnabled(
--     val id: UUID,
--     val userId: UUID,
--     val modifiedBy: UUID,
--     val date: Instant,
-- )
--
-- data class UserModification(
--     val id: UUID,
--     val userId: UUID,
--     val modifiedBy: UUID,
--     val date: Instant,
--     val reason: String,
-- )
--
-- data class UserPassword(
--     val id: UUID,
--     val userId: UUID,
--     val modifiedBy: UUID,
--     val date: Instant,
--     val current: String,
-- )

--

-- CREATE TABLE IF NOT EXISTS artist
-- (
--     id         UUID default random_uuid() PRIMARY KEY,
--     login      varchar(255) NOT NULL,
--     name       varchar(255),
--     first_name varchar(255),
--     last_name  varchar(255)
-- );
-- --
-- MERGE INTO artist (id, login, name, first_name, last_name)
--     VALUES (random_uuid(), 'karl.marx', 'Karl Marx', 'Karl', 'Marx'),
--            (random_uuid(), 'jean-jacques.rousseau', 'Jean-Jacques Rousseau', 'Jean-Jacques', 'Rousseau'),
--            (random_uuid(), 'victor.hugo', 'Victor Hugo', 'Victor', 'Hugo'),
--            (random_uuid(), 'platon', 'Platon', null, null),
--            (random_uuid(), 'rene.descartes', 'René Descartes', 'René', 'Descartes'),
--            (random_uuid(), 'socrate', 'Socrate', null, null),
--            (random_uuid(), 'homere', 'Homère', null, null),
--            (random_uuid(), 'paul.verlaine', 'Paul Verlaine', 'Paul', 'Verlaine'),
--            (random_uuid(), 'claude.roy', 'Claude Roy', 'Claude', 'Roy'),
--            (random_uuid(), 'bernard.friot', 'Bernard Friot', 'Bernard', 'Friot'),
--            (random_uuid(), 'francois.begaudeau', 'François Bégaudeau', 'François', 'Bégaudeau'),
--            (random_uuid(), 'frederic.lordon', 'Frederic Lordon', 'Frederic', 'Lordon'),
--            (random_uuid(), 'antonio.gramsci', 'Antonio Gramsci', 'Antonio', 'Gramsci'),
--            (random_uuid(), 'georg.lukacs', 'Georg Lukacs', 'Georg', 'Lukacs'),
--            (random_uuid(), 'franz.kafka', 'Franz Kafka', 'Franz', 'Kafka'),
--            (random_uuid(), 'arthur.rimbaud', 'Arthur Rimbaud', 'Arthur', 'Rimbaud'),
--            (random_uuid(), 'gerard.de.nerval', 'Gérard de Nerval', 'Gérard', 'de Nerval'),
--            (random_uuid(), 'paul.verlaine', 'Paul Verlaine', 'Paul', 'Verlaine'),
--            (random_uuid(), 'dominique.pagani', 'Dominique Pagani', 'Dominique', 'Pagani'),
--            (random_uuid(), 'roce', 'Rocé', 'José Youcef Lamine', 'Kaminsky'),
--            (random_uuid(), 'chretien.de.troyes', 'Chrétien de Troyes', 'Chrétien', 'de Troyes'),
--            (random_uuid(), 'francois.rabelais', 'François Rabelais', 'François', 'Rabelais'),
--            (random_uuid(), 'montesquieu', 'Montesquieu', 'Charles Louis', 'de Secondat'),
--            (random_uuid(), 'georg.hegel', 'Georg Hegel', 'Georg', 'Hegel'),
--            (random_uuid(), 'friedrich.engels', 'Friedrich Engels', 'Friedrich', 'Engels'),
--            (random_uuid(), 'voltaire', 'Voltaire', 'François-Marie', 'Arouet'),
--            (random_uuid(), 'michel.clouscard', 'Michel Clouscard', 'Michel', 'Clouscard');
-- --
