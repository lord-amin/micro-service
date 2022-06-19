package com.peykasa.authserver.test;

import com.peykasa.authserver.*;
import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.config.APIConfig;
import com.peykasa.authserver.model.entity.Permission;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.ClientRepository;
import com.peykasa.authserver.repository.PermissionRepository;
import com.peykasa.authserver.repository.RoleRepository;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.tools.ImportApplication;
import com.peykasa.authserver.validation.ValidationConfiguration;
import org.apache.commons.cli.ParseException;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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
public class AuthorizationControllerTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthorizationControllerTest.class);
    @MockBean
    private Auditor auditor;
    @Autowired
    private AuditServiceAspect auditServiceAspect;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private HttpUtil httpUtil;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ValidationConfiguration validationConfiguration;
    @Autowired
    private DatabaseUtil databaseUtil;
    @Autowired
    private Util util;
    @Autowired
    private ClientRepository clientRepository;


    private static boolean started = false;


    @BeforeClass
    public static void initClass() throws IOException, URISyntaxException, ParseException {
        String simpleName = AuthorizationControllerTest.class.getSimpleName();
        Properties properties = ConfigUtil.loadConfig(simpleName + ".properties");
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
        System.out.println();
    }

    @Before
    public void init() throws InterruptedException, IOException, URISyntaxException {
        util.resetValidation();
        if (started)
            return;
        databaseUtil.init(false);
        {
            User user = new User();
            user.setFirstName("no_super_admin");
            user.setLastName("no_super_admin");
            user.setClient(clientRepository.findOne("portal-client-id"));
            user.setUsername("no_super_admin");
            user.setPassword("1");
            user.setCreationDate(new Date());
            user.setEnabled(true);
            user.setDeleted(false);
            user.setSuperAdmin(false);
            user.setRoles(new HashSet<>());
            userRepository.save(user);
        }
        {
            User user = new User();
            user.setFirstName("user");
            user.setLastName("user");
            user.setClient(clientRepository.findOne("portal-client-id"));
            user.setUsername("user");
            user.setPassword("1");
            user.setCreationDate(new Date());
            user.setEnabled(true);
            user.setDeleted(false);
            user.setSuperAdmin(false);
            user.setRoles(new HashSet<>(roleRepository.findAll()));
            userRepository.save(user);
        }
        started = true;
    }


    @Test
    public void testCheckPermissions() throws Exception {
        List<Permission> all = permissionRepository.findAll((Specification<Permission>) null);
        Assert.assertEquals(10, all.size());
        Assert.assertEquals(20, all.stream().mapToInt(value -> value.getUrls().size()).sum());
        List<APIConfig> list = new ArrayList<>();
        list.add(new APIConfig("get", "/api/users"));
        list.add(new APIConfig("post", "/api/users"));
        list.add(new APIConfig("delete", "/api/users"));
        list.add(new APIConfig("patch", "/api/users"));

        list.add(new APIConfig("get", "/api/users/32131f/asdas/"));
        list.add(new APIConfig("post", "/api/users/13/sd"));
        list.add(new APIConfig("delete", "/api/users/654/asd"));
        list.add(new APIConfig("patch", "/api/users/sadf54/654"));

        list.add(new APIConfig("get", "/api/roles"));
        list.add(new APIConfig("post", "/api/roles"));
        list.add(new APIConfig("delete", "/api/roles"));
        list.add(new APIConfig("patch", "/api/roles"));

        list.add(new APIConfig("get", "/api/roles/32131f/asdas/"));
        list.add(new APIConfig("post", "/api/roles/13/sd"));
        list.add(new APIConfig("delete", "/api/roles/654/asd"));
        list.add(new APIConfig("patch", "/api/roles/sadf54/654"));

        list.add(new APIConfig("get", "/api/permissions"));
        list.add(new APIConfig("post", "/api/permissions"));
        list.add(new APIConfig("delete", "/api/permissions"));
        list.add(new APIConfig("patch", "/api/permissions"));

        list.add(new APIConfig("get", "/api/permissions/32131f/asdas/"));
        list.add(new APIConfig("post", "/api/permissions/13/sd"));
        list.add(new APIConfig("delete", "/api/permissions/654/asd"));
        list.add(new APIConfig("patch", "/api/permissions/sadf54/654"));
        {
            ResponseEntity<List> admin = httpUtil.post("admin", "1", "/api/" + Constants.AUTHORIZES_URL, list, List.class);
            List<Map<String, Object>> body = admin.getBody();
            Assert.assertEquals(HttpStatus.OK.value(), admin.getStatusCode().value());
            for (Map<String, Object> o : body) {
                Assert.assertEquals(true, o.get("access"));
            }
        }
        {
            ResponseEntity<List> admin = httpUtil.post("no_super_admin", "1", "/api/" + Constants.AUTHORIZES_URL, list, List.class);
            List<Map<String, Object>> body = admin.getBody();
            Assert.assertEquals(HttpStatus.OK.value(), admin.getStatusCode().value());
            for (Map<String, Object> o : body) {
                Assert.assertEquals(o.get("method") + ":" + o.get("url"), false, o.get("access"));
            }
        }
        {
            ResponseEntity<List> admin = httpUtil.post("user", "1", "/api/" + Constants.AUTHORIZES_URL, list, List.class);
            List<Map<String, Object>> body = admin.getBody();
            Assert.assertEquals(HttpStatus.OK.value(), admin.getStatusCode().value());
            int index = 0;
            List<Integer> falses = Arrays.asList(5, 13, 17, 18, 19, 21, 22, 23);
            for (Map<String, Object> o : body) {
                System.out.println(o.get("access"));
                Assert.assertEquals(o.get("method") + ":" + o.get("url"), !falses.contains(index), o.get("access"));
                index++;
            }
        }
        {
            ResponseEntity<Map> post = httpUtil.post("user", "1", "/api/" + Constants.AUTHORIZE_URL, new APIConfig("get", "/api/roles"), Map.class);
            Map body = post.getBody();
            Assert.assertEquals(HttpStatus.OK.value(), post.getStatusCode().value());
            Assert.assertEquals("user", body.get("username"));

        }
        {
            try {
                httpUtil.post("user", "1", "/api/" + Constants.AUTHORIZE_URL, new APIConfig("post", "/api/users/13/sd"), String.class);
            } catch (HttpClientErrorException e) {
                Assert.assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode().value());
                Assert.assertThat(e.getResponseBodyAsString(), CoreMatchers.containsString("permission denied"));
            }
        }
    }
}
