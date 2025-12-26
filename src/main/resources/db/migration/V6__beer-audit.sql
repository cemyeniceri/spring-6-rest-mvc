drop table if exists beer_audit;

create table beer_audit
(
    beer_style        smallint       not null check (beer_style between 0 and 8),
    price             decimal(38, 2) not null,
    quantity_on_hand  integer,
    version           integer,
    create_date       datetime(6),
    update_date       datetime(6),
    create_date_audit datetime(6),
    audit_id           varchar(36)    not null,
    id                varchar(36)    not null,
    beer_name         varchar(50)    not null,
    principal_name    varchar(255)   not null,
    audit_event_type  varchar(255)   not null,
    upc               varchar(255)   not null,
    primary key (audit_id)
) engine = InnoDB;