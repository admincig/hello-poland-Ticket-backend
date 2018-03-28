CREATE TABLE sights
(
  sight_id                 SERIAL       NOT NULL
    CONSTRAINT sights_pkey
    PRIMARY KEY,
  available_tickets_number INTEGER,
  description              VARCHAR(255),
  email                    VARCHAR(255),
  lead                     VARCHAR(255),
  main_image_url           VARCHAR(255),
  name                     VARCHAR(255) NOT NULL
    CONSTRAINT sights_name_key
    UNIQUE,
  phone                    VARCHAR(255),
  city                     VARCHAR(255),
  country                  VARCHAR(255),
  latitude                 DOUBLE PRECISION,
  longitude                DOUBLE PRECISION,
  street                   VARCHAR(255),
  zip_code                 VARCHAR(255)
);

CREATE TABLE ticket_definitions
(
  ticket_definition_id SERIAL       NOT NULL
    CONSTRAINT ticket_definitions_pkey
    PRIMARY KEY,
  date                 TIMESTAMP,
  name                 VARCHAR(255) NOT NULL,
  predefined_date      BOOLEAN      NOT NULL,
  price                INTEGER      NOT NULL,
  sight_id             BIGINT       NOT NULL
    CONSTRAINT fk_ticket_definitions_sight_id
    REFERENCES sights
);

CREATE TABLE tickets
(
  ticket_id     SERIAL       NOT NULL
    CONSTRAINT tickets_pkey
    PRIMARY KEY,
  date          TIMESTAMP,
  name          VARCHAR(255) NOT NULL,
  price         INTEGER      NOT NULL,
  serial_number BIGINT       NOT NULL,
  status        VARCHAR(255) NOT NULL,
  sight_id      BIGINT       NOT NULL
    CONSTRAINT fk_tickets_sight_id
    REFERENCES sights
);

INSERT INTO sights (sight_id, name, available_tickets_number) VALUES
(1,'Kolejkowo',5),
(2,'Zoo',3);

INSERT INTO ticket_definitions (ticket_definition_id, name, price, predefined_date, date, sight_id) VALUES
(1, 5,'A ticket to the Kolejkowo',1),
(2,3,'A ticket to the Zoo',2);

INSERT INTO tickets (ticket_id, sight_id, name, price, date, status, serial_number) VALUES
(1,1,'A ticket to the Kolejkowo',20000, '2018-03-28 11:48:20.775000', 'BOOKED', 3580378235539964975),
(2,2,'A ticket to the Zoo',20000, '2018-03-28 11:48:20.775000', 'BOOKED',7269114489581553309);