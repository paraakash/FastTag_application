-- Script: create_service_authentication.sql
-- Creates service_authentication table with composite primary key

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE service_authentication PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN
      RAISE;
    END IF;
END;
/

CREATE TABLE service_authentication (
  Tollplaza_name     VARCHAR2(255),
  UserName           VARCHAR2(255),
  encrypted_password VARCHAR2(255),
  salt               VARCHAR2(255),
  status             VARCHAR2(10),
  ceated_dt          DATE,
  modified_dt        DATE,
  token_string       VARCHAR2(255),
  token_expiry       TIMESTAMP,
  CONSTRAINT pk_service_authentication PRIMARY KEY (Tollplaza_name, UserName)
);

COMMIT;
