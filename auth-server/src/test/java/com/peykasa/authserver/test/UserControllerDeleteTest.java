package com.peykasa.authserver.test;

import com.peykasa.authserver.*;
import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.exception.RestErrorResponse;
import com.peykasa.authserver.model.SimpleRole;
import com.peykasa.authserver.model.cto.CreateUserCTO;
import com.peykasa.authserver.model.dto.RoleDTO;
import com.peykasa.authserver.model.dto.UserDTO;
import com.peykasa.authserver.model.entity.Role;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.RoleRepository;
import com.peykasa.authserver.repository.UserRepository;
import com.peykasa.authserver.validation.ValidationConfiguration;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

import static com.peykasa.authserver.Constants.ACCESS_DENIED_ADMIN;
import static com.peykasa.authserver.Constants.CAN_NOT_DELETE_YOURSELF;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

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
public class UserControllerDeleteTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserControllerDeleteTest.class);
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
    private ValidationConfiguration validationConfiguration;
    @Autowired
    private DatabaseUtil databaseUtil;
    @Autowired
    private Util util;


    private static boolean started = false;

    @BeforeClass
    public static void initClass() throws IOException {
        Properties properties = ConfigUtil.loadConfig("application.properties");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            System.err.println(entry.getKey() + "=" + entry.getValue());
        }
