package com.peykasa.authserver.controller;

import com.peykasa.authserver.config.AppConfig;
import com.peykasa.authserver.model.cto.CreateClientCTO;
import com.peykasa.authserver.model.entity.Client;
import com.peykasa.authserver.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class UserClientMapper {
    private final PasswordEncoder passwordEncoder;
    private final AppConfig appConfig;

    public User toUserFromCreate(Client client, CreateClientCTO userClientCTO) {
        User user = new User();
        user.setEnabled(true);
        user.setSuperAdmin(true);
        user.setDeleted(false);
        user.setClient(client);
        user.setFirstName(userClientCTO.getName());
        user.setLastName("");
        user.setPassword(passwordEncoder.encode(userClientCTO.getPassword()));
        user.setUsername(client.getClientId() + "_" + userClientCTO.getUsername());

        Date now = new Date();
        user.setCreationDate(now);
        user.setModifiedDate(now);
        //   set password expiry
        Duration expireTime = appConfig.getLoginPolicy().getExpireTime();
        if (expireTime != null && expireTime.toMillis() > 0) {
            user.setPasswordExpiry(now, expireTime.toMillis());
        }
        return user;
    }

    public Client toClientFromCreate(CreateClientCTO userClientCTO) {
        Client client = new Client();
        client.setClientId(userClientCTO.getClientId());
        client.setClientSecret(passwordEncoder.encode(userClientCTO.getPassword()));
        client.setGrantType("password");
        return client;
    }
}
