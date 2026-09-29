-- Script: create_toll_master.sql
-- Creates toll_master table with composite primary key on Tollplaza_name and vehicle_type

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE toll_master PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN
      RAISE;
    END IF;
END;
/

CREATE TABLE toll_master (
  Tollplaza_name VARCHAR2(255),
  vehicle_type   VARCHAR2(50),
  toll_amt       NUMBER(10),
  ceated_dt                 DATE,
  modified_dt               DATE,
  CONSTRAINT pk_toll_master PRIMARY KEY (Tollplaza_name, vehicle_type)
);

COMMIT;
