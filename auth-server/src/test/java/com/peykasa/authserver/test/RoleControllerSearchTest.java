package com.peykasa.authserver.test;

import com.peykasa.authserver.*;
import com.peykasa.authserver.audit.aspect.AuditServiceAspect;
import com.peykasa.authserver.audit.aspect.provider.dto.AuditRoleDTO;
import com.peykasa.authserver.audit.aspect.provider.dto.SearchInputDTO;
import com.peykasa.authserver.audit.aspect.provider.dto.SearchOutputDTO;
import com.peykasa.authserver.audit.auditor.Audit;
import com.peykasa.authserver.audit.auditor.Auditor;
import com.peykasa.authserver.exception.RestErrorResponse;
import com.peykasa.authserver.model.dto.RoleDTO;
import com.peykasa.authserver.model.entity.Permission;
import com.peykasa.authserver.model.entity.Role;
import com.peykasa.authserver.repository.PermissionRepository;
import com.peykasa.authserver.repository.RoleRepository;
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
import org.springframework.data.jpa.domain.Specification;
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
public class RoleControllerSearchTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(RoleControllerSearchTest.class);
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
    private PermissionRepository permissionRepository;
    @Autowired
    private DatabaseUtil databaseUtil;
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
        if (started)
            return;
        databaseUtil.init();
        started = true;
    }

    @Test
    public void testFindAllRoleSuccessAudit() throws Exception {

        final Audit<SearchInputDTO, SearchOutputDTO>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        ResponseEntity<Map> all = findAll("0", "10", null);
        Assert.assertEquals(all.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(((Map) all.getBody().get("page")).get("totalElements"), 1);
        Assert.assertEquals(((List) all.getBody().get("content")).size(), 1);

        Audit<SearchInputDTO, SearchOutputDTO> expected = new Audit<>(new SearchInputDTO("0", "10", null), new SearchOutputDTO(1L, 1));
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.SEARCH);
        expected.setStatus(Constants.SUCCESS);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());

    }

    @Test
    public void testFindOneUserSuccessAudit() throws Exception {
        final Audit<SearchInputDTO, SearchOutputDTO>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));
        List<Role> all = roleRepository.findAll((Specification<Role>) null);
        Role admin = all.get(0);
        ResponseEntity<RoleDTO> one = findOne(admin.getId());
        Assert.assertEquals(one.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(one.getBody().getId(), admin.getId());
        AuditRoleDTO auditRoleDTO = new AuditRoleDTO();
        auditRoleDTO.setId(admin.getId());
        auditRoleDTO.setName("admin");
        auditRoleDTO.setDesc("admin");
        auditRoleDTO.setPermissions(permissionRepository.findAll((Specification<Permission>) null).stream()
                .map(Permission::getPermission).sorted().collect(Collectors.toList()));
        Audit<AbstractMap.SimpleEntry<String, Object>, AuditRoleDTO> expected = new Audit<>(new AbstractMap.SimpleEntry<>("id", admin.getId()), auditRoleDTO);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.SEARCH);
        expected.setStatus(Constants.SUCCESS);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());

    }

    @Test
    public void testFindOneUserFailAudit() throws Exception {
        final Audit<AbstractMap.SimpleEntry<String, Object>, AbstractMap.SimpleEntry<String, String>>[] loginAudit = new Audit[1];
        doAnswer(invocationOnMock -> {
            loginAudit[0] = normalAudit(invocationOnMock.getArgumentAt(0, Audit.class));
            return true;
        }).when(auditor).audit(any(Audit.class));

        String exMsg = String.format(Constants.ROLE_NOT_FOUND, Integer.MAX_VALUE);
        ResponseEntity<RestErrorResponse> oneException = findOneException((long) Integer.MAX_VALUE);
        Assert.assertEquals(oneException.getStatusCode(), HttpStatus.NOT_FOUND);
        Assert.assertEquals(oneException.getBody().getKey(), Constants.NOTE_FOUND_ERROR);
        Assert.assertEquals(oneException.getBody().getMessages().size(), 1);
        Assert.assertEquals(oneException.getBody().getMessages().get(0), exMsg);

        AbstractMap.SimpleEntry<String, Object> id = new AbstractMap.SimpleEntry<>("id", Integer.MAX_VALUE);
        AbstractMap.SimpleEntry<String, String> msg = new AbstractMap.SimpleEntry<>("msg", exMsg);
        Audit<AbstractMap.SimpleEntry<String, Object>, AbstractMap.SimpleEntry<String, String>> expected = new Audit<>(id, msg);
        expected.setActor("admin");
        expected.setContext(Constants.ROLE);
        expected.setEvent(Constants.SEARCH);
        expected.setStatus(Constants.FAIL);
        Assert.assertEquals(expected.toString(), loginAudit[0].toString());

    }

    private ResponseEntity<Map> findAll(String page, String size, String sort) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>() {{
            put("page", page);
            put("size", size);
            if (sort != null)
                put("sort", sort);
        }};
        return httpUtil.get("admin", "1", Constants.ROLE_CONTEXT_PATH, hashMap, Map.class);
    }

    private ResponseEntity<RoleDTO> findOne(Long id) {
        return httpUtil.get("admin", "1", Constants.ROLE_CONTEXT_PATH + "/" + id, new HashMap<>(), RoleDTO.class);
    }

    private ResponseEntity<RestErrorResponse> findOneException(Long id) {
        return httpUtil.get("admin", "1", Constants.ROLE_CONTEXT_PATH + "/" + id, new HashMap<>(), RestErrorResponse.class);
    }

    private Audit normalAudit(Audit audit) {
        audit.setRemoteAddress(null);
        audit.setTime(null);
        LOGGER.warn(audit.toString());
        return audit;
    }
}
