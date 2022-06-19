//package com.peykasa.audit.auditserver.test;
//
//import com.peykasa.audit.AuditServerApplication;
//import com.peykasa.audit.auditserver.ConfigEntry;
//import com.peykasa.audit.auditserver.ConfigUtil;
//import com.peykasa.audit.domain.model.AuditModel;
//import com.peykasa.audit.exception.GlobalException;
//import com.peykasa.audit.service.AuditService;
//import com.peykasa.audit.service.elastic.ElasticConfig;
//import org.junit.*;
//import org.junit.runner.RunWith;
//import org.junit.runners.MethodSorters;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.*;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import javax.servlet.ServletException;
//import java.io.IOException;
//import java.net.URI;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author Yaser(amin) Sadeghi
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        classes = {
//                AuditServerApplication.class
//        })
//@AutoConfigureMockMvc
////@Ignore
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
//public class SearchControllerDistinctTest extends BaseTest {
//    private final static Logger LOGGER = LoggerFactory.getLogger(SearchControllerDistinctTest.class);
//    @Autowired
//    private ElasticConfig elasticConfig;
//    @Autowired
//    AuditService elasticService;
//    @Autowired
//    private MyElasticsearchTemplate elasticSearchTemplate;
//    @Autowired
//    private TestRestTemplate testRestTemplate;
//    @Autowired
//    private AuditService auditService;
//    private static List<AuditModel> list = new ArrayList<>();
//    private static Set<String> events = new TreeSet<>();
//    private static Set<String> contexts = new TreeSet<>();
//    private static Set<String> users = new TreeSet<>();
//    private static Map<String, Set<String>> eventByContext = new HashMap<>();
//    private String[] orig;
//    private static boolean started = false;
//    private static final String logUrl = "/api/log";
//    private static final String searchUrl = "/api/action/logs/search";
//
//    @BeforeClass
//    public static void initClass() throws IOException {
//        String simpleName = SearchControllerDistinctTest.class.getSimpleName();
//        ConfigEntry indexName = new ConfigEntry("app.audit.elastic.indexPrefix", simpleName.toLowerCase() + "-audit");
//        ConfigEntry templateName = new ConfigEntry("app.audit.elastic.templateName", simpleName.toLowerCase() + "-audit-template");
//        ConfigUtil.change(simpleName, indexName, templateName);
//        ConfigUtil.print(simpleName);
//    }
//
//    @Before
//    public void init() throws InterruptedException, IOException, ServletException, GlobalException {
//        securitySimulatedAdmin();
//        if (started)
//            return;
//        started = true;
//        insertData();
//    }
//
//    private void insertData() throws IOException, InterruptedException, GlobalException {
//        String indexName = elasticConfig.getIndexPrefix() + "*";
//        boolean b = elasticSearchTemplate.deleteIndex(indexName);
//        LOGGER.info("Deleting index {}={}", indexName, b);
//
//        Random r = new Random();
//        for (int i = 0; i < 15; i++) {
//            AuditModel e = new AuditModel();
//            e.setActor("actor-" + r.nextInt(3));
//            e.setContext("context-" + r.nextInt(3));
//            e.setEvent("event-" + r.nextInt(3));
//            e.setExtraInfo("extra-" + r.nextInt(3));
//            e.setRemoteAddress("addr-" + r.nextInt(3));
//            e.setStatus("stat-" + r.nextInt(3));
//            e.setTime(new Date());
//            users.add(e.getActor());
//            contexts.add(e.getContext());
//            events.add(e.getEvent());
//
//            Set<String> strings = eventByContext.get(e.getContext());
//            if (strings == null)
//                eventByContext.put(e.getContext(), new TreeSet<>());
//            eventByContext.get(e.getContext()).add(e.getEvent());
//            e.setFromState(new AbstractMap.SimpleEntry<>("f", "v" + i));
//            e.setToState(new AbstractMap.SimpleEntry<>("f", "v" + i));
//            list.add(e);
//            auditService.log(e);
//            TimeUnit.MILLISECONDS.sleep(500);
//        }
//
////        ArrayList<String> list = new ArrayList<>(users);
////        users =
////        Collections.sort(list);
////        Collections.sort(new ArrayList<>(events));
////        Collections.sort(new ArrayList<>(contexts));
////        list.sort(Comparator.comparing(AuditModel::getTime));
////        for (AuditModel model : list) {
////            send(logUrl, HttpMethod.POST, model, Boolean.class);
////        }
//    }
//
//
//    public ResponseEntity<List> get(String type) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
//        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
//        return testRestTemplate.exchange(new RequestEntity<>(headers, HttpMethod.GET, URI.create(searchUrl + "/" + type)), List.class);
//    }
//
//    public ResponseEntity<List> get1(String type, String filter) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
//        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
//        return testRestTemplate.exchange(new RequestEntity<>(headers, HttpMethod.GET, URI.create(searchUrl + "/" + type + "?context=" + filter)), List.class);
//    }
//
//    @AfterClass
//    public static void endClass() {
//    }
//
//    @After
//    public void after() {
//
//    }
//
//
//    @Test
//    public void testSearchEvent() throws Exception {
//        ResponseEntity<List> eventss = get("events");
//        Assert.assertEquals(eventss.getStatusCode(), HttpStatus.OK);
//        List<String> body = eventss.getBody();
//        body.sort(Comparator.naturalOrder());
//        Assert.assertEquals(body.size(), events.size());
//        Assert.assertEquals(body.toString(), events.toString());
//    }
//
//
//    @Test
//    public void testSearchContext() throws Exception {
//        ResponseEntity<List> contextss = get("contexts");
//        Assert.assertEquals(contextss.getStatusCode(), HttpStatus.OK);
//        List<String> body = contextss.getBody();
//        body.sort(Comparator.naturalOrder());
//        Assert.assertEquals(body.size(), contexts.size());
//        Assert.assertEquals(body.toString(), contexts.toString());
//    }
//
//    @Test
//    public void testSearchActor() throws Exception {
//        ResponseEntity<List> userss = get("users");
//        Assert.assertEquals(userss.getStatusCode(), HttpStatus.OK);
//        List<String> body = userss.getBody();
//        body.sort(Comparator.naturalOrder());
//        Assert.assertEquals(body.size(), users.size());
//        Assert.assertEquals(body.toString(), users.toString());
//    }
//
//    @Test
//    public void testSearchEventByContext() throws Exception {
//        String filter = new ArrayList<>(contexts).get(0);
//        ResponseEntity<List> resp = get1("eventsByContext", filter);
//        Assert.assertEquals(resp.getStatusCode(), HttpStatus.OK);
//        TreeSet body = new TreeSet<>(resp.getBody());
//
//        Assert.assertEquals(body.size(), eventByContext.get(filter).size());
//        Assert.assertEquals(body.toString(), eventByContext.get(filter).toString());
//    }
//}
