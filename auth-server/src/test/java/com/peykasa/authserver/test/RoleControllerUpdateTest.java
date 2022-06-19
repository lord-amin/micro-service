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
import java.util.stream.Collectors;

import static com.peykasa.authserver.Constants.ROLE_BLANK;
import static com.peykasa.authserver.Constants.SPACE_NOT;
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
public class RoleControllerUpdateTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(RoleControllerUpdateTest.class);
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
    public void testUpdateRoleSuccessAudit() throws Exception {
        final Audit<AuditRoleDTO, AuditRoleDTO>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        List<Permission> all = permissionRepository.findAll((Specification<Permission>) null);
        List<Role> all1 = roleRepository.findAll();
        Long id = all1.get(0).getId();
        {
            RoleCTO roleCTO = new RoleCTO();
            roleCTO.setName("test");
            ResponseEntity<RoleDTO> created = updateSuccess(roleCTO, id);
            RoleDTO body = created.getBody();
            Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
            AuditRoleDTO from = new AuditRoleDTO();
            from.setName("admin");
            AuditRoleDTO to = new AuditRoleDTO();
            to.setName(body.getName());
            from.setId(id);
            to.setId(id);
            Audit<AuditRoleDTO, AuditRoleDTO> expected = new Audit<>(from, to);
            expected.setActor("admin");
            expected.setContext(Constants.ROLE);
            expected.setEvent(Constants.UPDATE);
            expected.setStatus(Constants.SUCCESS);
            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        }
        {
            RoleCTO roleCTO = new RoleCTO();
            roleCTO.setPermissions(new ArrayList<>());
            roleCTO.getPermissions().add(new SimplePermission(all.get(0).getId(), null));
            ResponseEntity<RoleDTO> created = updateSuccess(roleCTO, id);
            RoleDTO body = created.getBody();
            Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);

            AuditRoleDTO from = new AuditRoleDTO();
            from.setPermissions(new ArrayList<>());
            from.getPermissions().addAll(all.stream().map(Permission::getPermission).sorted().collect(Collectors.toList()));

            AuditRoleDTO to = new AuditRoleDTO();
            to.setPermissions(new ArrayList<>());
            to.getPermissions().add(body.getPermissions().get(0).getPermission());
            from.setId(id);
            to.setId(id);
            Audit<AuditRoleDTO, AuditRoleDTO> expected = new Audit<>(from, to);
            expected.setActor("admin");
            expected.setContext(Constants.ROLE);
            expected.setEvent(Constants.UPDATE);
            expected.setStatus(Constants.SUCCESS);
            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        }
        {
            RoleCTO roleCTO = new RoleCTO();
            roleCTO.setDescription("test");
            ResponseEntity<RoleDTO> created = updateSuccess(roleCTO, id);
            RoleDTO body = created.getBody();
            Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
            AuditRoleDTO from = new AuditRoleDTO();
            from.setDesc("admin");
            AuditRoleDTO to = new AuditRoleDTO();
            to.setDesc(body.getDescription());
            from.setId(id);
            to.setId(id);
            Audit<AuditRoleDTO, AuditRoleDTO> expected = new Audit<>(from, to);
            expected.setActor("admin");
            expected.setContext(Constants.ROLE);
            expected.setEvent(Constants.UPDATE);
            expected.setStatus(Constants.SUCCESS);
            Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        }
        RoleCTO roleCTO = new RoleCTO();
        roleCTO.setName("admin");
        roleCTO.setDescription("admin");
        roleCTO.setPermissions(new ArrayList<>());
        roleCTO.getPermissions().addAll(all.stream().map(permission -> new SimplePermission(permission.getId(), null)).collect(Collectors.toList()));
        ResponseEntity<RoleDTO> created = updateSuccess(roleCTO, id);
        Assert.assertEquals(created.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void testUpdateUserFirstNameFailAudit() throws Exception {
        final Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        List<Role> all = roleRepository.findAll((Specification<Role>) null);
        Long id = all.get(0).getId();
        RoleCTO roleCTO = new RoleCTO();
        roleCTO.setName("");
        ResponseEntity<RestErrorResponse> oneException = update(roleCTO, id);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), ROLE_BLANK);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", ROLE_BLANK);
        AuditRoleDTO auditUserDTO = new AuditRoleDTO();
        auditUserDTO.setName(roleCTO.getName());
        auditUserDTO.setId(id);
        Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.UPDATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
        roleCTO.setName("test    1");
        oneException = update(roleCTO, id);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.BAD_REQUEST);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.VALIDATION_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), SPACE_NOT);
        msg = new AbstractMap.SimpleEntry<>("msg", SPACE_NOT);
        auditUserDTO = new AuditRoleDTO();
        auditUserDTO.setName(roleCTO.getName());
        auditUserDTO.setId(id);
        expected = new Audit<>(auditUserDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.UPDATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testUpdateRolePermissionNotFoundAudit() throws Exception {
        final Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        List<Role> all = roleRepository.findAll((Specification<Role>) null);
        Long id = all.get(0).getId();
        RoleCTO userCTO = new RoleCTO();
        userCTO.setPermissions(new ArrayList<>());
        userCTO.getPermissions().add(new SimplePermission((long) Integer.MAX_VALUE, null));
        ResponseEntity<RestErrorResponse> oneException = update(userCTO, id);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.NOT_FOUND);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.NOTE_FOUND_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Some permissions in list" + userCTO.getPermissions().stream().map(SimplePermission::getId).sorted().collect(Collectors.toList()) + " not found";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditRoleDTO auditRoleDTO = new AuditRoleDTO();
        auditRoleDTO.setId(id);
        auditRoleDTO.setPermissions(new ArrayList<>());
        auditRoleDTO.getPermissions().add(Integer.MAX_VALUE);
        Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditRoleDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.UPDATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    @Test
    public void testUpdateDuplicateRoleNameAudit() throws Exception {
        final Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        List<Role> all = roleRepository.findAll((Specification<Role>) null);
        Role created = roleRepository.saveAndFlush(new Role(null, "testi", "testi", new HashSet<>()));
        Long id = all.get(0).getId();
        RoleCTO roleCTO = new RoleCTO();
        roleCTO.setName(created.getName());
        ResponseEntity<RestErrorResponse> oneException = update(roleCTO, id);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.CONFLICT);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.DUPLICATE_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        String msgEx = "Duplicate role name testi";
        Assert.assertEquals(oneException.getBody().getMessages().get(0), msgEx);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", msgEx);
        AuditRoleDTO auditRoleDTO = new AuditRoleDTO();
        auditRoleDTO.setId(id);
        auditRoleDTO.setName("testi");
        Audit<AuditRoleDTO, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(auditRoleDTO, msg);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.UPDATE);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());
    }

    private ResponseEntity<RestErrorResponse> update(RoleCTO roleCTO, Long id) {
        return httpUtil.patch("admin", "1", Constants.ROLE_CONTEXT_PATH + "/" + id, roleCTO, RestErrorResponse.class);
    }

    private ResponseEntity<RoleDTO> updateSuccess(RoleCTO roleCTO, Long id) {
        return httpUtil.patch("admin", "1", Constants.ROLE_CONTEXT_PATH + "/" + id, roleCTO, RoleDTO.class);
    }


    private Audit normalAudit(Audit audit) {
        audit.setRemoteAddress(null);
        audit.setTime(null);
        LOGGER.warn(audit.toString());
        return audit;
    }


}
