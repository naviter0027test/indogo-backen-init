current_time=`date +"%Y-%m-%d %H:%M:%S"`
/data/backup/bin/backup.sh > /data/backup/logs/email.txt 2>&1
cat /data/backup/logs/email.txt | mail -s "Indogo System Backup $current_time" -r indogo.system@gmail.com ythung1@gmail.com indogotw@gmail.com