//        String simpleName = LoginControllerTest.class.getSimpleName();
//        ConfigEntry indexName = new ConfigEntry("app.audit.elastic.indexPrefix", simpleName.toLowerCase() + "-audit");
//        ConfigEntry templateName = new ConfigEntry("app.audit.elastic.templateName", simpleName.toLowerCase() + "-audit-template");
//        ConfigUtil.change(simpleName, indexName, templateName);
//        ConfigUtil.print(simpleName);
    }

    @Before
    public void init() throws InterruptedException, IOException {
        util.resetValidation();
        if (started)
            return;
        databaseUtil.init();
        started = true;
    }


    @Test
    public void testDeleteSuccessAudit() throws Exception {
        final Audit<AbstractMap.SimpleEntry<String, Long>, ?>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("test");
        userCTO.setPassword("test");
        userCTO.setRoles(new ArrayList<>());
        List<Role> all = roleRepository.findAll();
        userCTO.getRoles().add(new SimpleRole(all.get(0).getId()));
        ResponseEntity<UserDTO> created = createSuccess(userCTO);
        Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
        ResponseEntity<String> deleted = delete(created.getBody().getId());
        Assert.assertEquals(deleted.getStatusCode(), HttpStatus.NO_CONTENT);
        Audit<AbstractMap.SimpleEntry<String, Long>, Object> expected =
                new Audit<>(new AbstractMap.SimpleEntry<>("id", created.getBody().getId()), null);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.DELETE);
        expected.setStatus(Constants.SUCCESS);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testDeleteUserFail() throws Exception {
        ResponseEntity<RestErrorResponse> deleteFail = httpUtil.delete("admin", "1", Constants.USER_CONTEXT_PATH + "/gggg", RestErrorResponse.class);
        Assert.assertEquals(deleteFail.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(deleteFail.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(deleteFail.getBody().getMessages().get(0), "gggg is not valid for id");
    }

    @Test
    public void testDeleteUserNotFoundAudit() throws Exception {
        final Audit<AbstractMap.SimpleEntry<String, Long>, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        ResponseEntity<RestErrorResponse> deleteFail = deleteFail((long) Integer.MAX_VALUE);
        Assert.assertEquals(deleteFail.getStatusCode(), HttpStatus.NOT_FOUND);
        Assert.assertEquals(deleteFail.getBody().getKey(), Constants.NOTE_FOUND_ERROR);
        String format = String.format(Constants.USER_NOT_FOUND, Integer.MAX_VALUE);
        Assert.assertEquals(deleteFail.getBody().getMessages().get(0), format);

        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", format);
        Audit<AbstractMap.SimpleEntry<String, Long>, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(new AbstractMap.SimpleEntry<>("id", (long) Integer.MAX_VALUE),
                msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.DELETE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testDeleteUserEmptyFoundAudit() throws Exception {
        final Audit<AbstractMap.SimpleEntry<String, Long>, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        ResponseEntity<RestErrorResponse> deleteFail = deleteFail((long) -1);
        Assert.assertEquals(deleteFail.getStatusCode(), HttpStatus.NOT_FOUND);
        Assert.assertEquals(deleteFail.getBody().getKey(), Constants.NOTE_FOUND_ERROR);
        String format = "Empty id -1";
        Assert.assertEquals(deleteFail.getBody().getMessages().get(0), format);

        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", format);
        Audit<AbstractMap.SimpleEntry<String, Long>, AbstractMap.SimpleEntry<String, String>> expected =
                new Audit<>(new AbstractMap.SimpleEntry<>("id", (long) -1),
                        msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.DELETE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testDeleteUserAdminAudit() throws Exception {
        final Audit<AbstractMap.SimpleEntry<String, Long>, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("not_admin");
        userCTO.setPassword("1");
        userCTO.setRoles(new ArrayList<>());
        List<Role> all = roleRepository.findAll();
        RoleDTO role = new RoleDTO();
        role.setId(all.get(0).getId());
        userCTO.getRoles().add(role);
        ResponseEntity<UserDTO> success = createSuccess(userCTO);
        Assert.assertEquals(success.getStatusCode(), HttpStatus.OK);
        User admin = userRepository.fetchByUsername("admin");
        ResponseEntity<RestErrorResponse> deleteFail = httpUtil.delete(userCTO.getUsername(), userCTO.getPassword(), Constants.USER_CONTEXT_PATH + "/" + admin.getId(), RestErrorResponse.class);
        Assert.assertEquals(deleteFail.getStatusCode(), HttpStatus.FORBIDDEN);
        Assert.assertEquals(deleteFail.getBody().getKey(), Constants.PERMISSION_DENIED_ERROR);
        Assert.assertEquals(deleteFail.getBody().getMessages().get(0), ACCESS_DENIED_ADMIN);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", "");

    }

    @Test
    public void testDeleteMeAudit() throws Exception {
        final Audit<AbstractMap.SimpleEntry<String, Long>, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        User admin = userRepository.fetchByUsername("admin");
        ResponseEntity<RestErrorResponse> deleteFail = deleteFail(admin.getId());

        Assert.assertEquals(deleteFail.getStatusCode(), HttpStatus.FORBIDDEN);
        Assert.assertEquals(deleteFail.getBody().getKey(), Constants.PERMISSION_DENIED_ERROR);
        Assert.assertEquals(deleteFail.getBody().getMessages().get(0), CAN_NOT_DELETE_YOURSELF);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", CAN_NOT_DELETE_YOURSELF);
        Audit<AbstractMap.SimpleEntry<String, Long>, AbstractMap.SimpleEntry<String, String>> expected =
                new Audit<>(new AbstractMap.SimpleEntry<>("id", admin.getId()),
                        msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.DELETE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testLogoutAfterDeleteAudit() throws Exception {
        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("after_delete");
        userCTO.setPassword("1");
        userCTO.setRoles(new ArrayList<>());
        List<Role> all = roleRepository.findAll();
        RoleDTO role = new RoleDTO();
        role.setId(all.get(0).getId());
        userCTO.getRoles().add(role);
        ResponseEntity<UserDTO> success = createSuccess(userCTO);
        Assert.assertEquals(success.getStatusCode(), HttpStatus.OK);
        String login = httpUtil.login(userCTO.getUsername(), userCTO.getPassword());
        System.out.println("token is : " + login);

        ResponseEntity<String> deleteUser = delete(success.getBody().getId());
//        Assert.assertEquals(deleteUser.getStatusCode(), HttpStatus.NO_CONTENT);
//        User admin = userRepository.findByUsername("admin");
        ResponseEntity<String> deleteAfterLogout = httpUtil.send(login, Constants.USER_CONTEXT_PATH + "/" + 10, String.class);

        Assert.assertEquals(deleteAfterLogout.getStatusCode(), HttpStatus.UNAUTHORIZED);
//        Assert.assertEquals(deleteAfterLogout.getBody().getMessages().get(0), HttpStatus.UNAUTHORIZED);

    }


    private ResponseEntity<RestErrorResponse> deleteFail(Long id) {
        return httpUtil.delete("admin", "1", Constants.USER_CONTEXT_PATH + "/" + id, RestErrorResponse.class);
    }

    private ResponseEntity<String> delete(Long id) {
        return httpUtil.delete("admin", "1", Constants.USER_CONTEXT_PATH + "/" + id, String.class);
    }

    private ResponseEntity<UserDTO> createSuccess(CreateUserCTO userCTO) {
        return httpUtil.post("admin", "1", Constants.USER_CONTEXT_PATH, userCTO, UserDTO.class);
    }


    private Audit normalAudit(Audit audit) {
        audit.setRemoteAddress(null);
        audit.setTime(null);
        LOGGER.warn(audit.toString());
        return audit;
    }
}
