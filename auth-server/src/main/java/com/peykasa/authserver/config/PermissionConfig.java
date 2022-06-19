package com.peykasa.authserver.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.var;

import java.util.*;

/**
 * @author Yaser(amin) Sadeghi
 */
public class PermissionConfig {
    public static int ORDER = 0;
    @Getter
    @Setter
    private List<Perm> permissions = new ArrayList<>();

    public Map<APIConfig, Set<String>> toUrlMap() {
        var map = new TreeMap<APIConfig, Set<String>>();
        for (Perm permission : permissions) {
            var urlMap = permission.toUrlMap();
            for (var entry : urlMap.entrySet()) {
                var url = map.computeIfAbsent(entry.getKey(), k -> new HashSet<>());
                url.addAll(entry.getValue());
            }
        }
        return map;
    }

    public List<Perm> getFlatList() {
        var map = new ArrayList<Perm>();
        for (var entry : permissions) {
            map.add(entry);
            entry.setOrder(ORDER++);
            var permStringMap = entry.getFlatList();
            map.addAll(permStringMap);
        }
        return map;
    }

    @Override
    public String toString() {
        var mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setVisibility(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE));
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "PermissionConfig{" +
                    "permissions=" + permissions +
                    '}';
        }
    }
}
