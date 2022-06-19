package com.peykasa.authserver.test;

import com.peykasa.authserver.*;
import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.model.entity.User;
import com.peykasa.authserver.repository.UserRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

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
public class LoginControllerTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(LoginControllerTest.class);
    @MockBean()
    private Auditor auditor;
    @Autowired
    private AuditServiceAspect auditServiceAspect;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseUtil databaseUtil;

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private HttpUtil httpUtil;

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
        if (started)
            return;
//        databaseUtil.init();
        started = true;
    }

    @AfterClass
    public static void endClass() {
    }

    @After
    public void after() {

    }

    @Test
    public void testLoginSuccessAndAudit() throws Exception {
        final Audit<Object, Map<String, Object>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        Map<String, Object> map = new HashMap<>();
        map.put("username", "admin");
        map.put("passwordExpired", false);
        Audit<Object, Map<String, Object>> expected = new Audit<>(null, map);
        expected.setContext(Constants.AUTH);
        expected.setEvent(Constants.LOGIN);
        expected.setStatus(Constants.SUCCESS);
        expected.setActor("admin");
        httpUtil.login("admin", "1");
        System.out.println(loginAudit[0].toString());
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testLoginFailedPasswordAndAudit() throws Exception {
        final Audit<AbstractMap.SimpleEntry<String, String>, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        httpUtil.login("admin", "2");
        Audit<Object, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(
                new AbstractMap.SimpleEntry<>("username", "admin"),
                new AbstractMap.SimpleEntry<>("msg", "Bad credentials"));
        expected.setContext(Constants.AUTH);
        expected.setEvent(Constants.LOGIN);
        expected.setStatus(Constants.FAIL);
        expected.setActor("admin");
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testLoginUsernameNotFoundAndAudit() throws Exception {
        final Audit<AbstractMap.SimpleEntry<String, String>, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        httpUtil.login("aa", "2");
        Audit<Object, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(
                new AbstractMap.SimpleEntry<>("username", "aa"),
                new AbstractMap.SimpleEntry<>("msg", "The username aa not found"));
        expected.setContext(Constants.AUTH);
        expected.setEvent(Constants.LOGIN);
        expected.setStatus(Constants.FAIL);
        expected.setActor(Constants.INVALID_USER);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testLoginPasswordExpireAndAudit() throws Exception {
        final Audit<Object, Map<String, Object>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        User admin = userRepository.fetchByUsername("admin");
        admin.setPassExpirationDate(new Date(new Date().getTime() - 10000));
        userRepository.save(admin);
        httpUtil.login("admin", "1");
        Map<String, Object> map = new HashMap<>();
        map.put("username", "admin");
        map.put("passwordExpired", true);
        Audit<Object, Map<String, Object>> expected = new Audit<>(null, map);
        expected.setContext(Constants.AUTH);
        expected.setEvent(Constants.LOGIN);
        expected.setStatus(Constants.SUCCESS);
        expected.setActor("admin");
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        admin.setPassExpirationDate(null);
        userRepository.save(admin);
    }

    @Test
    public void testLoginAccountLockAndAudit() throws Exception {
        final Audit<Object, Map<String, Object>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        User admin = userRepository.fetchByUsername("admin");
        Date blockDate = new Date(new Date().getTime() + 100000);
        admin.setBlockDate(blockDate);
        userRepository.save(admin);
        httpUtil.login("admin", "1");
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0").format(blockDate);
        Audit<AbstractMap.SimpleEntry, AbstractMap.SimpleEntry> expected = new Audit<>(new AbstractMap.SimpleEntry<>("username", "admin"),
                new AbstractMap.SimpleEntry<>("msg", "User account is locked until " + date));
        expected.setActor("admin");
        expected.setContext(Constants.AUTH);
        expected.setEvent(Constants.LOGIN);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        admin.setBlockDate(null);
        userRepository.save(admin);
    }

    @Test
    public void testLoginAccountDisableAndAudit() throws Exception {
        final Audit<Object, Map<String, Object>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        User admin = userRepository.fetchByUsername("admin");
        admin.setEnabled(false);
        userRepository.save(admin);
        httpUtil.login("admin", "1");
        Audit<AbstractMap.SimpleEntry, AbstractMap.SimpleEntry> expected = new Audit<>(new AbstractMap.SimpleEntry<>("username", "admin"),
                new AbstractMap.SimpleEntry<>("msg", "User is disabled"));
        expected.setActor("admin");
        expected.setContext(Constants.AUTH);
        expected.setEvent(Constants.LOGIN);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        admin.setEnabled(true);
        admin.setBlockDate(null);
        userRepository.save(admin);
    }

    private Audit normalAudit(Audit audit) {
        audit.setRemoteAddress(null);
        audit.setTime(null);
        LOGGER.warn(audit.toString());
        return audit;
    }
}
