# zuul-proxy-service
This application is responsible for hiding all backend services. In order to run the project you need both [discovery-service](https://gitlab.peykasa.ir/silo/central/discovery-service) and [config-service](https://gitlab.peykasa.ir/silo/central/config-service). This service is using discovery-first approach. It means, the service first tries to connect to the discovery service, and asks the discovery for the config service addresses. 
## Docker Image
To build docker image, you need a docker engine up and running on your machine. Run the following command:
- `docker build -t zuul-service .`

to set eureka server , use the EUREKA_URI environment variable like this:
- `docker run --rm -it --env EUREKA_URI=http://localhost:9010/eureka zuul-service`

There are environment variables you could set to config the service:
- `ACTIVE_PROFILES`:  determine the active profiles list. Using this list, the service tries to fetch configurations from config server. The default value is `dev,default`. The Order of the list is important. So the service reads the configuration in `dev` profile first, and then reads the `default` profile. Be aware about local environment variable. These variables will override by profiles. You can set `OVERRIDE_SYS_PROPS` to `false` to prevent this.
- `OVERRIDE_SYS_PROPS`: indicate whether local system variables override by profiles. Default is `true`.
- `CONFIG_SERVICE_NAME`: The service name of config server which is registered under the discovery server. The default value is `config-service`.
- `EUREKA_URI`: The list of Eureka instances. The default value is `http://localhost:9010/eureka`
- `USER_PROFILE_HEADER`: Specify the http header key, which authenticated user profile sends through to backend services. The default value is `user_info`

