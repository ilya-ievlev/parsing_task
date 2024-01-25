create table if not exists jobs
(
    vacancy_id_from_site bigint not null primary key auto_increment unique,
    position_name        varchar(500),
    job_page_url         varchar(500),
    organization_url     varchar(500),
    logo_link            varchar(500),
    organization_title   varchar(500),
    labor_function       varchar(500),
    location             varchar(500),
    posted_date          bigint,
    description          TEXT,
    tags_names           varchar(5000)
)