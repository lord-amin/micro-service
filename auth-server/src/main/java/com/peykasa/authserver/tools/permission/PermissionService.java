package com.peykasa.authserver.tools.permission;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peykasa.authserver.config.APIConfig;
import com.peykasa.authserver.config.Perm;
import com.peykasa.authserver.config.PermissionConfig;
import com.peykasa.authserver.model.entity.Permission;
import com.peykasa.authserver.model.entity.PermissionURL;
import com.peykasa.authserver.model.entity.Role;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Yaser(amin) Sadeghi
 */
@Component
public class PermissionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionService.class);
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Getter
    private Map<APIConfig, Set<String>> url2Map = new TreeMap<>();

    @Autowired
    public PermissionService(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    public Map<String, String> update(File file, String serviceName) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        LOGGER.info("Reading file {}", file.toString());
        PermissionConfig permissionConfig = objectMapper.readValue(file, PermissionConfig.class);
        LOGGER.info("The file {} readed " + System.lineSeparator() + "{}", file.toString(), permissionConfig.toString());
        Map<APIConfig, Set<String>> urlMap = permissionConfig.toUrlMap();
        for (Map.Entry<APIConfig, Set<String>> entry : urlMap.entrySet()) {
            Set<String> url = url2Map.computeIfAbsent(entry.getKey(), k -> new HashSet<>());
            url.addAll(entry.getValue());
        }
        System.out.println("URL mapping is .......");
        for (Map.Entry<APIConfig, Set<String>> entry : url2Map.entrySet()) {
            System.out.println(entry.getKey().toString() + " = > " + entry.getValue());
        }
        return checkPermission(permissionConfig, serviceName);
    }

    public List<String> delete(String serviceName) {
        List<Permission> all = permissionRepository.findAll(Specifications.where((root, query, cb) -> cb.equal(root.get("serviceName"), serviceName)));
        if (all.isEmpty()) {
            return new ArrayList<>();
        }
        return deletePermissions(all);
    }

    private List<String> deletePermissions(List<Permission> all) {
        List<String> deleted = new ArrayList<>();
        for (Permission permission : all) {
            LOGGER.warn("Trying to remove permission {} from data base", permission.getPermission());
            List<Role> roles = roleRepository.findByPermission(permission.getId());
            if (roles.size() > 0) {
                for (Role role : roles) {
                    boolean remove = role.getPermissions().remove(permission);
                    LOGGER.info("Removing permission {} from role {}={}", permission.getPermission(), role.getName(), remove);
                    roleRepository.save(role);
                }
            }
        }
        if (all.size() > 0) {
            List<Permission> perms = new ArrayList<>();
            for (Permission permission : all) {
                if(permission.getParentId()==null){
                    perms.add(permission);
                }
            }
            if (perms.isEmpty()) {
                return new ArrayList<>();
            }
            remove(perms);
        }
        return deleted;
    }

    private void remove(List<Permission> all) {
        for (Permission permission1 : all) {
            try {
                Permission founded = permissionRepository.findOne(permission1.getId());
                if (founded != null)
                    permissionRepository.delete(founded);
                else{
                    LOGGER.warn("Permission  '{}:{}' maybe deleted before ", permission1.getId(), permission1.getPermission());
                }
            } catch (Exception e) {
                LOGGER.warn("Could not delete permission '{}:{}'", permission1.getId(), permission1.getPermission());
                LOGGER.warn("error is : {}", e.getMessage());
            }
            LOGGER.info("Removing permission {} and children from data base ", permission1.getPermission());
        }
    }

    private Map<String, String> checkPermission(PermissionConfig config, String serviceName) {
        Map<String, String> handledPermissions = new LinkedHashMap<>();
        List<Perm> flatList = config.getFlatList();
        flatList.sort(Comparator.comparing(Perm::getOrder));
        LOGGER.info("Flat list  after sort permissions is ");
        for (Perm perm : flatList) {
            LOGGER.info(perm.getName());
        }
        for (Perm entry : flatList) {
            List<Permission> permission = permissionRepository.findByPermissionAndServiceName(entry.getName(), serviceName);
            if (permission.size() > 1) {
                LOGGER.info("Permission {} from data base has more than one result ", entry.getName());
                throw new RuntimeException("Permission " + entry.getName() + " from data base has more than one result");
            }
            Permission foundFromDb;
            if (permission.isEmpty()) {
                LOGGER.info("Permission {} from file found in db , so updated", entry.getName());
                permission = permissionRepository.findByPermission(entry.getName());
            }
            if (permission.isEmpty()) {
                LOGGER.info("Permission {} from file not found in db , so inserted", entry.getName());
                foundFromDb = new Permission();
                foundFromDb.setPermission(entry.getName());
                handledPermissions.put(foundFromDb.getPermission(), "inserted");
            } else {
                LOGGER.info("Permission {} with service {} from file found in db , so updated", entry.getName(), serviceName);
                foundFromDb = permission.get(0);
                foundFromDb.setPermission(entry.getName());
                handledPermissions.put(foundFromDb.getPermission(), "updated");
            }
            foundFromDb.setDescription(entry.getDesc());
            foundFromDb.setTitle(entry.getTitle());
            if (entry.getParent() != null) {
                List<Permission> parent = permissionRepository.findAll(Specifications.where((root, query, cb) -> cb.equal(root.get("permission"), entry.getParent().getName())));
                if (parent.isEmpty()) {
                    LOGGER.error("The parent permission {} not found from data base for permission {}", entry.getParent().getName(), entry.getName());
                    throw new RuntimeException("The parent permission '" + entry.getParent().getName() + "' not found from data base for permission " + entry.getName());
                }
                if (parent.size() > 1) {
                    LOGGER.info("The parent permission {} from data base has more than one result for permission {}", entry.getParent(), entry.getName());
                    throw new RuntimeException("The parent permission '" + entry.getParent() + "' from data base has more than one result for permission " + entry.getName());
                }
                foundFromDb.setParentId(parent.get(0).getId());
            }
            foundFromDb.getUrls().clear();
            for (APIConfig apiConfig : entry.getApiList()) {
                PermissionURL e = new PermissionURL(apiConfig.getMethod(), apiConfig.getUrl(), foundFromDb);
                foundFromDb.getUrls().add(e);
            }
            foundFromDb.setServiceName(serviceName);
            permissionRepository.save(foundFromDb);
        }
        return handledPermissions;
    }
}
