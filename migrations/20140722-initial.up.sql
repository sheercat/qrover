CREATE TABLE users (
`id` int(10) unsigned NOT NULL AUTO_INCREMENT,
email VARCHAR(128) NOT NULL,
pass VARCHAR(100) NOT NULL,
admin BOOLEAN,
is_active BOOLEAN,
meta_data text,
created_at datetime,
updated_at TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=compressed KEY_BLOCK_SIZE=4;

CREATE TABLE types (
`id` int(10) unsigned NOT NULL AUTO_INCREMENT,
`type` VARCHAR(16) NOT NULL,
`name` VARCHAR(128) NOT NULL,
users_id INT(10) unsigned NOT NULL,
is_active BOOLEAN,
meta_data text,
created_at datetime,
updated_at TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `type` (`type`),
KEY `created_at` (`created_at`),
KEY `updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=compressed KEY_BLOCK_SIZE=4;

CREATE TABLE domains (
`id` int(10) unsigned NOT NULL AUTO_INCREMENT,
`domain` VARCHAR(64) NOT NULL,
users_id INT(10) unsigned NOT NULL,
is_active BOOLEAN,
meta_data text,
created_at datetime,
updated_at TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `domain` (`domain`),
KEY `created_at` (`created_at`),
KEY `updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=compressed KEY_BLOCK_SIZE=4;

CREATE TABLE qrcodes (
`id` int(10) unsigned NOT NULL AUTO_INCREMENT,
code VARCHAR(128) NOT NULL,
`types_id` INT(10) unsigned NOT NULL,
`domains_id` INT(10) unsigned NOT NULL,
users_id INT(10) unsigned NOT NULL,
is_active BOOLEAN,
meta_data text,
created_at datetime,
updated_at TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `code` (`code`),
KEY `created_at` (`created_at`),
KEY `updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=compressed KEY_BLOCK_SIZE=4;

CREATE TABLE emails (
`id` int(10) unsigned NOT NULL AUTO_INCREMENT,
email VARCHAR(128) NOT NULL,
qrcodes_id int(10) unsigned DEFAULT NULL,
`types_id` INT(10) unsigned DEFAULT NULL,
meta_data text,
created_at datetime,
updated_at TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `code_email` (`qrcodes_id`, `email`),
KEY `email` (`email`),
KEY `qrcodes_id` (`qrcodes_id`),
KEY `created_at` (`created_at`),
KEY `updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=compressed KEY_BLOCK_SIZE=4;
