```
mvn install:install-file -Dfile=kettle-core-9.1.0.0-324.jar  -DgroupId=pentaho-kettle -DartifactId=kettle-core -Dversion=9.1.0.0-324 -Dpackaging=jar
mvn install:install-file -Dfile=kettle-engine-9.1.0.0-324.jar  -DgroupId=pentaho-kettle -DartifactId=kettle-engine -Dversion=9.1.0.0-324 -Dpackaging=jar
mvn install:install-file -Dfile=metastore-9.1.0.0-324.jar  -DgroupId=pentaho-kettle -DartifactId=metastore -Dversion=9.1.0.0-324 -Dpackaging=jar
```
##本地创建两个表
```
create table test.stu1
(
	id int null,
	name varchar(20) null,
	age int null
);

create table test.stu2
(
	id int null,
	name varchar(20) null
);

```
### kt.kjb、kt.ktr这两个脚本中需要改成自己的本地数据库
