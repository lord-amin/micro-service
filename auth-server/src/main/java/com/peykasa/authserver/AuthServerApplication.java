package com.peykasa.authserver;

import com.peykasa.authserver.model.entity.Client;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.ClientRepository;
import com.peykasa.authserver.repository.RoleRepository;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.tools.ImportApplication;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * @author Taher Khorshidi
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AuthServerApplication {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientRepository clientRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServerApplication.class);

    public static void main(String[] args) throws ParseException, IOException {
        List<String> argsLocation = new ArrayList<>(Arrays.asList(args));
        System.out.println("ARGS is " + argsLocation);
        if (argsLocation.contains("--import")) {
            argsLocation.remove("--import");
            System.out.println("ARGS is " + Collections.singletonList(argsLocation));
            ImportApplication.main(argsLocation.toArray(new String[]{}));
            return;
        }
        ConfigurableApplicationContext run = SpringApplication.run(AuthServerApplication.class, argsLocation.toArray(new String[]{}));
        ConfigurableEnvironment environment = run.getEnvironment();
        logConfig(run);
        String base = "http://" + environment.getProperty("server.address") + ":" + environment.getProperty("server.port") + environment.getProperty("server.contextPath");
        String json = base + environment.getProperty("springfox.documentation.swagger.v2.path");
        String ui = base + "/swagger-ui.html";
        LOGGER.info("resource json address is [" + json + "]");
        LOGGER.info("resource ui address is [" + ui + "]");
    }

    /**
     * if application runs for the first time.
     * application doesn't have any User in Db, so nobody can login
     * in this method we create a DEFAULT USER : admin with password : 1
     */
    @PostConstruct
    public void userDataSeeding() {
        Client client = createClient();
        User user = createUser();

        Client found = clientRepository.findByClientId(client.getClientId());
        Optional<User> userOpt = userRepository.findByUsername(user.getUsername());

        if (found != null || userOpt.isPresent())
            return;

        clientRepository.saveAndFlush(client);
        userRepository.saveAndFlush(user);
    }

    private Client createClient() {
        Client client = new Client();
        client.setClientId("portal-client-id");
        client.setClientSecret(passwordEncoder.encode("Admin12345"));
        client.setGrantType("password");
        client.setAccessTokenValiditySeconds(300);
        client.setRefreshTokenValiditySeconds(6000);
        return client;
    }

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleRepository roleRepository;

    private User createUser() {
        User user = new User();
        user.setFirstName("admin");
        user.setLastName("admin");
        user.setClient(clientRepository.findOne("portal-client-id"));
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("1"));
        user.setCreationDate(new Date());
        user.setEnabled(true);
        user.setDeleted(false);
        user.setSuperAdmin(true);
        user.setRoles(new HashSet<>(roleRepository.findAll()));
        return user;
    }

    private static void logConfig(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof MapPropertySource) {
                MapPropertySource propertySource1 = (MapPropertySource) propertySource;
                if (propertySource1.getName().contains(".properties"))
                    for (String propertyName : propertySource1.getPropertyNames()) {
                        LOGGER.info("       {}={}", propertyName, environment.getProperty(propertyName));
                    }
            }
        }
    }
}
