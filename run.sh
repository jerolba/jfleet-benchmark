#!/bin/bash
set -e

IP_POSTGRES="localhost"
USER_POSTGRES="test"
PASS_POSTGRES="test"
IP_MYSQL="localhost"
USER_MYSQL="root"
PASS_MYSQL="root"
 
TIMES=10

dbs=()

while getopts ":p:m:t:" opts
do
  case $opts in
	t)
		TIMES=$OPTARG
	    ;;
    p)
        echo "Postgres IP: $OPTARG"
		IP_POSTGRES=$OPTARG
		echo "driver=org.postgresql.Driver" > postgres.properties
		echo "user=$USER_POSTGRES" >> postgres.properties
		echo "password=$PASS_POSTGRES" >> postgres.properties
		echo "urlConnection=jdbc:postgresql://$IP_POSTGRES:5432/testdb?reWriteBatchedInserts=true" >> postgres.properties
		
		echo "$IP_POSTGRES:5432:testdb:$USER_POSTGRES:$PASS_POSTGRES" > ~/.pgpass
		chmod 600 ~/.pgpass
		dbs+=("postgres")
        ;;
	m)
        echo "MySQL IP: $OPTARG"
		IP_MYSQL=$OPTARG
		echo "driver=com.mysql.jdbc.Driver" > mysql.properties
		echo "user=$USER_MYSQL" >> mysql.properties
		echo "password=$PASS_MYSQL" >> mysql.properties
		echo "urlConnection=jdbc:mysql://$IP_MYSQL/testdb?useSSL=false&useUnicode=true&characterEncoding=utf-8&rewriteBatchedStatements=true" >> mysql.properties
		dbs+=("mysql")
        ;;		
  esac
done

echo "Number of times: $TIMES"

if [ ! -x "log" ]
then
	mkdir log
fi


for db in "${dbs[@]}"
do
	for i in `seq 1 $TIMES`;
	do
		echo "Running JdbcBatchInsert for $db"
		sizes=(100 500 1000 5000)
		for size in "${sizes[@]}"
		do
			echo Iteration $i for size $size
			java -cp ./jfleet-benchmark.jar org.jfleet.benchmark.JdbcBatchInsert $db.properties $size > log/JdbcBatchInsert_$db\_$size\_$i.log
			sleep 10
		done

		echo "Running JpaBatchInsert for $db"
		sizes=(100 500 1000 5000)
		for size in "${sizes[@]}"
		do
			echo Iteration $i for size $size
			java -cp ./jfleet-benchmark.jar org.jfleet.benchmark.JpaBatchInsert $db.properties $size > log/JpaBatchInsert_$db\_$size\_$i.log
			sleep 10
		done
		
		if [ "$db" == "mysql" ]
		then
			echo "Running BulkInsert for mysql"
			sizes=(1 2 4 8 16 32 64)
			for size in "${sizes[@]}"
			do
				echo Iteration $i for size $size
				java -cp ./jfleet-benchmark.jar org.jfleet.benchmark.LoadDataInsert mysql.properties $size > log/BulkInsert_mysql_$size\_$i.log
				sleep 10
			done	
			
			echo "Running Import for mysql"
			echo Iteration $i
			mysql -h $IP_MYSQL -u $USER_MYSQL -p$PASS_MYSQL testdb < bike_trip.sql
			(time mysqlimport --local -h $IP_MYSQL -u $USER_MYSQL -p$PASS_MYSQL testdb bike_trip.txt) &> log/Import_mysql_$i.out
			sleep 10			
		fi
		
		if [ "$db" == "postgres" ]
		then
			echo "Running BulkInsert for postgres"
			sizes=(1 2 4 8 16 32 64)
			for size in "${sizes[@]}"
			do
				echo Iteration $i for size $size
				java -cp ./jfleet-benchmark.jar org.jfleet.benchmark.CopyDataInsert postgres.properties $size > log/BulkInsert_postgres_$size\_$i.log
				sleep 10
			done	
			
			echo "Running Import for postgres"
			echo Iteration $i
			psql -h $IP_POSTGRES -U $USER_POSTGRES testdb < postgres_drop.sql
			(time psql -h $IP_POSTGRES -U $USER_POSTGRES testdb < postgres_dump.sql) &> log/Import_postgres_$i.out
			sleep 10
		fi
	done    
done
