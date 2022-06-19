# config-service
this project is the centre piece of all others services' configurations. In order to run, it needs connect to
the discovery server. So you have to up and run the discovery server first.
## Docker Image
to build docker image, you need a docker engine up and running on your machine. Run the following command:
- `docker build -t config-service .`

to set eureka server, use the EUREKA_URI environment variable like this:
- `docker run --rm -it --env UEREKA_URI=http://localhost:9010/eureka config-service`

other environment variables are:
- `REPO_URI`: uri for the git repository contains all configs.
- `REPO_USER`: username to access the repository.
- `REPO_PASS`: password to access the repository.

## Structure of the repository
all services which want to use config server, **Should Have** an application name, defined in their configuration file (e.g. `spring.application.name=foo`). The Repository which passes to the config server has to have a folder in it with the application name (e.g. `foo`) and all configuration files of the applications are listed under this folder. (e.g. `foo.properties, foo-dev.properties`). Applications can ask for the specific profile by setting active profile in their `bootstrap.properties` file (e.g. `spring.profiles.active=dev,default`) 

