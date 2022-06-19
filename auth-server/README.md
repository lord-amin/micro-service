# Introduction

<div dir="rtl" align="right">
این کامپوننت وظیفه احراز حویت را بر عهده دارد
</div>

----




**Installation and Configuration:**
http://gitlab.peykasa.ir/UIFramework/auth-server/wikis/Installation-and-Configuration

**Get Token and User Information:**
http://gitlab.peykasa.ir/UIFramework/auth-server/wikis/Get-Token-and-User-Info


**Run with Docker:**
http://gitlab.peykasa.ir/UIFramework/auth-server/wikis/run-using-docker

**Build Docker Image:**
http://gitlab.peykasa.ir/UIFramework/auth-server/wikis/build-docker-image

----
**Authentication/Authorization Sequence**

![Auth Sequence](/doc/auth-seq.png)
----

**Database schema**

![Database Schema](/doc/erd.png)

----

**run using code**
```
./mvnw spring-boot:run
````
**run postman test**

install newman `npm install -g newman`
````bash
 newman run ./test-postman/Auth-Server-Test.postman_collection.json -e ./test-postman/Auth-Server-Test-Development.postman_environment.json 
````
----

**token path:** http://localhost:1998/auth-server/v2/oauth/token

**user information path:** http://localhost:1998/auth-server/v2/api/me

**api-docs json:** http://localhost:1998/auth-server/v2/api-docs

**swagger-ui:** http://localhost:1998/auth-server/v2/swagger-ui.html

**monitoring:** http://localhost:1998/auth-server/v2/monitoring

**monitoring-ui:** http://localhost:1998/auth-server/v2/admin
