drop table if exists beer_order_shipment;

CREATE TABLE `beer_order_shipment`
(
    id              varchar(36) NOT NULL PRIMARY KEY,
    beer_order_id   varchar(36) UNIQUE,
    tracking_number varchar(50),
    create_date     timestamp,
    update_date     datetime(6) DEFAULT NULL,
    version         bigint      DEFAULT NULL,
    CONSTRAINT bos_pk FOREIGN KEY (beer_order_id) REFERENCES beer_order (id)
) ENGINE = InnoDB;

ALTER TABLE beer_order
    ADD COLUMN beer_order_shipment_id VARCHAR(36);

ALTER TABLE beer_order
    ADD CONSTRAINT bos_shipment_fk FOREIGN KEY (beer_order_shipment_id) REFERENCES beer_order_shipment (id)
