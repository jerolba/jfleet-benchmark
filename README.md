# JFleet Benchmark

## TL;DR:

[JFleet](https://github.com/jerolba/jfleet) is a Java library which persist in database large collections of Java POJOs as fast as possible, using the best available technique in each database provider, achieving it with alternate persistence methods from each JDBC driver implementation.

Its goal is to store a large amount of information in a single table using available batch persistence techniques.

JFleet Benchmark is a project which tries to measure the efficiency of JFleet comparing with different persistence techniques:
- Using JPA with Hibernate implementation 
- Using JDBC driver directly
- After a database dump, using the associated import command

[![mysql vs postgres](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=485493047&format=image)](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=485493047&format=interactive)


### Conclusion

**JFleet performance is comparable to using the native database import tool, and is between 2.1X and 3.8X faster than using the JDBC driver directly.**

## Table of Contents

- [Dataset](#dataset)
- [Hardware and Software Setup](#hardware-and-software-setup)
    - [MySQL Setup](#mysql-setup)
    - [PostgreSQL Setup](#postgresql-setup)
    - [Client Setup](#client-setup)
- [Benchmark methodology](#benchmark-methodology)    
- [MySQL](#mysql)
    - [MySQL JPA Batch Insert](#mysql-jpa-batch-insert)
    - [MySQL JDBC Batch Insert](#mysql-jdbc-batch-insert)
    - [MySQL DB Import](#mysql-db-import)
    - [MySQL JFleet Bulk Insert](#mysql-jfleet-bulk-insert)
    - [MySQL Comparison](#mysql-comparison)
- [PostgreSQL](#postgresql)
    - [PostgreSQL JPA Batch Insert](#postgresql-jpa-batch-insert)
    - [PostgreSQL JDBC Batch Insert](#postgresql-jdbc-batch-insert)
    - [PostgreSQL DB Import](#postgresql-db-import)
    - [PostgreSQL JFleet Bulk Insert](#postgresql-jfleet-bulk-insert)
    - [PostgreSQL Comparison](#postgresql-comparison)


## Dataset

The selected dataset is a public dataset provided by [Citi Bike NYC](https://www.citibikenyc.com/system-data) about each trip with their bikes.

The dataset can be downloaded from: https://s3.amazonaws.com/tripdata/index.html, and the [selected range](/downloadDataset.sh) of files are the whole 2016 year, with 457 MB of compressed CSVs files (2478 MB uncompressed). There are 13,845,655 rows in the dataset with an average size of 180 bytes per record.

Depending on the database engine, the total space used in database is around 2500 MB, using an average of 110 bytes per record.

Each record contains a mix of data types:
 - 4 Integer
 - 4 Doubles
 - 3 Strings 
 - 2 Dates
 - 1 Long
 - 1 Char

## Hardware and Software Setup

All test has been executed under Google Cloud Platform using Cloud SQL fully-managed database service.

Both databases has the same hardware configuration, and no change has been done in their default setup provided by Google Cloud.

### MySQL Setup

![MySQL Setup](doc/MySQLSetup.png "MySQL Setup") 

### PostgreSQL Setup

![PostgreSQL Setup](doc/PostgreSQLSetup.png "PostgreSQL Setup")

### Client Setup

The client was located in the same zone as the servers with the following configuration:

![Client Setup](doc/ClientSetup.png "Client Setup")

The Java runtime used was JDK 8u161.

## Benchmark methodology

The benchmark only tries to measure the efficiency of JFleet comparing with different persistence techniques, and is not a comparasion between database engines.

The benchmark is not executed in a production like environment and only executes inserts, without any other type of concurrent access or queries which can affect to the database performance. **Results can not be used as a reference for your production system, and you must execute yours to evaluate the convenience of JFleet.**
  
 - The benchmark measures the total time in seconds spent from the first byte read from dataset, to the last commit
 - A commit is executed after each batch operation
 - 13,845,655 rows are inserted in each benchmark execution
 - Each test is executed 10 times
 - The best and the worst results are discarded
 - Charts represent rows per second (more is better)

 
## MySQL

### MySQL JPA Batch Insert

|Iteration|500 Rows|1000 Rows|5000 Rows|10000 Rows|
|----:|----:|----:|----:|----:|
|1|922|889|~~859~~|885|
|2|949|~~902~~|845|871|
|3|959|899|838|876|
|4|959|887|828|864|
|5|935|881|830|~~856~~|
|6|943|879|~~812~~|856|
|7|925|874|813|~~891~~|
|8|~~913~~|880|829|869|
|9|937|~~871~~|840|870|
|10|~~968~~|902|841|882|
|**Avg. Sec.**|**941**|**886**|**833**|**872**|
|**Avg. row/sec**|**14,712**|**15,621**|**16,621**|**15,885**|

[![mysql JPA Batch Insert](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=434745884&format=image)](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=434745884&format=interactive)

### MySQL JDBC Batch Insert

|Iteration|500 Rows|1000 Rows|5000 Rows|10000 Rows|
|----:|----:|----:|----:|----:|
|1|~~638~~|583|~~550~~|~~562~~|
|2|652|579|555|565|
|3|660|571|555|566|
|4|650|581|557|566|
|5|~~680~~|577|554|570|
|6|638|~~559~~|551|564|
|7|656|~~595~~|567|582|
|8|660|589|~~569~~|586|
|9|662|590|567|583|
|10|654|585|565|~~589~~|
|**Avg. Sec.**|**654**|**582**|**559**|**573**|
|**Avg. row/sec**|**21,171**|**23,795**|**24,774**|**24,174**|

[![mysql JDBC Batch Insert](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=200889394&format=image)](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=200889394&format=interactive)

### MySQL DB Import

The selected `mysqldump` format is [tab-separated text](https://dev.mysql.com/doc/refman/5.7/en/mysqldump.html#option_mysqldump_tab) which is more performant than the standard with INSERT sentences.  

|Iteration|1 Import|
|----:|----:|
|1|~~221~~|
|2|174|
|3|146|
|4|141|
|5|136|
|6|137|
|7|136|
|8|~~135~~|
|9|138|
|10|135|
|**Avg. Sec.**|**143**|
|**Avg. row/sec**|**96,907**|

[![mysql DB Import](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=923940725&format=image)](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=923940725&format=interactive)

### MySQL JFleet Bulk Insert

|Iteration|1 MB|2 MB|4 MB|8 MB|16 MB|32 MB|64 MB|
|----:|----:|----:|----:|----:|----:|----:|----:|
|1|~~180~~|~~186~~|~~184~~|~~186~~|~~189~~|~~190~~|~~191~~|
|2|~~163~~|154|158|159|159|163|177|
|3|166|~~150~~|~~142~~|157|152|~~148~~|178|
|4|164|155|155|153|~~148~~|163|174|
|5|165|151|143|147|149|154|174|
|6|163|160|148|~~146~~|149|155|171|
|7|163|153|152|154|157|155|182|
|8|165|152|145|148|152|159|180|
|9|168|154|143|149|151|162|~~168~~|
|10|166|150|151|152|155|149|180|
|**Avg. Sec.**|**165**|**154**|**149**|**152**|**153**|**158**|**177**|
|**Avg. row/sec**|**83,913**|**90,126**|**92,691**|**90,866**|**90,494**|**87,909**|**78,224**|

[![mysql JFleet Bulk Insert](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=733817956&format=image)](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=733817956&format=interactive)


### MySQL Comparison

||Best Time|Best row/sec|
|-----|----:|----:|
|JPA Batch Insert|833|16,621|
|JDBC Batch Insert|559|24,774|
|DB Import|143|96,907|
|JFleet Bulk Insert|149|92,691|


[![mysql Comparison](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=1459748489&format=image)](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=1459748489&format=interactive)

## PostgreSQL

### PostgreSQL JPA Batch Insert

|Iteration|500 Rows|1000 Rows|5000 Rows|10000 Rows|
|----:|----:|----:|----:|----:|
|1|548|535|570|533|
|2|548|~~539~~|~~583~~|~~576~~|
|3|565|538|579|536|
|4|~~566~~|522|576|522|
|5|551|534|562|536|
|6|533|497|554|537|
|7|523|507|563|532|
|8|529|503|551|~~512~~|
|9|517|497|~~547~~|545|
|10|~~512~~|~~496~~|556|518|
|**Avg. Sec.**|**539**|**517**|**564**|**532**|
|**Avg. row/sec**|**25,676**|**26,800**|**24,554**|**26,007**|

[![postgres JPA Batch Insert](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=1290107757&format=image)](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=1290107757&format=interactive)

### PostgreSQL JDBC Batch Insert

|Iteration|500 Rows|1000 Rows|5000 Rows|10000 Rows|
|----:|----:|----:|----:|----:|
|1|363|317|~~314~~|337|
|2|~~367~~|~~330~~|~~340~~|~~345~~|
|3|353|316|320|337|
|4|355|318|322|340|
|5|359|325|325|344|
|6|355|324|324|335|
|7|~~350~~|316|319|331|
|8|350|~~313~~|322|336|
|9|350|316|316|~~323~~|
|10|355|319|323|345|
|**Avg. Sec.**|**355**|**319**|**321**|**338**|
|**Avg. row/sec**|**39,002**|**43,420**|**43,083**|**40,948**|

[![postgres JDBC Batch Insert](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=1563329639&format=image)](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=1563329639&format=interactive)

### PostgreSQL DB Import

|Iteration|1 Import|
|----:|----:|
|1|~~157~~|
|2|155|
|3|155|
|4|155|
|5|155|
|6|~~154~~|
|7|155|
|8|155|
|9|156|
|10|156|
|**Avg. Sec.**|**155**|
|**Avg. row/sec**|**89,183**|

[![postgres DB Import](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=871813304&format=image)](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=871813304&format=interactive)

### PostgreSQL JFleet Bulk Insert

|Iteration|1 MB|2 MB|4 MB|8 MB|16 MB|32 MB|64 MB|
|----:|----:|----:|----:|----:|----:|----:|----:|
|1|~~154~~|~~153~~|~~150~~|~~143~~|148|153|~~160~~|
|2|163|156|152|153|149|155|168|
|3|167|~~173~~|~~170~~|~~162~~|~~163~~|~~166~~|~~171~~|
|4|166|163|156|153|152|158|168|
|5|~~173~~|166|165|159|154|152|166|
|6|167|164|157|154|149|155|169|
|7|165|159|152|150|149|158|165|
|8|163|159|153|152|~~146~~|152|166|
|9|158|161|156|147|147|~~151~~|164|
|10|156|161|154|150|150|155|165|
|**Avg. Sec.**|**163**|**161**|**156**|**152**|**150**|**155**|**166**|
|**Avg. row/sec**|**84,878**|**85,931**|**88,968**|**90,940**|**92,458**|**89,471**|**83,220**|

[![postgres JFleet Bulk Insert](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=1488662574&format=image)](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=1488662574&format=interactive)


### PostgreSQL Comparison

||Best Time|Best row/sec|
|-----|----:|----:|
|JPA Batch Insert|517|26,800|
|JDBC Batch Insert|319|43,420|
|DB Import|155|89,183|
|JFleet Bulk Insert|150|92,458|


[![postgres Comparison](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=1970047048&format=image)](https://docs.google.com/spreadsheets/d/e/2PACX-1vTx61C0YNYlczo0S-ZTN56FH2mxvHPHf4jamTnY4wdMwjjF3TvxcW3Ti7VR83dd1R5EznB7xVhD1HD6/pubchart?oid=1970047048&format=interactive)


