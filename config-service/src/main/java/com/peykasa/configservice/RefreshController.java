package com.peykasa.configservice;


import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/")
public class RefreshController {
    private final static Logger LOGGER = LoggerFactory.getLogger(RefreshController.class);
    private final EurekaClient eurekaClient;
    private final RestTemplate restTemplate;
    @Value("${spring.application.name:config-service}")
    private String name;


    @Autowired
    public RefreshController(@Qualifier("eurekaClient") EurekaClient eurekaClient, RestTemplate restTemplate) {
        this.eurekaClient = eurekaClient;
        this.restTemplate = restTemplate;
    }


    @GetMapping("/refresh")
    public Map<String, String> refresh() {
        var resultMap = new HashMap<String, String>();

        if (Objects.isNull(eurekaClient) || CollectionUtils.isEmpty(eurekaClient.getApplications().getRegisteredApplications())) {
            resultMap.put("Failure", "No Eureka Clients available to refresh");
            return resultMap;
        }
        for (var item : eurekaClient.getApplications().getRegisteredApplications()) {
            if (name.equalsIgnoreCase(item.getName()))
                continue;
            for (var instance : item.getInstances()) {
                invokeClient(instance, resultMap);
            }
        }
        return resultMap;

    }

    private void invokeClient(InstanceInfo instanceInfo, Map<String, String> resultMap) {
        try {


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(null, headers);

            final var url = "http://" + instanceInfo.getHostName() + ":" + instanceInfo.getPort() + "/actuator/refresh";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            resultMap.put(instanceInfo.getHomePageUrl(), response.getStatusCode().getReasonPhrase());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resultMap.put(instanceInfo.getHomePageUrl(), "Error");
        }
    }

}
