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
- change application name `psqlsimple` to your own name (optional)
- create postgres service instance, here the name can be `pg-perf` or replace with your own instance name
- then execute

```
mvn clean package
cf push
```

## api documentation
- link

[https://psqlsimple.run.aws-jp01-pr.ice.predix.io/swagger-ui.html](https://psqlsimple.run.aws-jp01-pr.ice.predix.io/swagger-ui.html)

- GET /student/{id}

```
GET https://psqlsimple.run.aws-jp01-pr.ice.predix.io/student/1
Content-Type: application/json
Response Body:
{
  "student": {
    "id": 1,
    "name": "Hello"
  }
}
```

- GET /student/all

```
GET https://psqlsimple.run.aws-jp01-pr.ice.predix.io/student/all
Content-Type: application/json
Response Body:
{
  "students": [
    {
      "id": 1,
      "name": "Hello"
    },
    {
      "id": 2,
      "name": "World"
    }
  ]
}
```

- POST /student

```
POST https://psqlsimple.run.aws-jp01-pr.ice.predix.io/student
Content-Type: application/json
Request Body:
{
  "name": "test"
}

Response:
Status: 200
{
  "created": {
    "id": 12
  }
}
```

- PUT /student/3

```
PUT https://psqlsimple.run.aws-jp01-pr.ice.predix.io/student/3
Content-Type: application/json
Request Body:
{
  "id": 3,
  "name": "222"
}

Response: 
# if /student/3 is non-existing:
Status: 200
{
  "created": {
    "id": 3
  }
}

# if /student/3 is existing:
Status: 200
{
  "updated": {
    "id": 3
  }
}
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
- JDBC URL: `jdbc:h2:mem:test;MVCC=TRUE;DB_CLOSE_DELAY=-1;MODE=POSTGRESQL`
- username: `postgres`
- password: `root`

