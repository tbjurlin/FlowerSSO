# Startup and initialize flowersso db

Creates a new flowerdb locally

### Start docker mysql
docker run -p 3306:3306 --name flowerDB -v /var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=flowerdb -d mysql

### copy schema.sql file into docker container
docker cp ./schema.sql flowerDB:/root/sql/schema.sql

### Start bash inside docker container
docker exec -ti flowerDB bash

### Change to sql dir
cd ~/sql/

### start mysql
mysql -u root -p flowerdb
password: root

### load .sql
SOURCE schema.sql
