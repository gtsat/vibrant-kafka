DROP SCHEMA IF EXISTS vibrant;
CREATE SCHEMA vibrant;
USE vibrant;

DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS similarities;

CREATE TABLE accounts (
	username VARCHAR(100) NOT NULL,
	password VARCHAR(100) NOT NULL,
	firstname VARCHAR(100) NOT NULL,
	lastname VARCHAR(100) NOT NULL,
	msisdn VARCHAR(100) NOT NULL,
	email VARCHAR(100) NOT NULL,
	creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_usage TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  status BIT(1) DEFAULT 1,
  admin BIT(1) DEFAULT 0,
  region VARCHAR(4000),
  PRIMARY KEY (username)
);

CREATE TABLE events (
	offset BIGINT NOT NULL,
	producer VARCHAR(256) NOT NULL,
	creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	similarity INT NOT NULL,
  PRIMARY KEY (offset)
);

CREATE INDEX events_producer_index ON events (producer);

CREATE TABLE benchmarks (
	creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	producer VARCHAR(256) NOT NULL,
	frequency FLOAT NOT NULL,
	sample LONGTEXT NOT NULL,
  PRIMARY KEY (producer)
);


COMMIT;

INSERT INTO accounts VALUES ('gtsat','202cb962ac59075b964b07152d234b70','George','Tsatsanifos','6949290888','gtsatsanifos@gmail.com',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,1,1,'Greece');
COMMIT;
