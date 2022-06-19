package com.peykasa.authserver.test;

import com.peykasa.authserver.*;
import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditUserDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.exception.RestErrorResponse;
import com.peykasa.authserver.model.SimpleRole;
import com.peykasa.authserver.model.cto.CreateUserCTO;
import com.peykasa.authserver.model.dto.UserDTO;
import com.peykasa.authserver.model.entity.Role;
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
import java.util.stream.Collectors;

import static com.peykasa.authserver.Constants.*;
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
public class UserControllerCreateTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserControllerCreateTest.class);
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
    public void testCreateSuccessAudit() throws Exception {
        final Audit<?, AuditUserDTO>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("test");
        userCTO.setPassword("ssssssssssssssssss");
        userCTO.setRoles(new ArrayList<>());
        List<Role> all = roleRepository.findAll();
        userCTO.getRoles().add(new SimpleRole(all.get(0).getId()));
        ResponseEntity<UserDTO> created = createSuccess(userCTO);
        Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
        AuditUserDTO to = new AuditUserDTO();
        UserDTO body = created.getBody();
        to.setId(body.getId());
        to.setUsername(body.getUsername());
        to.setFirstName(body.getFirstName());
        to.setLastName(body.getLastName());
        to.setEnabled(body.isEnabled());
        to.setRoles(body.getRoles().stream().map(SimpleRole::getName).sorted().collect(Collectors.toList()));

        Audit<?, AuditUserDTO> expected = new Audit<>(null, to);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.SUCCESS);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        userRepository.delete(body.getId());
    }

    @Test
    public void testJsonError() throws Exception {
        ResponseEntity<RestErrorResponse> oneException = httpUtil.post("admin", "1", Constants.USER_CONTEXT_PATH, "ttttttttttt", RestErrorResponse.class);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.CLIENT_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertTrue(oneException.getBody().getMessages().get(0).contains("Unrecognized token 'ttttttttttt'"));
    }

    @Test
    public void testJsonError1() throws Exception {
        ResponseEntity<RestErrorResponse> oneException = httpUtil.post("admin", "1", Constants.USER_CONTEXT_PATH, "{\"roles\":\"-1\"}", RestErrorResponse.class);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.CLIENT_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertTrue(oneException.getBody().getMessages().get(0).contains("JSON parse error: Can not deserialize instance of java.util.ArrayList "));
    }

    @Test
    public void testCreateFirstNameFailAudit() throws Exception {
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        CreateUserCTO userCTO = new CreateUserCTO();
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), FIRST_NAME_NULL);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", FIRST_NAME_NULL);
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(new AuditUserDTO(), msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreateLastNameFailAudit() throws Exception {
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), LAST_NAME_NULL);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", LAST_NAME_NULL);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreateUserNameFailAudit() throws Exception {
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), USER_NAME_NULL);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", USER_NAME_NULL);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        auditUserDTO.setLastName(userCTO.getLastName());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreateUserNameSpaceFailAudit() throws Exception {
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("te st");
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), SPACE_NOT);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", SPACE_NOT);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        auditUserDTO.setLastName(userCTO.getLastName());
        auditUserDTO.setUsername(userCTO.getUsername());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreatePasswordNullAudit() throws Exception {
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("test");
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), PASSWORD_NULL);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", PASSWORD_NULL);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        auditUserDTO.setLastName(userCTO.getLastName());
        auditUserDTO.setUsername(userCTO.getUsername());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreatePasswordRuleMinLengthAudit() throws Exception {
        validationConfiguration.getValidation().setMinLength(3);
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("test");
        userCTO.setPassword("te");
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must be 3 or more characters in length.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        auditUserDTO.setLastName(userCTO.getLastName());
        auditUserDTO.setUsername(userCTO.getUsername());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreatePasswordRuleMaxLengthAudit() throws Exception {
        validationConfiguration.getValidation().setMaxLength(5);
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("test");
        userCTO.setPassword("teeeeee");
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        System.err.println("===>" + oneException.getBody().getMessages());
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must be no more than 5 characters in length.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        auditUserDTO.setLastName(userCTO.getLastName());
        auditUserDTO.setUsername(userCTO.getUsername());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreatePasswordRuleLowerLengthAudit() throws Exception {
        validationConfiguration.getValidation().setHasLower(true);
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("test");
        userCTO.setPassword("TEEEE555EE");
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must contain 1 or more lowercase characters.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        auditUserDTO.setLastName(userCTO.getLastName());
        auditUserDTO.setUsername(userCTO.getUsername());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreatePasswordRuleUpperAudit() throws Exception {
        validationConfiguration.getValidation().setHasUpper(true);
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("test");
        userCTO.setPassword("teeee555ee");
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must contain 1 or more uppercase characters.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        auditUserDTO.setLastName(userCTO.getLastName());
        auditUserDTO.setUsername(userCTO.getUsername());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreatePasswordRuleSpecialAudit() throws Exception {
        validationConfiguration.getValidation().setHasSpecial(true);
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("test");
        userCTO.setPassword("teeee555ee");
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must contain 1 or more special characters.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        auditUserDTO.setLastName(userCTO.getLastName());
        auditUserDTO.setUsername(userCTO.getUsername());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreatePasswordRuleDigitAudit() throws Exception {
        validationConfiguration.getValidation().setHasDigit(true);
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("test");
        userCTO.setPassword("ssssssssssssssssss");
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must contain 1 or more digit characters.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        auditUserDTO.setLastName(userCTO.getLastName());
        auditUserDTO.setUsername(userCTO.getUsername());
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreateDuplicateUsernameAudit() throws Exception {
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("admin");
        userCTO.setPassword("ssssssssssssssssss");
        userCTO.setRoles(new ArrayList<>());
        userCTO.getRoles().add(new SimpleRole(555L));
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.CONFLICT);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.DUPLICATE_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Duplicate user name admin";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        auditUserDTO.setLastName(userCTO.getLastName());
        auditUserDTO.setUsername(userCTO.getUsername());
        auditUserDTO.setRoles(new ArrayList<>());
        auditUserDTO.getRoles().add(555);
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreateRoleNotFoundAudit() throws Exception {
        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        CreateUserCTO userCTO = new CreateUserCTO();
        userCTO.setFirstName("test");
        userCTO.setLastName("test");
        userCTO.setUsername("test");
        userCTO.setPassword("ssssssssssssssssss");
        userCTO.setRoles(new ArrayList<>());
        userCTO.getRoles().add(new SimpleRole(555L));
        ResponseEntity<RestErrorResponse> oneException = create(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.NOT_FOUND);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.NOTE_FOUND_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Some roles in list" + userCTO.getRoles().stream().map(SimpleRole::getId).sorted().collect(Collectors.toList()) + " not found";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditUserDTO auditUserDTO = new AuditUserDTO();
        auditUserDTO.setFirstName(userCTO.getFirstName());
        auditUserDTO.setLastName(userCTO.getLastName());
        auditUserDTO.setUsername(userCTO.getUsername());
        auditUserDTO.setRoles(new ArrayList<>());
        auditUserDTO.getRoles().add(555);
        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }


    private ResponseEntity<RestErrorResponse> create(CreateUserCTO userCTO) {
        return httpUtil.post("admin", "1", Constants.USER_CONTEXT_PATH, userCTO, RestErrorResponse.class);
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
