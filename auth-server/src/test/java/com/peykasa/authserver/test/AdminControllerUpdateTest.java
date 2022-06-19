//package com.peykasa.authserver.test;
//
//import com.peykasa.authserver.*;
//import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
//import com.peykasa.authserver.audit.aspect.provider.dto.AuditUserDTO;
//import com.peykasa.authserver.audit.auditor.Audit;
//import com.peykasa.authserver.audit.auditor.Auditor;
//import com.peykasa.authserver.exception.RestErrorResponse;
//import com.peykasa.authserver.model.SimpleRole;
//import com.peykasa.authserver.model.cto.CreateUserCTO;
//import com.peykasa.authserver.model.cto.ExtendedUserCTO;
//import com.peykasa.authserver.model.cto.RoleCTO;
//import com.peykasa.authserver.model.dto.BaseDTO;
//import com.peykasa.authserver.model.dto.UserDTO;
//import com.peykasa.authserver.model.entity.Role;
//import com.peykasa.authserver.repository.RoleRepository;
//import com.peykasa.authserver.repository.UserRepository;
//import com.peykasa.authserver.validation.ValidationConfiguration;
//import org.junit.*;
//import org.junit.runner.RunWith;
//import org.junit.runners.MethodSorters;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static com.peykasa.authserver.Constants.*;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.doAnswer;
//
///**
// * @author Yaser(amin) Sadeghi
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        classes = {
//                AuthServerApplication.class
//        })
//@AutoConfigureMockMvc
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
//public class AdminControllerUpdateTest {
//    private final static Logger LOGGER = LoggerFactory.getLogger(AdminControllerUpdateTest.class);
//    @MockBean
//    private Auditor auditor;
//    @Autowired
//    private AuditServiceAspect auditServiceAspect;
//    @Autowired
//    private HttpUtil httpUtil;
//    @Autowired
//    private RoleRepository roleRepository;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private ValidationConfiguration validationConfiguration;
//    @Autowired
//    private DatabaseUtil databaseUtil;
//    @Autowired
//    private Util util;
//
//    private static boolean started = false;
//
//    @BeforeClass
//    public static void initClass() throws IOException {
//        Properties properties = ConfigUtil.loadConfig("application.properties");
//        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
//            System.err.println(entry.getKey() + "=" + entry.getValue());
//        }
//    }
//
//    @Before
//    public void init() throws InterruptedException, IOException {
//        util.resetValidation();
//        if (started)
//            return;
//        databaseUtil.init();
//        started = true;
//    }
//
//
//    @Test
//    public void testUpdateInvalid() throws Exception {
//        {
//            ResponseEntity<RestErrorResponse> admin = httpUtil.patch("admin", "1", Constants.ADMIN_CONTEXT_PATH + "/hhhh", null, RestErrorResponse.class);
//            Assert.assertEquals(admin.getStatusCode(), HttpStatus.BAD_REQUEST);
//            Assert.assertEquals(admin.getBody().getKey(), Constants.CLIENT_ERROR);
//        }
//        {
//            ResponseEntity<RestErrorResponse> admin1 = httpUtil.patch("admin", "1", Constants.ADMIN_CONTEXT_PATH + "/hhhh", new RoleCTO(), RestErrorResponse.class);
//            Assert.assertEquals(admin1.getStatusCode(), HttpStatus.BAD_REQUEST);
//            Assert.assertEquals(admin1.getBody().getKey(), Constants.VALIDATION_ERROR);
//            System.out.println(admin1.getBody().getMessages());
//        }
//        {
//            ResponseEntity<RestErrorResponse> admin1 = httpUtil.patch("admin", "1", Constants.ADMIN_CONTEXT_PATH + "/1", "test", RestErrorResponse.class);
//            Assert.assertEquals(admin1.getStatusCode(), HttpStatus.BAD_REQUEST);
//            Assert.assertEquals(admin1.getBody().getKey(), Constants.CLIENT_ERROR);
//            System.out.println(admin1.getBody().getMessages());
//        }
//    }
//
//    @Test
//    public void testUpdateInvalidIdAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        {
//            ExtendedUserCTO userCTO = new ExtendedUserCTO();
//            ResponseEntity<RestErrorResponse> update = update(userCTO, -1L);
//
//            Assert.assertEquals(update.getStatusCode(), HttpStatus.NOT_FOUND);
//            AuditUserDTO from = new AuditUserDTO();
//            from.setId(-1L);
//            Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, new AbstractMap.SimpleEntry<>("msg", "Empty id " + from.getId()));
//            expected.setActor("admin");
//            expected.setContext(Constants.USER);
//            expected.setEvent(Constants.UPDATE);
//            expected.setStatus(Constants.FAIL);
//            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//        }
//    }
//
//    @Test
//    public void testFail() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        {
//            ExtendedUserCTO userCTO = new ExtendedUserCTO();
//            ResponseEntity<RestErrorResponse> update = update(userCTO, (long) Integer.MAX_VALUE);
//
//            Assert.assertEquals(update.getStatusCode(), HttpStatus.NOT_FOUND);
//            AuditUserDTO from = new AuditUserDTO();
//            from.setId((long) Integer.MAX_VALUE);
//            Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, new AbstractMap.SimpleEntry<>("msg", "User with id " + from.getId() + " not found"));
//            expected.setActor("admin");
//            expected.setContext(Constants.USER);
//            expected.setEvent(Constants.UPDATE);
//            expected.setStatus(Constants.FAIL);
//            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//        }
//    }
//
//    @Test
//    public void testUpdateUserNotFoundAudit() throws Exception {
//        ResponseEntity<RestErrorResponse> update = httpUtil.patch("admin", "1", Constants.ADMIN_CONTEXT_PATH + "/" + 10, "{\"enabled\":\"dd\"}", RestErrorResponse.class);
//        Assert.assertEquals(update.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(update.getBody().getKey(), Constants.CLIENT_ERROR);
//        Assert.assertTrue(update.getBody().getMessages().get(0).contains("JSON parse error: Can not deserialize value of type java.lang.Boolean"));
//    }
//
//    @Test
//    public void testUpdateRoleNotFoundAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin";
//        UserDTO anotherUser = create(username, username);
//        {
//            ExtendedUserCTO userCTO = new ExtendedUserCTO();
//            userCTO.setRoles(new ArrayList<>());
//            userCTO.getRoles().add(new SimpleRole((long) Integer.MAX_VALUE));
//            ResponseEntity<RestErrorResponse> update = update(userCTO, anotherUser.getId());
//            RestErrorResponse body = update.getBody();
//            Assert.assertEquals(update.getStatusCode(), HttpStatus.NOT_FOUND);
//            Assert.assertEquals(body.getKey(), Constants.NOTE_FOUND_ERROR);
//
//            AuditUserDTO from = new AuditUserDTO();
//            from.setId(anotherUser.getId());
//            from.setRoles(new ArrayList<>());
//            from.getRoles().add(Integer.MAX_VALUE);
//            AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", "Some roles in list" + userCTO.getRoles().stream().map(BaseDTO::getId).sorted().collect(Collectors.toList()) + " not found");
//            Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//            expected.setActor("admin");
//            expected.setContext(Constants.USER);
//            expected.setEvent(Constants.UPDATE);
//            expected.setStatus(Constants.FAIL);
//            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//        }
//    }
//
//    @Test
//    public void testUpdateFirstNameFailAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_FirstName";
//        UserDTO anotherUser = create(username, username);
//        ExtendedUserCTO userCTO = new ExtendedUserCTO();
//        userCTO.setFirstName("    ");
//        ResponseEntity<RestErrorResponse> oneException = update(userCTO, anotherUser.getId());
//        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
//        Assert.assertEquals(oneException.getBody().getMessages().get(0), FIRST_NAME_EMPTY);
//        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", FIRST_NAME_EMPTY);
//        AuditUserDTO from = new AuditUserDTO();
//        from.setFirstName(userCTO.getFirstName());
//        from.setId(anotherUser.getId());
//        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//        expected.setActor("admin");
//        expected.setContext(Constants.USER);
//        expected.setEvent(Constants.UPDATE);
//        expected.setStatus(Constants.FAIL);
//        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//    }
//
//    @Test
//    public void testUpdateLastNameFailAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_LastName";
//        UserDTO anotherUser = create(username, username);
//        ExtendedUserCTO userCTO = new ExtendedUserCTO();
//        userCTO.setLastName("    ");
//        ResponseEntity<RestErrorResponse> oneException = update(userCTO, anotherUser.getId());
//        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
//        Assert.assertEquals(oneException.getBody().getMessages().get(0), LAST_NAME_EMPTY);
//        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", LAST_NAME_EMPTY);
//        AuditUserDTO from = new AuditUserDTO();
//        from.setLastName(userCTO.getLastName());
//        from.setId(anotherUser.getId());
//        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//        expected.setActor("admin");
//        expected.setContext(Constants.USER);
//        expected.setEvent(Constants.UPDATE);
//        expected.setStatus(Constants.FAIL);
//        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//    }
//
//    @Test
//    public void testUpdateLastNameFailEmptyAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_LastName_empty";
//        UserDTO anotherUser = create(username, username);
//        ExtendedUserCTO userCTO = new ExtendedUserCTO();
//        userCTO.setLastName("");
//        ResponseEntity<RestErrorResponse> oneException = update(userCTO, anotherUser.getId());
//        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
//        Assert.assertEquals(oneException.getBody().getMessages().get(0), LAST_NAME_EMPTY);
//        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", LAST_NAME_EMPTY);
//        AuditUserDTO from = new AuditUserDTO();
//        from.setLastName(userCTO.getLastName());
//        from.setId(anotherUser.getId());
//        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//        expected.setActor("admin");
//        expected.setContext(Constants.USER);
//        expected.setEvent(Constants.UPDATE);
//        expected.setStatus(Constants.FAIL);
//        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//    }
//
//    @Test
//    public void testUpdateUserNameFailAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_UserName";
//        UserDTO anotherUser = create(username, username);
//        ExtendedUserCTO userCTO = new ExtendedUserCTO();
//        userCTO.setUsername("    ");
//        ResponseEntity<RestErrorResponse> oneException = update(userCTO, anotherUser.getId());
//        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
//        Assert.assertEquals(oneException.getBody().getMessages().get(0), USER_NAME_EMPTY);
//        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", USER_NAME_EMPTY);
//        AuditUserDTO from = new AuditUserDTO();
//        from.setUsername(userCTO.getUsername());
//        from.setId(anotherUser.getId());
//        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//        expected.setActor("admin");
//        expected.setContext(Constants.USER);
//        expected.setEvent(Constants.UPDATE);
//        expected.setStatus(Constants.FAIL);
//        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//    }
//
//    @Test
//    public void testUpdatePasswordMinLenFailAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_Password_min";
//        UserDTO anotherUser = create(username, username);
//        validationConfiguration.getValidation().setMinLength(3);
//        ExtendedUserCTO userCTO = new ExtendedUserCTO();
//        userCTO.setPassword("12");
//        ResponseEntity<RestErrorResponse> oneException = update(userCTO, anotherUser.getId());
//        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
//        String msgEx = "Password must be 3 or more characters in length.";
//        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
//        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
//        AuditUserDTO from = new AuditUserDTO();
//        from.setUsername(userCTO.getUsername());
//        from.setId(anotherUser.getId());
//        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//        expected.setActor("admin");
//        expected.setContext(Constants.USER);
//        expected.setEvent(Constants.UPDATE);
//        expected.setStatus(Constants.FAIL);
//        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//    }
//
//    @Test
//    public void testUpdatePasswordMaxLenFailAudit() throws Exception {
//
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_Password_max";
//        UserDTO anotherUser = create(username, username);
//        validationConfiguration.getValidation().setMaxLength(5);
//        ExtendedUserCTO userCTO = new ExtendedUserCTO();
//        userCTO.setPassword("123456");
//        ResponseEntity<RestErrorResponse> oneException = update(userCTO, anotherUser.getId());
//        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
//        String msgEx = "Password must be no more than 5 characters in length.";
//        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
//        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
//        AuditUserDTO from = new AuditUserDTO();
//        from.setUsername(userCTO.getUsername());
//        from.setId(anotherUser.getId());
//        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//        expected.setActor("admin");
//        expected.setContext(Constants.USER);
//        expected.setEvent(Constants.UPDATE);
//        expected.setStatus(Constants.FAIL);
//        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//    }
//
//    @Test
//    public void testUpdatePasswordLowerFailAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_Password_lower";
//        UserDTO anotherUser = create(username, username);
//        validationConfiguration.getValidation().setHasLower(true);
//        ExtendedUserCTO userCTO = new ExtendedUserCTO();
//        userCTO.setPassword("TTTTTTTT");
//        ResponseEntity<RestErrorResponse> oneException = update(userCTO, anotherUser.getId());
//        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
//        String msgEx = "Password must contain 1 or more lowercase characters.";
//        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
//        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
//        AuditUserDTO from = new AuditUserDTO();
//        from.setUsername(userCTO.getUsername());
//        from.setId(anotherUser.getId());
//        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//        expected.setActor("admin");
//        expected.setContext(Constants.USER);
//        expected.setEvent(Constants.UPDATE);
//        expected.setStatus(Constants.FAIL);
//        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//    }
//
//    @Test
//    public void testUpdatePasswordUpperFailAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_Password_upper";
//        UserDTO anotherUser = create(username, username);
//        validationConfiguration.getValidation().setHasUpper(true);
//        ExtendedUserCTO userCTO = new ExtendedUserCTO();
//        userCTO.setPassword("tttttttt");
//        ResponseEntity<RestErrorResponse> oneException = update(userCTO, anotherUser.getId());
//        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
//        String msgEx = "Password must contain 1 or more uppercase characters.";
//        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
//        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
//        AuditUserDTO from = new AuditUserDTO();
//        from.setUsername(userCTO.getUsername());
//        from.setId(anotherUser.getId());
//        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//        expected.setActor("admin");
//        expected.setContext(Constants.USER);
//        expected.setEvent(Constants.UPDATE);
//        expected.setStatus(Constants.FAIL);
//        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//    }
//
//    @Test
//    public void testUpdatePasswordSpecialFailAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_Password_space";
//        UserDTO anotherUser = create(username, username);
//        validationConfiguration.getValidation().setHasSpecial(true);
//        ExtendedUserCTO userCTO = new ExtendedUserCTO();
//        userCTO.setPassword("tttttttt");
//        ResponseEntity<RestErrorResponse> oneException = update(userCTO, anotherUser.getId());
//        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
//        String msgEx = "Password must contain 1 or more special characters.";
//        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
//        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
//        AuditUserDTO from = new AuditUserDTO();
//        from.setUsername(userCTO.getUsername());
//        from.setId(anotherUser.getId());
//        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//        expected.setActor("admin");
//        expected.setContext(Constants.USER);
//        expected.setEvent(Constants.UPDATE);
//        expected.setStatus(Constants.FAIL);
//        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//    }
//
//    @Test
//    public void testUpdatePasswordDigitFailAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_Password_digit";
//        UserDTO anotherUser = create(username, username);
//        validationConfiguration.getValidation().setHasDigit(true);
//        ExtendedUserCTO userCTO = new ExtendedUserCTO();
//        userCTO.setPassword("tttttttt");
//        ResponseEntity<RestErrorResponse> oneException = update(userCTO, anotherUser.getId());
//        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
//        String msgEx = "Password must contain 1 or more digit characters.";
//        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
//        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
//        AuditUserDTO from = new AuditUserDTO();
//        from.setUsername(userCTO.getUsername());
//        from.setId(anotherUser.getId());
//        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//        expected.setActor("admin");
//        expected.setContext(Constants.USER);
//        expected.setEvent(Constants.UPDATE);
//        expected.setStatus(Constants.FAIL);
//        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//    }
//
//    @Test
//    public void testUpdatePasswordDuplicateNameFailAudit() throws Exception {
//        final Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_dupl";
//        UserDTO anotherUser = create(username, username);
//        validationConfiguration.getValidation().setHasDigit(true);
//        ExtendedUserCTO userCTO = new ExtendedUserCTO();
//        userCTO.setUsername("admin");
//        ResponseEntity<RestErrorResponse> oneException = update(userCTO, anotherUser.getId());
//        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.CONFLICT);
//        Assert.assertEquals(oneException.getBody().getKey(), Constants.DUPLICATE_ERROR);
//        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
//        String exMsg = "Duplicate user name admin";
//        Assert.assertEquals(oneException.getBody().getMessages().get(0), exMsg);
//        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", exMsg);
//        AuditUserDTO from = new AuditUserDTO();
//        from.setUsername(userCTO.getUsername());
//        from.setId(anotherUser.getId());
//        Audit<AuditUserDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(from, msg);
//        expected.setActor("admin");
//        expected.setContext(Constants.USER);
//        expected.setEvent(Constants.UPDATE);
//        expected.setStatus(Constants.FAIL);
//        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//    }
//
//    @Test
//    public void testUpdateSuccessAudit() throws Exception {
//        final Audit<AuditUserDTO, AuditUserDTO>[] loginAudit = new Audit[1];
//        doAnswer(invocationOnMock -> {
//            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
//            return true;
//        }).when(auditor).audit(any(Audit.class));
//        String username = "anotherWithAdmin_success";
//        UserDTO anotherUser = create(username, username);
//        Role savedRole = roleRepository.saveAndFlush(new Role(10L, username + "_test", username + "_test", new HashSet<>()));
//        {
//            ExtendedUserCTO userCTO = new ExtendedUserCTO();
//            userCTO.setFirstName(username + "_change");
//            ResponseEntity<UserDTO> created = updateSuccess(userCTO, anotherUser.getId());
//            UserDTO body = created.getBody();
//            Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
//            AuditUserDTO from = new AuditUserDTO();
//            from.setFirstName(username);
//            AuditUserDTO to = new AuditUserDTO();
//            from.setId(anotherUser.getId());
//            to.setId(anotherUser.getId());
//            to.setFirstName(body.getFirstName());
//            Audit<AuditUserDTO, AuditUserDTO> expected = new Audit<>(from, to);
//            expected.setActor("admin");
//            expected.setContext(Constants.USER);
//            expected.setEvent(Constants.UPDATE);
//            expected.setStatus(Constants.SUCCESS);
//            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//        }
//        {
//            ExtendedUserCTO userCTO = new ExtendedUserCTO();
//            userCTO.setLastName(username + "_change");
//            ResponseEntity<UserDTO> created = updateSuccess(userCTO, anotherUser.getId());
//            UserDTO body = created.getBody();
//            Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
//
//            AuditUserDTO from = new AuditUserDTO();
//            from.setLastName(username);
//
//            AuditUserDTO to = new AuditUserDTO();
//            to.setLastName(body.getLastName());
//            from.setId(anotherUser.getId());
//            to.setId(anotherUser.getId());
//            Audit<AuditUserDTO, AuditUserDTO> expected = new Audit<>(from, to);
//            expected.setActor("admin");
//            expected.setContext(Constants.USER);
//            expected.setEvent(Constants.UPDATE);
//            expected.setStatus(Constants.SUCCESS);
//            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//        }
//        {
//            ExtendedUserCTO userCTO = new ExtendedUserCTO();
//            userCTO.setUsername(username + "_change");
//            ResponseEntity<UserDTO> created = updateSuccess(userCTO, anotherUser.getId());
//            UserDTO body = created.getBody();
//            Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
//
//            AuditUserDTO from = new AuditUserDTO();
//            from.setUsername(username);
//
//            AuditUserDTO to = new AuditUserDTO();
//            to.setUsername(body.getLastName());
//            from.setId(anotherUser.getId());
//            to.setId(anotherUser.getId());
//            Audit<AuditUserDTO, AuditUserDTO> expected = new Audit<>(from, to);
//            expected.setActor("admin");
//            expected.setContext(Constants.USER);
//            expected.setEvent(Constants.UPDATE);
//            expected.setStatus(Constants.SUCCESS);
//            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//        }
//        {
//            ExtendedUserCTO userCTO = new ExtendedUserCTO();
//            userCTO.setPassword(username + "_change");
//            ResponseEntity<UserDTO> created = updateSuccess(userCTO, anotherUser.getId());
//            UserDTO body = created.getBody();
//            Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
//
//            AuditUserDTO from = new AuditUserDTO();
//            AuditUserDTO to = new AuditUserDTO();
//            from.setId(anotherUser.getId());
//            to.setId(anotherUser.getId());
//            Audit<AuditUserDTO, AuditUserDTO> expected = new Audit<>(from, to);
//            expected.setActor("admin");
//            expected.setContext(Constants.USER);
//            expected.setEvent(Constants.UPDATE);
//            expected.setStatus(Constants.SUCCESS);
//            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//        }
//        {
//            ExtendedUserCTO userCTO = new ExtendedUserCTO();
//            userCTO.setRoles(new ArrayList<>());
//            userCTO.getRoles().add(new SimpleRole(savedRole.getId()));
//            ResponseEntity<UserDTO> created = updateSuccess(userCTO, anotherUser.getId());
//            UserDTO body = created.getBody();
//            Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
//
//            AuditUserDTO from = new AuditUserDTO();
//            from.setRoles(new ArrayList<>());
//            from.getRoles().add("admin");
//
//            AuditUserDTO to = new AuditUserDTO();
//            to.setRoles(new ArrayList<>());
//            to.getRoles().addAll(body.getRoles().stream().map(SimpleRole::getName).collect(Collectors.toList()));
//            from.setId(anotherUser.getId());
//            to.setId(anotherUser.getId());
//            Audit<AuditUserDTO, AuditUserDTO> expected = new Audit<>(from, to);
//            expected.setActor("admin");
//            expected.setContext(Constants.USER);
//            expected.setEvent(Constants.UPDATE);
//            expected.setStatus(Constants.SUCCESS);
//            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
//        }
//    }
//
//
//    private ResponseEntity<RestErrorResponse> update(ExtendedUserCTO userCTO, Long id) {
//        return httpUtil.patch("admin", "1", Constants.ADMIN_CONTEXT_PATH + "/" + id, userCTO, RestErrorResponse.class);
//    }
//
//    private ResponseEntity<UserDTO> updateSuccess(ExtendedUserCTO userCTO, Long id) {
//        return httpUtil.patch("admin", "1", Constants.ADMIN_CONTEXT_PATH + "/" + id, userCTO, UserDTO.class);
//    }
//
//
//    private Audit normalAudit(Audit audit) {
//        audit.setRemoteAddress(null);
//        audit.setTime(null);
//        LOGGER.warn(audit.toString());
//        return audit;
//    }
//
//
//    private UserDTO create(String userName, String pass) {
//        CreateUserCTO userCTO = new CreateUserCTO();
//        userCTO.setFirstName(userName);
//        userCTO.setLastName(userName);
//        userCTO.setUsername(userName);
//        userCTO.setPassword(pass);
//        userCTO.setRoles(new ArrayList<>());
//        List<Role> all = roleRepository.findAll();
//        userCTO.getRoles().add(new SimpleRole(all.get(0).getId()));
//        ResponseEntity<UserDTO> created = createSuccess(userCTO);
//        Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
//        return created.getBody();
//    }
//
//    private ResponseEntity<UserDTO> createSuccess(CreateUserCTO userCTO) {
//        return httpUtil.post("admin", "1", Constants.USER_CONTEXT_PATH, userCTO, UserDTO.class);
//    }
//
//
//}
