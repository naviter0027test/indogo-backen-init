if [[ -z $1 ]]; then
    echo "no param"
    exit 1
fi

work_dir=/data/backup

current_time=$1

cd $work_dir &&
tar -zxf temp_${current_time}.tgz &&
bin/restore_database.sh &&
rm -f temp_${current_time}.tgz

