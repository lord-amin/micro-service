package com.peykasa.authserver.test;

import com.peykasa.authserver.*;
import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditUserDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.exception.RestErrorResponse;
import com.peykasa.authserver.model.SimpleRole;
import com.peykasa.authserver.model.cto.UserCTO;
import com.peykasa.authserver.model.dto.UserDTO;
import com.peykasa.authserver.model.entity.Role;
import com.peykasa.authserver.repository.RoleRepository;
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
import java.util.stream.Collectors;

import static com.peykasa.authserver.Constants.FIRST_NAME_EMPTY;
import static com.peykasa.authserver.Constants.LAST_NAME_EMPTY;
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
public class UserControllerUpdateTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserControllerUpdateTest.class);
    @MockBean
    private Auditor auditor;
    @Autowired
    private AuditServiceAspect auditServiceAspect;
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
    public void testUpdateUserSuccessAudit() throws Exception {
        final Audit<AuditUserDTO, AuditUserDTO>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        Role savedRole = roleRepository.saveAndFlush(new Role(10L, "test", "test", new HashSet<>()));
        {
            UserCTO userCTO = new UserCTO();
            userCTO.setFirstName("test");
            ResponseEntity<UserDTO> created = updateSuccess(userCTO);
            UserDTO body = created.getBody();
            Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
            AuditUserDTO from = new AuditUserDTO();
            from.setFirstName("admin");
            AuditUserDTO to = new AuditUserDTO();
            to.setFirstName(body.getFirstName());
            Audit<AuditUserDTO, AuditUserDTO> expected = new Audit<>(from, to);
            expected.setActor("admin");
            expected.setContext(Constants.USER);
            expected.setEvent(Constants.UPDATE);
            expected.setStatus(Constants.SUCCESS);
            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        }
        {
            UserCTO userCTO = new UserCTO();
            userCTO.setRoles(new ArrayList<>());
            userCTO.getRoles().add(new SimpleRole(savedRole.getId()));
            ResponseEntity<UserDTO> created = updateSuccess(userCTO);
            UserDTO body = created.getBody();
            Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);

            AuditUserDTO from = new AuditUserDTO();
            from.setRoles(new ArrayList<>());
            from.getRoles().add("admin");

            AuditUserDTO to = new AuditUserDTO();
            to.setRoles(new ArrayList<>());
            to.getRoles().addAll(body.getRoles().stream().map(SimpleRole::getName).collect(Collectors.toList()));

            Audit<AuditUserDTO, AuditUserDTO> expected = new Audit<>(from, to);
            expected.setActor("admin");
            expected.setContext(Constants.USER);
            expected.setEvent(Constants.UPDATE);
            expected.setStatus(Constants.SUCCESS);
            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        }
        {
            UserCTO userCTO = new UserCTO();
            userCTO.setLastName("test");
            ResponseEntity<UserDTO> created = updateSuccess(userCTO);
            UserDTO body = created.getBody();
            Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);

            AuditUserDTO from = new AuditUserDTO();
            from.setLastName("admin");

            AuditUserDTO to = new AuditUserDTO();
            to.setLastName(body.getLastName());

            Audit<AuditUserDTO, AuditUserDTO> expected = new Audit<>(from, to);
            expected.setActor("admin");
            expected.setContext(Constants.USER);
            expected.setEvent(Constants.UPDATE);
            expected.setStatus(Constants.SUCCESS);
            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        }
    }

    @Test
    public void testUpdateUserRoleNotFoundAudit() throws Exception {
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        UserCTO userCTO = new UserCTO();
        userCTO.setRoles(new ArrayList<>());
        userCTO.getRoles().add(new SimpleRole(555L));
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.NOT_FOUND);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.NOTE_FOUND_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Some roles in list" + userCTO.getRoles().stream().map(SimpleRole::getId).sorted().collect(Collectors.toList()) + " not found";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setRoles(new ArrayList<>());
        auditUserDTO.getRoles().add(555);
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.UPDATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testUpdateUserFirstNameFailAudit() throws Exception {
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        UserCTO userCTO = new UserCTO();
        userCTO.setFirstName("    ");
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), FIRST_NAME_EMPTY);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", FIRST_NAME_EMPTY);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.UPDATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreateUserLastNameFailAudit() throws Exception {
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        UserCTO userCTO = new UserCTO();
        userCTO.setLastName("  ");
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), LAST_NAME_EMPTY);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", LAST_NAME_EMPTY);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setLastName(userCTO.getLastName());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.UPDATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreateUserLastNameEmptyAudit() throws Exception {
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        UserCTO userCTO = new UserCTO();
        userCTO.setLastName("");
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), LAST_NAME_EMPTY);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", LAST_NAME_EMPTY);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setLastName(userCTO.getLastName());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.UPDATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }


    private ResponseEntity<RestErrorResponse> update(UserCTO userCTO) {
        return httpUtil.patch("admin", "1", Constants.USER_CONTEXT_PATH, userCTO, RestErrorResponse.class);
    }

    private ResponseEntity<UserDTO> updateSuccess(UserCTO userCTO) {
        return httpUtil.patch("admin", "1", Constants.USER_CONTEXT_PATH, userCTO, UserDTO.class);
    }


    private Audit normalAudit(Audit audit) {
        audit.setRemoteAddress(null);
        audit.setTime(null);
        LOGGER.warn(audit.toString());
        return audit;
    }


}
