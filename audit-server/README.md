# Introduction

<div dir="rtl" align="right">
این مولفه وظیفه ذخیره و بازیابی اطلاعات مربوط به فعالیت کاربران را دارد
</div>

----


**Installation and Configuration:**
http://gitlab.peykasa.ir/UIFramework/audit-server/wikis/Installation-and-configuration

**REST API:**
http://gitlab.peykasa.ir/UIFramework/audit-server/wikis/REST-api

**Run with Docker:**
http://gitlab.peykasa.ir/UIFramework/audit-server/wikis/run-using-docker

**Build Docker Image:**
http://gitlab.peykasa.ir/UIFramework/audit-server/wikis/build-docker-image

----

**run using code**
```
./mvnw spring-boot:run
````
**run postman test**

install newman `npm install -g newman`
````bash
 newman run ./test-postman/Audit-Server-Test.postman_collection.json -e ./test-postman/Audit-Server-Test-Development.postman_environment.json 
````
----

**log path:** localhost:1999/audit-server/v1/log

**search path** localhost:1999/audit-server/v1/search

**api-docs json:** http://localhost:1999/audit-server/v1/api-docs

**swagger-ui:** http://localhost:1999/audit-server/v1/swagger-ui.html
