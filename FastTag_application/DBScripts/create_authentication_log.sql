-- Script: create_authentication_log.sql
-- Creates authentication_log table, sequence, trigger, and partitioning by Login_date_time (range partitions per year)

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE authentication_log PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN
      RAISE;
    END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE auth_log_seq';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -2289 THEN
      RAISE;
    END IF;
END;
/

CREATE SEQUENCE auth_log_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE authentication_log (
  auth_log_id           NUMBER,
  Tollplaza_name        VARCHAR2(255),
  UserName              VARCHAR2(255),
  Login_date_time       TIMESTAMP,
  authentication_status VARCHAR2(20),
  ip_address            VARCHAR2(255),
  CONSTRAINT pk_authentication_log PRIMARY KEY (auth_log_id)
)
PARTITION BY RANGE (CAST(Login_date_time AS DATE)) (
  PARTITION p_before_2020 VALUES LESS THAN (TO_DATE('01-JAN-2020','DD-MON-YYYY')),
  PARTITION p_2020 VALUES LESS THAN (TO_DATE('01-JAN-2021','DD-MON-YYYY')),
  PARTITION p_2021 VALUES LESS THAN (TO_DATE('01-JAN-2022','DD-MON-YYYY')),
  PARTITION p_2022 VALUES LESS THAN (TO_DATE('01-JAN-2023','DD-MON-YYYY')),
  PARTITION p_max VALUES LESS THAN (MAXVALUE)
);

-- Trigger to populate auth_log_id using sequence
CREATE OR REPLACE TRIGGER trg_auth_log_id
BEFORE INSERT ON authentication_log
FOR EACH ROW
BEGIN
  IF :NEW.auth_log_id IS NULL THEN
    SELECT auth_log_seq.NEXTVAL INTO :NEW.auth_log_id FROM dual;
  END IF;
END;
/

COMMIT;
