-- Script: create_do_balance_update.sql
-- Creates stored procedure do_balance_update as specified

CREATE OR REPLACE PROCEDURE do_balance_update (
    p_Vehocle_registration_nbr IN VARCHAR2,
    p_fast_tag_id IN VARCHAR2,
    p_ip_address IN VARCHAR2,
    p_Tollplaza_name IN VARCHAR2,
    p_toll_amt IN NUMBER,
    p_curr_bal IN NUMBER,
    p_vehicle_type IN VARCHAR2,
    p_ret_message OUT VARCHAR2,
    p_ret_code OUT NUMBER
)
AS
BEGIN
  IF p_curr_bal - p_toll_amt <= 0 THEN
    p_ret_message := 'Insufficient balance';
    p_ret_code := -2;
    RETURN;
  END IF;

  UPDATE transaction_balance
  SET currnet_bal_amt = currnet_bal_amt - p_toll_amt
  WHERE Vehocle_registration_nbr = p_Vehocle_registration_nbr
    AND fast_tag_id = p_fast_tag_id;

  INSERT INTO transaction_log (txn_id, Vehocle_registration_nbr, fast_tag_id, vehicle_type, currnet_bal_amt, deducted_amt, new_bal_amt, ceated_dt)
  VALUES (SYS_GUID(), p_Vehocle_registration_nbr, p_fast_tag_id, p_vehicle_type, p_curr_bal, p_toll_amt, p_curr_bal - p_toll_amt, SYSDATE);

  p_ret_message := 'Success';
  p_ret_code := 1;
  -- NOTE: transaction commit/rollback is managed by the caller; do NOT commit here
EXCEPTION
  WHEN OTHERS THEN
    p_ret_message := SQLERRM;
    p_ret_code := -1;
    RAISE;
END do_balance_update;
/
