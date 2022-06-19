package com.peykasa.authserver.test;

import com.peykasa.authserver.*;
import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.exception.RestErrorResponse;
import com.peykasa.authserver.model.UserPassword;
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
import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;

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
public class UserControllerChangePassTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserControllerChangePassTest.class);
    @MockBean
    private Auditor auditor;
    @Autowired
    private AuditServiceAspect auditServiceAspect;
    @Autowired
    private HttpUtil httpUtil;


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
    public void testUpdatePasswordMinLenFailAudit() throws Exception {
        final Audit<?, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        validationConfiguration.getValidation().setMinLength(3);
        UserPassword userPassword = new UserPassword();
        userPassword.setNewPassword("12");
        userPassword.setConfirmPassword("12");
//        userPassword.setOldPassword("1");
        ResponseEntity<RestErrorResponse> oneException = update(userPassword);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must be 3 or more characters in length.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        Audit<?, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(null, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CHANGE_PASSWORD);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }


    @Test
    public void testUpdatePasswordMaxLenFailAudit() throws Exception {

        final Audit<?, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        validationConfiguration.getValidation().setMaxLength(5);
        UserPassword userCTO = new UserPassword();
        userCTO.setNewPassword("123456");
        userCTO.setConfirmPassword("123456");
//        userCTO.setOldPassword("1");
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must be no more than 5 characters in length.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        Audit<?, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(null, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CHANGE_PASSWORD);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    //
    @Test
    public void testUpdatePasswordLowerFailAudit() throws Exception {
        final Audit<?, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        validationConfiguration.getValidation().setHasLower(true);
        UserPassword userCTO = new UserPassword();
        userCTO.setNewPassword("TTTTTTTT");
        userCTO.setConfirmPassword("TTTTTTTT");
//        userCTO.setOldPassword("1");
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must contain 1 or more lowercase characters.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        Audit<?, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(null, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CHANGE_PASSWORD);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        validationConfiguration.getValidation().setHasLower(false);
    }

    @Test
    public void testUpdatePasswordUpperFailAudit() throws Exception {
        final Audit<?, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        validationConfiguration.getValidation().setHasUpper(true);
        UserPassword userCTO = new UserPassword();
        userCTO.setNewPassword("tttttttt");
        userCTO.setConfirmPassword("tttttttt");
//        userCTO.setOldPassword("1");
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must contain 1 or more uppercase characters.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        Audit<?, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(null, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CHANGE_PASSWORD);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        validationConfiguration.getValidation().setHasUpper(false);
    }

    @Test
    public void testUpdatePasswordSpecialFailAudit() throws Exception {
        final Audit<?, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        validationConfiguration.getValidation().setHasSpecial(true);
        UserPassword userCTO = new UserPassword();
        userCTO.setNewPassword("tttttttt");
        userCTO.setConfirmPassword("tttttttt");
//        userCTO.setOldPassword("1");
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must contain 1 or more special characters.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        Audit<?, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(null, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CHANGE_PASSWORD);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        validationConfiguration.getValidation().setHasSpecial(false);
    }

    @Test
    public void testUpdatePasswordDigitFailAudit() throws Exception {
        final Audit<?, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        validationConfiguration.getValidation().setHasDigit(true);
        UserPassword userCTO = new UserPassword();
        userCTO.setNewPassword("tttttttt");
        userCTO.setConfirmPassword("tttttttt");
//        userCTO.setOldPassword("1");
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Password must contain 1 or more digit characters.";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        Audit<?, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(null, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CHANGE_PASSWORD);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        validationConfiguration.getValidation().setHasDigit(true);
    }

    @Test
    public void testUpdatePasswordOldFailAudit() throws Exception {
        final Audit<?, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        UserPassword userCTO = new UserPassword();
        userCTO.setNewPassword("tttttttt");
        userCTO.setConfirmPassword("tttttttt");
//        userCTO.setOldPassword("2");
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = Constants.OLD_PASS_FAIL;
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        Audit<?, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(null, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CHANGE_PASSWORD);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }
@Test
    public void testUpdatePasswordOldAndNewSameAudit() throws Exception {
        final Audit<?, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        UserPassword userCTO = new UserPassword();
        userCTO.setNewPassword("1");
        userCTO.setConfirmPassword("1");
//        userCTO.setOldPassword("1");
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = Constants.OLD_AND_NEW_PASS_FAIL;
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        Audit<?, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(null, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CHANGE_PASSWORD);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testUpdatePasswordConfirmFailAudit() throws Exception {
        final Audit<?, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        UserPassword userCTO = new UserPassword();
        userCTO.setNewPassword("3");
        userCTO.setConfirmPassword("4");
//        userCTO.setOldPassword("1");
        ResponseEntity<RestErrorResponse> oneException = update(userCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = Constants.CONFIRM_PASSWORD_FAIL;
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        Audit<?, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(null, msg);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CHANGE_PASSWORD);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testUpdatePasswordOkAudit() throws Exception {
        final Audit<?, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        UserPassword userCTO = new UserPassword();
        userCTO.setNewPassword("3");
        userCTO.setConfirmPassword("3");
//        userCTO.setOldPassword("1");
        ResponseEntity<String> oneException = updateSuccess(userCTO);
        Assert.assertEquals(HttpStatus.NO_CONTENT, oneException.getStatusCode());

        Audit<?, ?> expected = new Audit<>(null, null);
        expected.setActor("admin");
        expected.setContext(Constants.USER);
        expected.setEvent(Constants.CHANGE_PASSWORD);
        expected.setStatus(Constants.SUCCESS);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        userCTO.setNewPassword("1");
        userCTO.setConfirmPassword("1");
//        userCTO.setOldPassword("3");
        oneException = updateSuccess("3",userCTO);
        Assert.assertEquals(HttpStatus.NO_CONTENT, oneException.getStatusCode());
    }

    private ResponseEntity<RestErrorResponse> update(UserPassword userPassword) {
        return httpUtil.post("admin", "1", Constants.USER_CONTEXT_PATH + Constants.USER_CHANGE_PASSWORD_PATH, userPassword, RestErrorResponse.class);
    }

    private ResponseEntity<String> updateSuccess(UserPassword userPassword) {
        return updateSuccess("1",userPassword);
    }

    private ResponseEntity<String> updateSuccess(String p, UserPassword userPassword) {
        return httpUtil.post("admin", p, Constants.USER_CONTEXT_PATH + Constants.USER_CHANGE_PASSWORD_PATH, userPassword, String.class);
    }


    private Audit normalAudit(Audit audit) {
        audit.setRemoteAddress(null);
        audit.setTime(null);
        LOGGER.warn(audit.toString());
        return audit;
    }


}
