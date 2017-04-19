# PsqlSimple
A simple webapp to demonstrate how to use postgresql service on predix

## feature
- use h2 data source connection on local environment
- use postgresql data source connection on predix
- disabled spring auto reconfiguration on predix, so we can optimize the configuration of datasource connection pool
- druid JDBC monitoring web page
- local h2 console web page

## run on local

```
mvn jetty:run
```

## run on predix
- open manifest.yml 
- change application name `psqlsimple` to your own name
- change application bind service `pg-perf` to your own postgresql service
- then execute

```
mvn clean package
cf push
```

## api implemented
- /student/{id}

```
https://psqlsimple.run.aws-jp01-pr.ice.predix.io/student/1
```

- /student/all

```
https://psqlsimple.run.aws-jp01-pr.ice.predix.io/student/all
```

## druid monitor
- username : `druid`
- password : `druid`

```
https://psqlsimple.run.aws-jp01-pr.ice.predix.io/druid/login.html
```

## h2 console for local environment
```
http://localhost:8080/psql/h2/login.jsp
```
- Settings: `Generic H2 (Embedded)`
- Driver Class: `org.h2.Driver`
- JDBC URL: `jdbc:h2:mem:psqldb;MVCC=TRUE;DB_CLOSE_DELAY=-1;MODE=POSTGRESQL`
- username: postgres
- password: root

