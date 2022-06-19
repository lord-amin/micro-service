package com.peykasa.authserver.test;

import com.peykasa.authserver.*;
import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditRoleDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.exception.RestErrorResponse;
import com.peykasa.authserver.model.SimplePermission;
import com.peykasa.authserver.model.cto.RoleCTO;
import com.peykasa.authserver.model.dto.RoleDTO;
import com.peykasa.authserver.model.entity.Permission;
import com.peykasa.authserver.repository.PermissionRepository;
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
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
public class RoleControllerCreateTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(RoleControllerCreateTest.class);
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
    public void testCreateSuccessAudit() throws Exception {
        final Audit<?, AuditRoleDTO>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        RoleCTO roleCTO = new RoleCTO();
        roleCTO.setName("test");
        roleCTO.setDescription("test");
        List<Permission> all1 = permissionRepository.findAll((Specifications<Permission>) null);
        roleCTO.setPermissions(all1.stream().map(permission -> new SimplePermission(permission.getId(), permission.getPermission())).collect(Collectors.toList()));
        ResponseEntity<RoleDTO> created = createSuccess(roleCTO);
        Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
        AuditRoleDTO to = new AuditRoleDTO();
        RoleDTO body = created.getBody();
        to.setId(body.getId());
        to.setName(body.getName());
        to.setDesc(body.getDescription());
        to.setPermissions(body.getPermissions().stream().map(SimplePermission::getPermission).sorted().collect(Collectors.toList()));

        Audit<?, AuditRoleDTO> expected = new Audit<>(null, to);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.SUCCESS);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        roleRepository.delete(body.getId());
    }

    @Test
    public void testCreateNameFailAudit() throws Exception {
        final Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        RoleCTO roleCTO = new RoleCTO();
        ResponseEntity<RestErrorResponse> oneException = create(roleCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), Constants.ROLE_EMPTY);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", Constants.ROLE_EMPTY);
        AuditRoleDTO auditRoleDTO= new AuditRoleDTO();
        auditRoleDTO.setName(roleCTO.getName());
        Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditRoleDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }
    @Test
    public void testCreateNameSpaceFailAudit() throws Exception {
        final Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        RoleCTO roleCTO = new RoleCTO();
        roleCTO.setName("test     1");
        ResponseEntity<RestErrorResponse> oneException = create(roleCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), Constants.SPACE_NOT);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", Constants.SPACE_NOT);
        AuditRoleDTO auditRoleDTO= new AuditRoleDTO();
        auditRoleDTO.setName(roleCTO.getName());
        Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditRoleDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreateDuplicateRolenameAudit() throws Exception {
        final Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        RoleCTO roleCTO = new RoleCTO();
        roleCTO.setName("admin");
        ResponseEntity<RestErrorResponse> oneException = create(roleCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.CONFLICT);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.DUPLICATE_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Duplicate role name admin";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditRoleDTO auditUserDTO = new AuditRoleDTO();
        auditUserDTO.setName(roleCTO.getName());
        Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testCreatePermissionNotFoundAudit() throws Exception {
        final Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        RoleCTO roleCTO = new RoleCTO();
        roleCTO.setName("test");
        roleCTO.setPermissions(new ArrayList<>());
        roleCTO.getPermissions().add(new SimplePermission((long) Integer.MAX_VALUE,null));
        ResponseEntity<RestErrorResponse> oneException = create(roleCTO);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.NOT_FOUND);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.NOTE_FOUND_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Some permissions in list" + roleCTO.getPermissions().stream().map(SimplePermission::getId).sorted().collect(Collectors.toList()) + " not found";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditRoleDTO auditUserDTO = new AuditRoleDTO();
        auditUserDTO.setName(roleCTO.getName());
        auditUserDTO.setPermissions(new ArrayList<>());
        auditUserDTO.getPermissions().add(Integer.MAX_VALUE);
        Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.CREATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }


    private ResponseEntity<RestErrorResponse> create(RoleCTO roleCTO) {
        return httpUtil.post("admin", "1", Constants.ROLE_CONTEXT_PATH, roleCTO, RestErrorResponse.class);
    }

    private ResponseEntity<RoleDTO> createSuccess(RoleCTO roleCTO) {
        return httpUtil.post("admin", "1", Constants.ROLE_CONTEXT_PATH, roleCTO, RoleDTO.class);
    }


    private Audit normalAudit(Audit audit) {
        audit.setRemoteAddress(null);
        audit.setTime(null);
        LOGGER.warn(audit.toString());
        return audit;
    }
}
