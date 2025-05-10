drop table if exists category;
drop table if exists beer_category;

CREATE TABLE category
(
    id          varchar(36) NOT NULL PRIMARY KEY,
    description varchar(50),
    create_date timestamp,
    update_date datetime(6) DEFAULT NULL,
    version     bigint      DEFAULT NULL
) ENGINE = InnoDB;

CREATE TABLE `beer_category`
(
    beer_id     varchar(36) NOT NULL,
    category_id varchar(36) NOT NULL,
    PRIMARY KEY (beer_id, category_id),
    CONSTRAINT pc_beer_id_fk FOREIGN KEY (beer_id) REFERENCES beer (id),
    CONSTRAINT pc_category_id_fk FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE = InnoDB;