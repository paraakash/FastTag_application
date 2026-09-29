
-- Script: create_transaction_balance.sql
-- Creates transaction_balance table partitioned by first 4 chars of Vehocle_registration_nbr
-- (first 2 chars = state code, next 2 chars = district code). Uses RANGE partitions
-- on the 4-char prefix. Adjust partition cutoffs as needed for your state/district map.

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE transaction_balance PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN
      RAISE;
    END IF;
END;
/

-- Using virtual column for 4-char prefix and RANGE partitioning on it
CREATE TABLE transaction_balance (
  Vehicle_registration_nbr VARCHAR2(50),
  fast_tag_id               VARCHAR2(50),
  vehicle_type              VARCHAR2(50),
  currnet_bal_amt           NUMBER(10),
  ceated_dt                 DATE,
  modified_dt               DATE,
  reg_prefix                VARCHAR2(4) GENERATED ALWAYS AS (SUBSTR(Vehocle_registration_nbr,1,4)) VIRTUAL,
  CONSTRAINT pk_transaction_balance PRIMARY KEY (Vehocle_registration_nbr, fast_tag_id)
)
PARTITION BY LIST (reg_prefix) (
  PARTITION tb_A_F  VALUES ('MH01','MH02'),
  PARTITION tb_G_L  VALUES ('MH03'),
  PARTITION tb_M_R  VALUES ('MH04'),
  PARTITION tb_S_Z  VALUES ('MH05'),
  PARTITION p_unknown VALUES (DEFAULT)
);

COMMIT;
