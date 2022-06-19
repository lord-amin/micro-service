package com.peykasa.authserver.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.var;

import java.util.*;

/**
 * @author Yaser(amin) Sadeghi
 */
@Data
public class Perm {
    private String name;
    private String title;
    private String desc;
    private List<Perm> permissions = new ArrayList<>();
    private List<APIConfig> apiList = new ArrayList<>();
    @JsonIgnore
    private Integer order;
    @JsonIgnore
    private Perm parent;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Perm perm = (Perm) o;

        return name.equals(perm.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public List<Perm> getFlatList() {
        List<Perm> map = new ArrayList<>();
        for (Perm entry : permissions) {
            entry.setParent(this);
            map.add(entry);
            entry.setOrder(PermissionConfig.ORDER++);
            List<Perm> permStringMap = entry.getFlatList();
            map.addAll(permStringMap);
        }
        return map;
    }


    private Map<APIConfig, Set<String>> toMap() {
        Map<APIConfig, Set<String>> map = new TreeMap<>();
        for (APIConfig apiConfig : apiList) {
            Set<String> flat = map.computeIfAbsent(apiConfig, k -> new HashSet<>());
            flat.add(name);
        }
        return map;
    }

    public Map<APIConfig, Set<String>> toUrlMap() {
        var map = new TreeMap<APIConfig, Set<String>>();
        for (var permission : permissions) {
            if (permission.permissions.isEmpty()) {
                var stringSetMap = permission.toMap();
                for (var entry : stringSetMap.entrySet()) {
                    var url = map.computeIfAbsent(entry.getKey(), k -> new HashSet<>());
                    url.addAll(entry.getValue());
                }
            } else {
                var urlMap = permission.toUrlMap();
                for (var entry : urlMap.entrySet()) {
                    var url = map.computeIfAbsent(entry.getKey(), k -> new HashSet<>());
                    url.addAll(entry.getValue());
                }
            }
        }
        return map;
    }

}
