# discovery-service
This project is the heart of central silo solution. It hold a registry of all available micro-services. Those services need to register themselves in the discovery server in order to be found by the others

## Docker Image
to build docker image, you need a docker engine up and running on your machine. Run the following command:
- `docker build -t discovery-service .`

to run the docker container, run the following command:
- `docker run --rm -it --env EUREKA_PORT=9010 discovery-service`

There are environment variables you could set:
- `EUREKA_PORT`: The listening port. default is `9010`
- `APPLICATION_NAME`: The `spring.application.name`. default is `discovery-service`
- `REGISTER_TO_OTHER`: Indicate whether this instance register to other instance in the cluster. default is `false` 
- `DEFAULT_ZONE`: The list of address of the other instances.
- `HOST_NAME`: The shown name of the instance in the other's peer list.  default is equal to `
