package com.peykasa.apigatewayservice.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Yaser(amin) Sadeghi
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthUser {

    private Long id;
    private String username;
    private Client client;
    private boolean superAdmin = false;
    private Set<OAuthRole> roles = new HashSet<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Client {
        private String clientId;

        @Override
        public String toString() {
            return "Client{" +
                    "clientId='" + clientId + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "OAuthUser{" +
                "username='" + username + '\'' +
                ", client=" + client +
                ", superAdmin=" + superAdmin +
                '}';
    }
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OAuthRole {
        @Getter
        @Setter
        private Set<OAuthPermission> permissions = new HashSet<>();

    }
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OAuthPermission {
        @Getter
        @Setter
        private String permission;
        @Getter
        @Setter
        private String serviceName;
    }
}
