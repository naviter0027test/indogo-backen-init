work_dir=/data/backup

current_time=`date +%Y%m%d%H%M%S`

echo "backup database"
cd $work_dir &&
bin/backup_database.sh &&
tar -zcf temp_${current_time}.tgz temp/ &&
echo "copy file to backup server" &&
scp temp_${current_time}.tgz yohobk1:/data/backup/temp_${current_time}.tgz &&
echo "restore data into backup server using remote shell" &&
ssh yohobk1 $work_dir/bin/restore.sh $current_time

if [[ -f temp_${current_time}.tgz ]]; then
    echo "copy file to liontrix server for archiving"
    scp temp_${current_time}.tgz guest@59.126.29.43:backup_indogo_database/
    rm -f temp_${current_time}.tgz
fi

echo "backup images"
rsync -avh /data/tomcat/images/ yohobk1:/data/tomcat/images/
