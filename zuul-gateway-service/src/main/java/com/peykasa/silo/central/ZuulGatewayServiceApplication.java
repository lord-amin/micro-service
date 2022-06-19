package com.peykasa.silo.central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableZuulProxy
@EnableFeignClients
public class ZuulGatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulGatewayServiceApplication.class, args);
    }


}
