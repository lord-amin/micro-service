package com.peykasa.audit.auditserver.test;

import com.peykasa.audit.AuditServerApplication;
import com.peykasa.audit.auditserver.ConfigEntry;
import com.peykasa.audit.auditserver.ConfigUtil;
import com.peykasa.audit.common.Constants;
import com.peykasa.audit.common.config.AppConfig;
import com.peykasa.audit.domain.model.AuditModel;
import com.peykasa.audit.domain.model.SearchCTO;
import com.peykasa.audit.exception.RestErrorResponse;
import com.peykasa.audit.service.AuditService;
import com.peykasa.audit.service.elastic.ElasticConfig;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Yaser(amin) Sadeghi
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                AuditServerApplication.class,
                AppConfig.class,
                ElasticConfig.class
        })
//@DataJpaTest()
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
//@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ReceiveControllerTest extends BaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(ReceiveControllerTest.class);
    @Autowired
    private AuditService elasticService;

    @Autowired
    private TestRestTemplate testRestTemplate;
    private static boolean started = false;
    private static final String logUrl = "/api/log";

    @BeforeClass
    public static void initClass() throws IOException {
        String simpleName = ReceiveControllerTest.class.getSimpleName();
        ConfigEntry indexName = new ConfigEntry("app.audit.elastic.indexPrefix", simpleName.toLowerCase() + "-audit");
        ConfigEntry templateName = new ConfigEntry("app.audit.elastic.templateName", simpleName.toLowerCase() + "-audit-template");
        ConfigUtil.change(simpleName, indexName, templateName);
        ConfigUtil.print(simpleName);
    }

    @Before
    public void init() throws InterruptedException, IOException, ServletException {
        super.securitySimulatedAdmin();
        if (started)
            return;
        started = true;
    }


    @AfterClass
    public static void endClass() {
    }

    @After
    public void after() {

    }

    @Test
    public void testLog() throws Exception {
        long l = System.currentTimeMillis();
        Date time = new Date(l);
        AuditModel auditModel = new AuditModel();
        auditModel.setActor("actor-" + l);
        auditModel.setContext("context-" + l);
        auditModel.setEvent("event-" + l);
        auditModel.setExtraInfo("سلام برادر");
        auditModel.setRemoteAddress("rm-" + l);
        auditModel.setStatus("stat-" + l);
        auditModel.setFromState(new AbstractMap.SimpleEntry<>("k1", "v1"));
        auditModel.setToState(new AbstractMap.SimpleEntry<>("k1", "v1"));
        auditModel.setTime(time);
        ResponseEntity<Boolean> send = send(auditModel, Boolean.class);
        Assert.assertEquals(send.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(send.getBody(), true);
        TimeUnit.SECONDS.sleep(3);
        SearchCTO searchCTO = new SearchCTO();
        searchCTO.setContext(auditModel.getContext());
        searchCTO.setSize(10);
        searchCTO.setPage(0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.HOUR, -1);
        searchCTO.setStart(calendar.getTime());
        calendar.add(Calendar.HOUR, 2);
        searchCTO.setEnd(calendar.getTime());
        Page<AuditModel> search = elasticService.search(searchCTO);
        Assert.assertEquals(search.getTotalElements(), 1);
        Assert.assertEquals(search.getContent().get(0).getEvent(), auditModel.getEvent());
    }

    @Test
    public void testLogNoActor() throws Exception {
        long l = System.currentTimeMillis();
        Date time = new Date(l);
        AuditModel auditModel = new AuditModel();
        auditModel.setContext("context-" + l);
        auditModel.setEvent("event-" + l);
        auditModel.setExtraInfo("extra-" + l);
        auditModel.setRemoteAddress("rm-" + l);
        auditModel.setStatus("stat-" + l);
        auditModel.setFromState(new AbstractMap.SimpleEntry<>("k1", "v1"));
        auditModel.setToState(new AbstractMap.SimpleEntry<>("k1", "v1"));
        auditModel.setTime(time);
        ResponseEntity<Boolean> send = send(auditModel, Boolean.class);
        Assert.assertEquals(send.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(true, send.getBody());
    }

    @Test
    public void testLogNoContext() throws Exception {
        long l = System.currentTimeMillis();
        Date time = new Date(l);
        AuditModel auditModel = new AuditModel();
        auditModel.setActor("actor-" + l);
        auditModel.setEvent("event-" + l);
        auditModel.setExtraInfo("extra-" + l);
        auditModel.setRemoteAddress("rm-" + l);
        auditModel.setStatus("stat-" + l);
        auditModel.setFromState(new AbstractMap.SimpleEntry<>("k1", "v1"));
        auditModel.setToState(new AbstractMap.SimpleEntry<>("k1", "v1"));
        auditModel.setTime(time);
        ResponseEntity<Boolean> send = send(auditModel, Boolean.class);
        Assert.assertEquals(send.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(true, send.getBody());
    }

    @Test
    public void testLogNoEvent() throws Exception {
        long l = System.currentTimeMillis();
        Date time = new Date(l);
        AuditModel auditModel = new AuditModel();
        auditModel.setActor("actor-" + l);
        auditModel.setContext("cont-" + l);
        auditModel.setExtraInfo("extra-" + l);
        auditModel.setRemoteAddress("rm-" + l);
        auditModel.setStatus("stat-" + l);
        auditModel.setFromState(new AbstractMap.SimpleEntry<>("k1", "v1"));
        auditModel.setToState(new AbstractMap.SimpleEntry<>("k1", "v1"));
        auditModel.setTime(time);
        ResponseEntity<Boolean> send = send(auditModel, Boolean.class);
        Assert.assertEquals(send.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(true, send.getBody());
    }

    @Test
    public void testLogNoTime() throws Exception {
        long l = System.currentTimeMillis();
        AuditModel auditModel = new AuditModel();
        auditModel.setActor("actor-" + l);
        auditModel.setContext("cont-" + l);
        auditModel.setEvent("ev-" + l);
        auditModel.setRemoteAddress("rm-" + l);
        auditModel.setStatus("stat-" + l);
        auditModel.setTime(null);
        auditModel.setFromState(new AbstractMap.SimpleEntry<>("k1", "v1"));
        auditModel.setToState(new AbstractMap.SimpleEntry<>("k1", "v1"));
        ResponseEntity<Boolean> send = send(auditModel, Boolean.class);
        Assert.assertEquals(send.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(true, send.getBody());
    }

    @Test
    public void testLogNoNested() throws Exception {
        long l = System.currentTimeMillis();
        Date time = new Date(l);
        AuditModel auditModel = new AuditModel();
        auditModel.setActor("actor-" + l);
        auditModel.setContext("cont-" + l);
        auditModel.setEvent("ev-" + l);
        auditModel.setRemoteAddress("rm-" + l);
        auditModel.setStatus("stat-" + l);
        auditModel.setFromState("from");
        auditModel.setToState("from");
        auditModel.setTime(time);
        ResponseEntity<RestErrorResponse> send = send(auditModel, RestErrorResponse.class);
        Assert.assertEquals(send.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        Assert.assertEquals(send.getBody().getKey(), Constants.UNHANDLED_ERROR);
        Assert.assertEquals(send.getBody().getMessages().get(0), "object mapping for [fromState] tried to parse field [fromState] as object, but found a concrete value");
    }

    public <T> ResponseEntity<T> send(AuditModel auditModel, Class<T> ret) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(auditModel, headers);
        return testRestTemplate.exchange(
                logUrl,
                HttpMethod.POST,
                entity,
                ret);

    }
}
