select a.action_time, a.action_user, sum(a.score) as score, b.color_id from (
	select date(action_time) as action_time, 'create_invoice' as action_id, action_user, count(*) * %2$s as score
	  from history
	 where table_id = 4
	   and action_time >= date_add(date(sysdate()), interval -%1$s day)
		and action_user <> 'app'
		and action_id = 1
	group by date(action_time), action_user
	union all
	select date(action_time) as action_time, 'create_member' as action_id, action_user, count(*) * %3$s as score
	  from history
	 where table_id = 1
	   and action_time >= date_add(date(sysdate()), interval -%1$s day)
		and action_id = 1
	group by date(action_time), action_user
	union all
	SELECT action_time, 'create_signature' as action_id, action_user, COUNT(*) * %4$s as score
	FROM (
	    SELECT (
	           SELECT new_attr_value
	             FROM history_data b
	            WHERE b.log_id = a.log_id AND b.attr_name = 'signature_photo_basename') AS signature_photo_basename,
	           a.action_user,
	           DATE(a.action_time) AS action_time
	      FROM history a
	     WHERE a.table_id = 1 AND a.action_time >= date_add(date(sysdate()), interval -%1$s day) AND a.action_id = 2
	) c
	where c.signature_photo_basename not like '%%_app'
	GROUP BY action_time, action_user
	union all
	select date(action_time) as action_time, 'verify_new_member' as action_id, action_user, count(*) * %5$s as score
	  from history
	 where table_id = 5
	   and action_time >= date_add(date(sysdate()), interval -%1$s day)
		and action_id = 2
		and action_desc = 'new_member'
	group by date(action_time), action_user
	union all
	select date(action_time) as action_time, 'verify_old_member' as action_id, action_user, count(*) * %6$s as score
	  from history
	 where table_id = 5
	   and action_time >= date_add(date(sysdate()), interval -%1$s day)
		and action_id = 2
		and action_desc = 'old_member'
	group by date(action_time), action_user
) a, user_list b
where a.action_user = b.user_name
group by a.action_time, a.action_user