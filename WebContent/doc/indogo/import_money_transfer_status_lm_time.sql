select concat('update money_transfer set lm_time_pending = ''', a.action_time, ''' where txn_id = ', a.row_id, ';') as update_sql from history a, history_data b
where a.table_id = 4
and a.action_id = 1
and a.log_id = b.log_id
and b.attr_name = 'transfer_status_id'
and b.new_attr_value = '1';

select concat('update money_transfer set lm_time_paid = ''', a.action_time, ''' where txn_id = ', a.row_id, ';') as update_sql from history a, history_data b
where a.table_id = 4
and a.action_id = 1
and a.log_id = b.log_id
and b.attr_name = 'transfer_status_id'
and b.new_attr_value = '2';

select concat('update money_transfer set lm_time_pending = ''', max(lm_time), ''' where txn_id = ', txn_id, ';') as update_sql
  from money_transfer_status
 where new_status_id = 1
group by txn_id;
 
select concat('update money_transfer set lm_time_paid = ''', max(lm_time), ''' where txn_id = ', txn_id, ';') as update_sql
  from money_transfer_status
 where new_status_id = 2
group by txn_id;

select concat('update money_transfer set lm_time_process = ''', max(lm_time), ''' where txn_id = ', txn_id, ';') as update_sql
  from money_transfer_status
 where new_status_id = 3
group by txn_id;

select concat('update money_transfer set lm_time_transfer = ''', max(lm_time), ''' where txn_id = ', txn_id, ';') as update_sql
  from money_transfer_status
 where new_status_id = 4
group by txn_id;

select concat('update money_transfer set lm_time_failed = ''', max(lm_time), ''' where txn_id = ', txn_id, ';') as update_sql
  from money_transfer_status
 where new_status_id = 5
group by txn_id;

select concat('update money_transfer set lm_time_cancel = ''', max(lm_time), ''' where txn_id = ', txn_id, ';') as update_sql
  from money_transfer_status
 where new_status_id = 6
group by txn_id;