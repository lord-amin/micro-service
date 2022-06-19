package com.peykasa.authserver.test;

import com.peykasa.authserver.*;
import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.exception.RestErrorResponse;
import com.peykasa.authserver.model.SimplePermission;
import com.peykasa.authserver.model.SimpleRole;
import com.peykasa.authserver.model.cto.CreateUserCTO;
import com.peykasa.authserver.model.cto.RoleCTO;
import com.peykasa.authserver.model.dto.RoleDTO;
import com.peykasa.authserver.model.dto.UserDTO;
import com.peykasa.authserver.model.entity.Permission;
import com.peykasa.authserver.repository.PermissionRepository;
import com.peykasa.authserver.repository.RoleRepository;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.tools.ImportApplication;
import org.apache.commons.cli.ParseException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Yaser(amin) Sadeghi
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                AuthServerApplication.class
        })
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class PermissionControllerSearchTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(PermissionControllerSearchTest.class);
    @MockBean
    private Auditor auditor;
    @Autowired
    private AuditServiceAspect auditServiceAspect;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HttpUtil httpUtil;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private DatabaseUtil databaseUtil;
    private static boolean started = false;

    @BeforeClass
    public static void initClass() throws IOException, URISyntaxException, ParseException {
        Properties properties = ConfigUtil.loadConfig("application.properties");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            System.err.println(entry.getKey() + "=" + entry.getValue());
        }
        URI uri = ConfigUtil.class.getClassLoader().getResource("permissions.json").toURI();
        String path1 = uri.getPath();
        ImportApplication.main(new String[]{
                "-a",
                "delete",
                "-s",
                "auth",
                "-l",
                "info"});
        ImportApplication.main(new String[]{
                "-a",
                "update",
                "-f",
                path1,
                "-s",
                "auth", "-l", "info"});
    }

    @Before
    public void init() throws InterruptedException, IOException {
        if (started)
            return;
        databaseUtil.init(false);
        started = true;
    }

    private static final String AUTH_SERVER_ROLE_CREATE = "auth_server_role_create";
    private static final String AUTH_SERVER_ROLE_READ = "auth_server_role_read";
    private static final String AUTH_SERVER_ROLE_UPDATE = "auth_server_role_update";

    @Test
    public void testFindAllPermissionsNoSuperAdminAndHasNotRolesPermissionToView() throws Exception {
        List<Permission> all1 = permissionRepository.findAll((Specifications<Permission>) null);
        List<SimplePermission> collect = all1.stream().map(permission -> new SimplePermission(permission.getId(), permission.getPermission()))
                .filter(simplePermission ->
                        !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_CREATE) &&
                                !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_READ) &&
                                !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_UPDATE)
                ).collect(Collectors.toList());
        String username = "no_perm";
        RoleDTO no_perm = createRole(username, collect);
        String pass = "1111";
        createUser(username, pass, Collections.singletonList(no_perm));
        ResponseEntity<RestErrorResponse> resourcesResponseEntity = find(username, pass, RestErrorResponse.class);
        System.out.println(resourcesResponseEntity);
        Assert.assertEquals(resourcesResponseEntity.getBody().getKey(), Constants.PERMISSION_DENIED_ERROR);
    }

    @Test
    public void testFindAllPermissionsNoSuperAdminAndHasRoleCreatePermissionToView() throws Exception {
        List<Permission> all1 = permissionRepository.findAll((Specifications<Permission>) null);
        List<SimplePermission> collect = all1.stream().map(permission -> new SimplePermission(permission.getId(), permission.getPermission()))
                .filter(simplePermission ->
//                        !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_CREATE) &&
                                !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_READ) &&
                                        !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_UPDATE)
                ).collect(Collectors.toList());
        String username = "has_perm_create";
        RoleDTO no_perm = createRole(username, collect);
        String pass = "1111";
        createUser(username, pass, Collections.singletonList(no_perm));
        ResponseEntity<String> resourcesResponseEntity = find(username, pass, String.class);
        System.out.println(resourcesResponseEntity);
        Assert.assertEquals(resourcesResponseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testFindAllPermissionsNoSuperAdminAndHasRoleReadToView() throws Exception {
        List<Permission> all1 = permissionRepository.findAll((Specifications<Permission>) null);
        List<SimplePermission> collect = all1.stream().map(permission -> new SimplePermission(permission.getId(), permission.getPermission()))
                .filter(simplePermission ->
                                !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_CREATE) &&
//                                !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_READ) &&
                                        !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_UPDATE)
                ).collect(Collectors.toList());
        String username = "has_perm_read";
        RoleDTO no_perm = createRole(username, collect);
        String pass = "1111";
        createUser(username, pass, Collections.singletonList(no_perm));
        ResponseEntity<String> resourcesResponseEntity = find(username, pass, String.class);
        System.out.println(resourcesResponseEntity);
        Assert.assertEquals(resourcesResponseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testFindAllPermissionsNoSuperAdminAndHasRoleUpdateToView() throws Exception {
        List<Permission> all1 = permissionRepository.findAll((Specifications<Permission>) null);
        List<SimplePermission> collect = all1.stream().map(permission -> new SimplePermission(permission.getId(), permission.getPermission()))
                .filter(simplePermission ->
                                !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_CREATE) &&
                                        !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_READ)
//                                &&
//                                !simplePermission.getPermission().equals(AUTH_SERVER_ROLE_UPDATE)
                ).collect(Collectors.toList());
        String username = "has_perm_update";
        RoleDTO no_perm = createRole(username, collect);
        String pass = "1111";
        createUser(username, pass, Collections.singletonList(no_perm));
        ResponseEntity<String> resourcesResponseEntity = find(username, pass, String.class);
        System.out.println(resourcesResponseEntity);
        Assert.assertEquals(resourcesResponseEntity.getStatusCode(), HttpStatus.OK);
    }

    private UserDTO createUser(String username, String pass, List<SimpleRole> list) {
        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("noPermissions");
        userCTO.setLastName("noPermissions");
        userCTO.setUsername(username);
        userCTO.setPassword(pass);
        userCTO.setRoles(new ArrayList<>());
        userCTO.getRoles().addAll(list);
        ResponseEntity<UserDTO> created = createSuccess(userCTO);
        Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
        return created.getBody();
    }


    private <T> ResponseEntity<T> find(String u, String p, Class<T> tClass) {
        return httpUtil.get(u, p, "/api/permissions", new HashMap<>(), tClass);
    }

    private ResponseEntity<RestErrorResponse> findOneException(Long id) {
        return httpUtil.get("admin", "1", Constants.ROLE_CONTEXT_PATH + "/" + id, new HashMap<>(), RestErrorResponse.class);
    }

    private Audit normalAudit(Audit audit) {
        audit.setRemoteAddress(null);
        audit.setTime(null);
        LOGGER.warn(audit.toString());
        return audit;
    }

    private ResponseEntity<UserDTO> createSuccess(CreateUserCTO userCTO) {
        return httpUtil.post("admin", "1", Constants.USER_CONTEXT_PATH, userCTO, UserDTO.class);
    }

    private RoleDTO createRole(String name, List<SimplePermission> collect) {
        RoleCTO roleCTO = new RoleCTO();
        roleCTO.setName(name);
        roleCTO.setDescription(name);
        roleCTO.setPermissions(collect);
        ResponseEntity<RoleDTO> created = createSuccess(roleCTO);
        Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
        return created.getBody();
    }

    private ResponseEntity<RoleDTO> createSuccess(RoleCTO roleCTO) {
        return httpUtil.post("admin", "1", Constants.ROLE_CONTEXT_PATH, roleCTO, RoleDTO.class);
    }

}
