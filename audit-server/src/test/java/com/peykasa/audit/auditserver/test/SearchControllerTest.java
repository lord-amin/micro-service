//package com.peykasa.audit.auditserver.test;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.peykasa.audit.AuditServerApplication;
//import com.peykasa.audit.auditserver.ConfigEntry;
//import com.peykasa.audit.auditserver.ConfigUtil;
//import com.peykasa.audit.commom.Messages;
//import com.peykasa.audit.common.Constants;
//import com.peykasa.audit.common.config.AppConfig;
//import com.peykasa.audit.domain.model.AuditModel;
//import com.peykasa.audit.domain.model.Order;
//import com.peykasa.audit.domain.model.SearchCTO;
//import com.peykasa.audit.exception.RestErrorResponse;
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
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.*;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import javax.servlet.ServletException;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URL;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author Yaser(amin) Sadeghi
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        classes = {
//                AuditServerApplication.class,
//                AppConfig.class,
//                ElasticConfig.class
//        })
////@DataJpaTest()
////@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@AutoConfigureMockMvc
////@Ignore
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
//public class SearchControllerTest  extends BaseTest{
//    private final static Logger LOGGER = LoggerFactory.getLogger(SearchControllerTest.class);
//    @Autowired
//    private ElasticConfig elasticConfig;
//    @Autowired
//    AuditService elasticService;
//    @Autowired
//    private MyElasticsearchTemplate elasticSearchTemplate;
//    @Autowired
//    private TestRestTemplate testRestTemplate;
//    private String[] orig;
//    private static boolean started = false;
//    private static final String logUrl = "/api/log";
//
//    @BeforeClass
//    public static void initClass() throws IOException {
//        String simpleName = SearchControllerTest.class.getSimpleName();
//        ConfigEntry indexName = new ConfigEntry("app.audit.elastic.indexPrefix", simpleName.toLowerCase() + "-audit");
//        ConfigEntry templateName = new ConfigEntry("app.audit.elastic.templateName", simpleName.toLowerCase() + "-audit-template");
//        ConfigUtil.change(simpleName, indexName, templateName);
//        ConfigUtil.print(simpleName);
//    }
//
//    @Before
//    public void init() throws InterruptedException, IOException, ServletException {
//        securitySimulatedAdmin();
//        if (started)
//            return;
//        insertData();
//        started = true;
//    }
//
//    private void insertData() throws IOException, InterruptedException {
//        String indexName = elasticConfig.getIndexPrefix() + "*";
//        boolean b = elasticSearchTemplate.deleteIndex(indexName);
//        LOGGER.info("Deleting index {}={}", indexName, b);
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        URL resource = SearchControllerTest.class.getResource("/audit.json");
//        List<AuditModel> list = mapper.readValue(resource, mapper.getTypeFactory().constructCollectionType(List.class, AuditModel.class));
//        list.sort(Comparator.comparing(AuditModel::getTime));
//        for (AuditModel model : list) {
//            send(logUrl, HttpMethod.POST, model, Boolean.class);
//            TimeUnit.SECONDS.sleep(1);
//        }
//    }
//
//    public <T> ResponseEntity<T> send(String url, HttpMethod method, AuditModel dto, Class<T> clazz) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
//        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
//        return testRestTemplate.exchange(new RequestEntity<>(dto, headers, method, URI.create(url)), clazz);
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
//    @Test
//    public void testSearchAll() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        searchCTO.setPage(0);
//        searchCTO.setSize(100);
//        Order e = new Order();
//        e.setDirection(Sort.Direction.DESC);
//        e.setProperty("event");
//        searchCTO.getOrders().add(e);
//        Page<AuditModel> search = elasticService.search(searchCTO);
//        Assert.assertEquals(search.getTotalElements(), 6);
//    }
//
//    @Test
//    public void testSearchEvent() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        searchCTO.setEvent("event-1");
//        ResponseEntity<HashMap> response = send(searchCTO);
//        LOGGER.info("resp is {}", response.getBody());
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        Assert.assertEquals(((List) response.getBody().get("content")).size(), 1);
//        Assert.assertEquals(((Map) ((List) response.getBody().get("content")).get(0)).get("event"), "event-1");
//    }
//
//    @Test
//    public void testSearchContext() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        searchCTO.setContext("context -1");
//        ResponseEntity<HashMap> response = send(searchCTO);
//        LOGGER.info("resp is {}", response.getBody());
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        List content = (List) response.getBody().get("content");
//        Assert.assertEquals(content.size(), 2);
//        for (Object o : content) {
//            Assert.assertEquals(((Map) o).get("context"), "context -1");
//        }
//    }
//
//    @Test
//    public void testSearchActor() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        searchCTO.setActor("actor-1");
//        ResponseEntity<HashMap> response = send(searchCTO);
//        LOGGER.info("resp is {}", response.getBody());
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        List content = (List) response.getBody().get("content");
//        Assert.assertEquals(content.size(), 3);
//        for (Object o : content) {
//            Assert.assertEquals(((Map) o).get("actor"), "actor-1");
//        }
//    }
//
//    @Test
//    public void testSearchStatus() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        searchCTO.setStatus("status-1");
//        ResponseEntity<HashMap> response = send(searchCTO);
//        LOGGER.info("resp is {}", response.getBody());
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        List content = (List) response.getBody().get("content");
//        Assert.assertEquals(content.size(), 4);
//        for (Object o : content) {
//            Assert.assertEquals(((Map) o).get("status"), "status-1");
//        }
//    }
//
//    @Test
//    public void testSearchRemoteAddress() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        searchCTO.setRemoteAddress("r-m-1");
//        ResponseEntity<HashMap> response = send(searchCTO);
//        LOGGER.info("resp is {}", response.getBody());
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        List content = (List) response.getBody().get("content");
//        Assert.assertEquals(content.size(), 5);
//        for (Object o : content) {
//            Assert.assertEquals(((Map) o).get("remoteAddress"), "r-m-1");
//        }
//    }
//
//    @Test
//    public void testSearchExtraInfo() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        searchCTO.setExtraInfo("extraInfo-1");
//        ResponseEntity<HashMap> response = send(searchCTO);
//        LOGGER.info("resp is {}", response.getBody());
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
//        List content = (List) response.getBody().get("content");
//        Assert.assertEquals(content.size(), 6);
//        for (Object o : content) {
//            Assert.assertEquals(((Map) o).get("extraInfo"), "extraInfo-1");
//        }
//    }
//
//    @Test
//    public void testSearchNoStartDate() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        ResponseEntity<RestErrorResponse> response = send("", "2017-01-01-00-00-00", searchCTO, RestErrorResponse.class);
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(response.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(response.getBody().getMessages().get(0), Messages.START_NULL);
//
//    }
//
//    @Test
//    public void testSearchNoEndDate() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        ResponseEntity<RestErrorResponse> response = send("2017-01-01-00-00-00", "", searchCTO, RestErrorResponse.class);
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(response.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(response.getBody().getMessages().get(0), Messages.END_NULL);
//
//    }
//
//    @Test
//    public void testSearchStartBiggerEndDate() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        ResponseEntity<RestErrorResponse> response = send("2017-01-01-00-00-00", "2016-01-01-00-00-00", searchCTO, RestErrorResponse.class);
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(response.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(response.getBody().getMessages().get(0), Messages.START_BIGGER);
//
//    }
//
//    @Test
//    public void testSearchMaxUI() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        searchCTO.setPage(5000000);
//        searchCTO.setSize(1);
//        ResponseEntity<RestErrorResponse> response = send("2017-01-01-00-00-00", "2019-01-01-00-00-00", searchCTO, RestErrorResponse.class);
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(response.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(response.getBody().getMessages().get(0), String.format(Messages.MAX_UI, 10000, 5000001));
//
//    }
//
//    @Test
//    public void testSearchEqualStartAndENd() throws Exception {
//        SearchCTO searchCTO = new SearchCTO();
//        ResponseEntity<RestErrorResponse> response = send("2017-01-01-00-00-00", "2017-01-01-00-00-00", searchCTO, RestErrorResponse.class);
//        Assert.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
//        Assert.assertEquals(response.getBody().getKey(), Constants.VALIDATION_ERROR);
//        Assert.assertEquals(response.getBody().getMessages().get(0), Messages.START_BIGGER);
//
//    }
//
//    private <T> ResponseEntity<T> send(String start, String end, SearchCTO searchCTO, Class<T> tClass) throws ParseException {
//        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/api/search");
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
//        if (start != null && !"".equals(start))
//            searchCTO.setStart(simpleDateFormat.parse(start));
//        if (end != null && !"".equals(end))
//            searchCTO.setEnd(simpleDateFormat.parse(end));
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<?> entity = new HttpEntity<>(searchCTO, headers);
//        return testRestTemplate.exchange(
//                builder.toUriString(),
//                HttpMethod.POST,
//                entity,
//                tClass);
//
//    }
//
//    private ResponseEntity<HashMap> send(SearchCTO searchCTO) throws ParseException {
//        return send("2017-01-01-00-00-00", "2018-01-01-00-00-00", searchCTO, HashMap.class);
//    }
//}
