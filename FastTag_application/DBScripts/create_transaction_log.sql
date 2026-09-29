-- Script: create_transaction_log.sql
-- Creates transaction_log table with partitioning by ceated_dt and index on vehicle fields

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE transaction_log PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN
      RAISE;
    END IF;
END;
/

CREATE TABLE transaction_log (
  txn_id                   VARCHAR2(50),
  Vehocle_registration_nbr VARCHAR2(50),
  fast_tag_id              VARCHAR2(50),
  vehicle_type             VARCHAR2(50),
  currnet_bal_amt          NUMBER(10),
  deducted_amt             NUMBER(10),
  new_bal_amt              NUMBER(10),
  ceated_dt                DATE,
  ip_address               VARCHAR2(255),
  Tollplaza_name           VARCHAR2(255),
  CONSTRAINT pk_transaction_log PRIMARY KEY (txn_id)
)
PARTITION BY LIST (SUBSTR(Vehocle_registration_nbr, 1, 2))
SUBPARTITION BY RANGE (ceated_dt)
(
  PARTITION p_state_01 VALUES ('MH01')
    (SUBPARTITION p_state_01_2024 VALUES LESS THAN (TO_DATE('01-JAN-2025','DD-MON-YYYY')),
     SUBPARTITION p_state_01_2025 VALUES LESS THAN (MAXVALUE)),
  PARTITION p_state_02 VALUES ('MH02')
    (SUBPARTITION p_state_02_2024 VALUES LESS THAN (TO_DATE('01-JAN-2025','DD-MON-YYYY')),
     SUBPARTITION p_state_02_2025 VALUES LESS THAN (MAXVALUE)),
  PARTITION p_state_others VALUES (DEFAULT)
    (SUBPARTITION p_state_others_2024 VALUES LESS THAN (TO_DATE('01-JAN-2025','DD-MON-YYYY')),
     SUBPARTITION p_state_others_2025 VALUES LESS THAN (MAXVALUE))
);

-- Index on Vehocle_registration_nbr and fast_tag_id
CREATE INDEX idx_txn_vehicle ON transaction_log (Vehocle_registration_nbr, fast_tag_id);

COMMIT;
