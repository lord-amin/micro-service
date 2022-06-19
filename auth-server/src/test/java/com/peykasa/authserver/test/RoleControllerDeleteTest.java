package com.peykasa.authserver.test;

import com.peykasa.authserver.*;
import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.exception.RestErrorResponse;
import com.peykasa.authserver.model.entity.Permission;
import com.peykasa.authserver.model.entity.Role;
import com.peykasa.authserver.repository.PermissionRepository;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
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
public class RoleControllerDeleteTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(RoleControllerDeleteTest.class);
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
    @Autowired
    private PermissionRepository permissionRepository;


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
        Role roleCTO = new Role();
        roleCTO.setName("delete_role");
        roleCTO.setDescription("delete_role");
        roleCTO.setPermissions(new HashSet<>(permissionRepository.findAll((Specification<Permission>) null)));
        Role save = roleRepository.save(roleCTO);

        ResponseEntity<String> deleted = delete(save.getId());
        Assert.assertEquals(deleted.getStatusCode(), HttpStatus.NO_CONTENT);
        Audit<AbstractMap.SimpleEntry<String, Long>, Object> expected =
                new Audit<>(new AbstractMap.SimpleEntry<>("id", save.getId()), null);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.DELETE);
        expected.setStatus(Constants.SUCCESS);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testDeleteRoleNotFoundAudit() throws Exception {
        final Audit<AbstractMap.SimpleEntry<String, Long>, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        ResponseEntity<RestErrorResponse> deleteFail = deleteFail((long) Integer.MAX_VALUE);
        Assert.assertEquals(deleteFail.getStatusCode(), HttpStatus.NOT_FOUND);
        Assert.assertEquals(deleteFail.getBody().getKey(), Constants.NOTE_FOUND_ERROR);
        String format = String.format(Constants.ROLE_NOT_FOUND, Integer.MAX_VALUE);
        Assert.assertEquals(deleteFail.getBody().getMessages().get(0), format);

        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", format);
        Audit<AbstractMap.SimpleEntry<String, Long>, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(new AbstractMap.SimpleEntry<>("id", (long) Integer.MAX_VALUE),
                msg);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.DELETE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testDeleteROleEmptyFoundAudit() throws Exception {
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
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.DELETE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testDeleteRoleAssignedFoundAudit() throws Exception {
        final Audit<AbstractMap.SimpleEntry<String, Long>, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        List<Role> all = roleRepository.findAll((Specification<Role>) null);
        ResponseEntity<RestErrorResponse> deleteFail = deleteFail(all.get(0).getId());
        Assert.assertEquals(deleteFail.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);
        Assert.assertEquals(deleteFail.getBody().getKey(), Constants.RELATION_ERROR);
        String format = "Could not delete role ,role id " + all.get(0).getId() + " assigned to the some users";
        Assert.assertEquals(deleteFail.getBody().getMessages().get(0), format);

        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", format);
        Audit<AbstractMap.SimpleEntry<String, Long>, AbstractMap.SimpleEntry<String, String>> expected =
                new Audit<>(new AbstractMap.SimpleEntry<>("id", all.get(0).getId()),
                        msg);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.DELETE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }


    private ResponseEntity<RestErrorResponse> deleteFail(Long id) {
        return httpUtil.delete("admin", "1", Constants.ROLE_CONTEXT_PATH + "/" + id, RestErrorResponse.class);
    }

    private ResponseEntity<String> delete(Long id) {
        return httpUtil.delete("admin", "1", Constants.ROLE_CONTEXT_PATH + "/" + id, String.class);
    }

    private Audit normalAudit(Audit audit) {
        audit.setRemoteAddress(null);
        audit.setTime(null);
        LOGGER.warn(audit.toString());
        return audit;
    }
}
