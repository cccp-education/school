CREATE TABLE IF NOT EXISTS `user` (
   `id`                     UUID default random_uuid() PRIMARY KEY,
   `login`                  VARCHAR,
   `password`               VARCHAR,
   `email`                  VARCHAR,
   `lang_key`               VARCHAR,
   `version`                bigint
    );
CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_login`
    ON `user` (`login`);
CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_email`
    ON `user` (`email`);

CREATE TABLE IF NOT EXISTS `authority`(
                                          `role` VARCHAR(50) PRIMARY KEY);
MERGE INTO `authority`
VALUES ('ADMIN'),
       ('USER'),
       ('ANONYMOUS');

CREATE TABLE IF NOT EXISTS `user_authority`(
                                               `id`         IDENTITY NOT NULL PRIMARY KEY,
                                               `user_id`    UUID,
                                               `role`       VARCHAR,
                                               FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    FOREIGN KEY (`role`) REFERENCES `authority` (`role`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
    );

CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_authority`
    ON `user_authority` (`role`, `user_id`);

CREATE TABLE IF NOT EXISTS `user_activation` (
                                                 `id`                     UUID PRIMARY KEY,
                                                 `activation_key`         VARCHAR,
                                                 `activation_date`        datetime,
                                                 `created_date`           datetime,
                                                 FOREIGN KEY (`id`) REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);