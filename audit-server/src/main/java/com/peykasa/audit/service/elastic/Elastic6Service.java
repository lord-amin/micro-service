//package com.peykasa.audit.service.elastic;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.peykasa.audit.domain.model.AuditModel;
//import com.peykasa.audit.domain.model.Order;
//import com.peykasa.audit.domain.model.SearchCTO;
//import com.peykasa.audit.service.AuditService;
//import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
//import org.elasticsearch.action.ActionFuture;
//import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
//import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateResponse;
//import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
//import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
//import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
//import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
//import org.elasticsearch.action.search.SearchType;
//import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
//import org.elasticsearch.common.collect.ImmutableOpenMap;
//import org.elasticsearch.common.compress.CompressedXContent;
//import org.elasticsearch.common.xcontent.XContentType;
//import org.elasticsearch.index.query.*;
//import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
//import org.elasticsearch.search.aggregations.bucket.terms.Terms;
//import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
//import org.elasticsearch.search.aggregations.support.ValueType;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.elasticsearch.core.DefaultResultMapper;
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
//import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
//import org.springframework.data.elasticsearch.core.query.IndexQuery;
//import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
//import org.springframework.util.StringUtils;
//
//import java.io.IOException;
//import java.net.URL;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
///**
// * @author Yaser(amin) Sadeghi
// */
//public class Elastic6Service implements AuditService {
//    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticService.class);
//    private static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss";
//    private final MyElasticsearchTemplate elasticsearchTemplate;
//    private final ElasticPartitionType elasticPartitionType;
//    private final String elasticIndexPrefix;
//    private ObjectMapper mapper = new ObjectMapper();
//
//    public enum ElasticPartitionType {
//        DAY, WEEK, MONTH
//    }
//
//
//    public ElasticService(MyElasticsearchTemplate elasticsearchTemplate, ElasticPartitionType elasticPartitionType, String elasticTemplateName, String elasticIndexPrefix, String mappingFileName) throws IOException {
//        this.elasticPartitionType = elasticPartitionType;
//        this.elasticIndexPrefix = elasticIndexPrefix;
//        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
//        this.elasticsearchTemplate = elasticsearchTemplate;
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        String indexNameWildCard = elasticIndexPrefix + "*";
//        LOGGER.info("Configuring elastic mapping");
//        LOGGER.info("Finding template mapping '{}'", elasticTemplateName);
//        IndexTemplateMetaData template = getTemplate(elasticsearchTemplate, elasticTemplateName);
//        String mappingString = mapper.writeValueAsString(getStringMppingMap(mapper, mappingFileName).get("mappings").get("json"));
//        String mappingNotFound = "Mapping not found Put mapping '{}'";
//        if (template == null) {
//            boolean b = putTemplate(elasticsearchTemplate, mapper,
//                    mappingFileName,
//                    elasticTemplateName,
//                    indexNameWildCard);
//
//            LOGGER.info(mappingNotFound, b);
//        } else {
//            ImmutableOpenMap<String, CompressedXContent> mappings = template.getMappings();
//            if (mappings.size() == 0) {
//                boolean deleteTemplate = deleteTemplate(elasticsearchTemplate, elasticTemplateName);
//                LOGGER.info("delete template {}={}", elasticTemplateName, deleteTemplate);
//                boolean b = putTemplate(elasticsearchTemplate, mapper,
//                        mappingFileName,
//                        elasticTemplateName,
//                        indexNameWildCard);
//                LOGGER.info(mappingNotFound, b);
//            } else {
//                CompressedXContent json = mappings.get("json");
//                LOGGER.info("The mapping string is {}", json);
//                if (json == null || !mappingString.equals(json.toString())) {
//                    boolean deleteTemplate = deleteTemplate(elasticsearchTemplate, elasticTemplateName);
//                    LOGGER.info("delete template {}={}", elasticTemplateName, deleteTemplate);
//                    boolean b = putTemplate(elasticsearchTemplate, mapper,
//                            elasticTemplateName,
//                            mappingFileName,
//                            indexNameWildCard);
//                    LOGGER.info(mappingNotFound, b);
//                }
//            }
//            LOGGER.info("Mapping found {}", template.mappings());
//        }
//    }
//
//    @Override
//    public Page<AuditModel> search(SearchCTO cto) {
//        NativeSearchQuery searchQuery = buildQuery(cto);
//        searchQuery.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
//        searchQuery.addTypes("json");
//        String[] indexForSearch = getIndexForSearch(cto.getStart(), cto.getEnd());
//        searchQuery.addIndices(indexForSearch);
//        LOGGER.info("Searching in indices {}", indexForSearch);
//        return elasticsearchTemplate.queryForPage(searchQuery, AuditModel.class, new DefaultResultMapper());
//    }
//
//    @Override
//    public List<String> distinct(String fieldName) {
//        TermsAggregationBuilder unique = unique(fieldName);
//        NativeSearchQuery searchQuery = new NativeSearchQuery(null);
//        searchQuery.setPageable(AggrUnPaged.INSTANCE);
//        searchQuery.addAggregation(unique);
//        searchQuery.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
//        searchQuery.addTypes("json");
//        String[] indexForSearch = getIndexForSearch(null, null);
//        searchQuery.addIndices(indexForSearch);
//        LOGGER.info("Searching in indices {}", indexForSearch);
//        AggregatedPage<String> strings = elasticsearchTemplate.queryForPage(searchQuery, String.class);
//        List<String> list = new ArrayList<>();
//        if (strings.getAggregations() != null) {
//            StringTerms terms = (strings.getAggregations().get(fieldName + "_Agg"));
//            if (terms != null) {
//                List<StringTerms.Bucket> buckets = terms.getBuckets();
//                if (buckets != null && buckets.size() > 0) {
//                    for (Terms.Bucket bucket : buckets) {
//                        list.add(bucket.getKeyAsString());
//                    }
//                }
//            }
//        }
//        LOGGER.debug(list.toString());
//        return list;
//    }
//
//    @Override
//    public List<String> distinctBy(String parent, String fieldName, String parentFilter) {
//        TermsAggregationBuilder unique = unique(parent);
//        unique.subAggregation(unique(fieldName));
//        NativeSearchQuery searchQuery = new NativeSearchQuery(null);
//        searchQuery.setPageable(AggrUnPaged.INSTANCE);
//        searchQuery.addAggregation(unique);
//        searchQuery.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
//        searchQuery.addTypes("json");
//        String[] indexForSearch = getIndexForSearch(null, null);
//        searchQuery.addIndices(indexForSearch);
//        LOGGER.info("Searching in indices {}", indexForSearch);
//        AggregatedPage<String> strings = elasticsearchTemplate.queryForPage(searchQuery, String.class);
//        List<String> list = new ArrayList<>();
//        if (strings.getAggregations() != null) {
//            StringTerms terms = (strings.getAggregations().get(parent + "_Agg"));
//            if (terms != null) {
//                List<StringTerms.Bucket> buckets = terms.getBuckets();
//                if (buckets != null && buckets.size() > 0) {
//                    for (Terms.Bucket bucket : buckets) {
//                        if (!bucket.getKeyAsString().equals(parentFilter))
//                            continue;
//                        StringTerms stringTerms = bucket.getAggregations().get(fieldName + "_Agg");
//                        if (stringTerms == null)
//                            continue;
//                        List<StringTerms.Bucket> eventBuckets = stringTerms.getBuckets();
//                        for (StringTerms.Bucket eventBucket : eventBuckets) {
//                            list.add(eventBucket.getKeyAsString());
//
//                        }
//                    }
//                }
//            }
//        }
//        LOGGER.debug(list.toString());
//        return list;
//    }
//
//    @Override
//    public int getMaxUIResult() {
//        return elasticsearchTemplate.getMaxResultWindow(elasticIndexPrefix);
//    }
//
//    private String[] getIndexForSearch(Date start, Date end) {
//        if (start == null || end == null)
//            return new String[]{elasticIndexPrefix + "*"};
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(start);
//        Set<String> dayIndices = new TreeSet<>();
//        Set<String> weekIndices = new TreeSet<>();
//        Set<String> monthIndices = new TreeSet<>();
//        while (calendar.getTime().before(end)) {
//            dayIndices.add(partition(ElasticPartitionType.DAY, elasticIndexPrefix, calendar.getTime()));
//            weekIndices.add(partition(ElasticPartitionType.WEEK, elasticIndexPrefix, calendar.getTime()));
//            monthIndices.add(partition(ElasticPartitionType.MONTH, elasticIndexPrefix, calendar.getTime()));
//            calendar.add(Calendar.DAY_OF_MONTH, 1);
//        }
//        dayIndices.add(partition(ElasticPartitionType.DAY, elasticIndexPrefix, calendar.getTime()));
//        weekIndices.add(partition(ElasticPartitionType.WEEK, elasticIndexPrefix, calendar.getTime()));
//        monthIndices.add(partition(ElasticPartitionType.MONTH, elasticIndexPrefix, calendar.getTime()));
//
//        LOGGER.info("The day indices is {}", dayIndices);
//        LOGGER.info("The week indices is {}", weekIndices);
//        LOGGER.info("The month indices is {}", monthIndices);
//
//        ArrayList<String> arrayList = new ArrayList<>();
//        arrayList.addAll(dayIndices);
//        arrayList.addAll(weekIndices);
//        arrayList.addAll(monthIndices);
//        return arrayList.toArray(new String[arrayList.size()]);
//
//    }
//
//    private String getKeyword(String field) {
//        return field + ".keyword";
//    }
//
//    private String contains(String value) {
////        String s = "*" + QueryParserUtil.escape(value) + "*";
//        String s = QueryParserUtil.escape(value);
//        s = escapeSpace(s);
//        return s;
//    }
//
//    public static String escapeSpace(String s) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < s.length(); ++i) {
//            char c = s.charAt(i);
//            if (' ' == c) {
//                sb.append('\\');
//            }
//            sb.append(c);
//        }
//        return sb.toString();
//    }
//
//    private NativeSearchQuery buildQuery(SearchCTO cto) {
//        LOGGER.info("The cto is {}", cto);
//        NativeSearchQuery searchQuery;
//        RangeQueryBuilder searchTimeFilter = null;
//        QueryBuilder matchQuery;
//
//        List<QueryBuilder> expressions = new ArrayList<>();
//        if (!StringUtils.isEmpty(cto.getActor())) {
//            expressions.add(like(getKeyword("actor"), contains(cto.getActor())));
//        }
//        if (!StringUtils.isEmpty(cto.getStatus())) {
//            expressions.add(like(getKeyword("status"), contains(cto.getStatus())));
//        }
//        if (!StringUtils.isEmpty(cto.getContext())) {
//            expressions.add(like(getKeyword("context"), contains(cto.getContext())));
//        }
//        if (!StringUtils.isEmpty(cto.getEvent())) {
//            expressions.add(like(getKeyword("event"), contains(cto.getEvent())));
//        }
//        if (!StringUtils.isEmpty(cto.getRemoteAddress())) {
//            expressions.add(like(getKeyword("remoteAddress"), contains(cto.getRemoteAddress())));
//        }
//        if (!StringUtils.isEmpty(cto.getExtraInfo())) {
//            expressions.add(like(getKeyword("extraInfo"), contains(cto.getExtraInfo())));
//        }
//
//        if (cto.getPrimary() != null) {
//            expressions.add(QueryBuilders.termQuery("primary", cto.getPrimary()));
//        }
//
//        if (!expressions.isEmpty()) {
//            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//            expressions.forEach(boolQueryBuilder::must);
//            matchQuery = boolQueryBuilder;
//        } else {
//            matchQuery = QueryBuilders.matchAllQuery();
//        }
//
////        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
//        if ((cto.getStart() != null || cto.getEnd() != null)) {
//            searchTimeFilter = QueryBuilders.rangeQuery("time").format("epoch_millis");
//            if (cto.getStart() != null) {
//                LOGGER.info("Start={}", new SimpleDateFormat(DATE_FORMAT).format(cto.getStart()));
//                searchTimeFilter.gt(cto.getStart().getTime());
//            }
//            if (cto.getEnd() != null) {
//                LOGGER.info("End={}", new SimpleDateFormat(DATE_FORMAT).format(cto.getEnd()));
//                searchTimeFilter.lte(cto.getEnd().getTime());
//            }
//        }
//        searchQuery = new NativeSearchQuery(matchQuery, searchTimeFilter);
//        Order defaultOrder = new Order();
//        defaultOrder.setProperty("time");
//        defaultOrder.setDirection(Sort.Direction.DESC);
//        if (cto.getOrders().isEmpty() || !cto.getOrders().contains(defaultOrder)) {
//            cto.getOrders().add(defaultOrder);
//        }
//        if (!cto.getOrders().isEmpty()) {
//            for (Order order : cto.getOrders()) {
//                if (useKeyword(order.getProperty()))
//                    searchQuery.addSort(Sort.by(new Sort.Order(Sort.Direction.fromString(order.getDirection().name()), getKeyword(order.getProperty()))));
//                else
//                    searchQuery.addSort(Sort.by(new Sort.Order(Sort.Direction.fromString(order.getDirection().name()), order.getProperty())));
//            }
//        }
//        PageRequest pageRequest = PageRequest.of(cto.getPage(), cto.getSize());
//        searchQuery.setPageable(pageRequest);
//        return searchQuery;
//    }
//
//    private boolean useKeyword(String name) {
//        try {
//            java.lang.reflect.Field field = AuditModel.class.getDeclaredField(name);
//            if (field == null) {
//                return false;
//            }
//            if (field.getType().getTypeName().endsWith("String")) {
//                return true;
//            }
//
//        } catch (Exception ex) {
//            LOGGER.error(ex.getMessage(), ex);
//        }
//        return false;
//
//    }
//
//    @Override
//    public void log(AuditModel body) throws JsonProcessingException {
//        String json = mapper.writeValueAsString(body);
//        LOGGER.info("Saving {}", json);
//        IndexQuery indexQuery = getIndexQuery(body.getTime(), json);
//        String index = elasticsearchTemplate.index(indexQuery);
//        LOGGER.info("Saving document {} with id {}", json, index);
//    }
//
//    private IndexQuery getIndexQuery(Date date, String json) {
//        IndexQuery indexQuery = new IndexQuery();
//        indexQuery.setIndexName(partition(elasticPartitionType, elasticIndexPrefix, date));
//        indexQuery.setType("json");
//        indexQuery.setSource(json);
//        return indexQuery;
//    }
//
//    private static String partition(ElasticPartitionType traceElasticPartitionType, String prefix, Date time) {
//        Calendar from = Calendar.getInstance();
//        from.setTime(time);
//        from.set(Calendar.MILLISECOND, 0);
//        from.set(Calendar.SECOND, 0);
//        from.set(Calendar.MINUTE, 0);
//        from.set(Calendar.HOUR, 0);
//        Calendar to = Calendar.getInstance();
//        if (traceElasticPartitionType == ElasticPartitionType.DAY) {
//            to.setTime(from.getTime());
//            to.add(Calendar.DAY_OF_YEAR, 1);
//        } else if (traceElasticPartitionType == ElasticPartitionType.WEEK) {
//            from.set(Calendar.DAY_OF_WEEK, 1);
//            to.setTime(from.getTime());
//            to.add(Calendar.WEEK_OF_YEAR, 1);
//        } else if (traceElasticPartitionType == ElasticPartitionType.MONTH) {
//            from.set(Calendar.DAY_OF_MONTH, 1);
//            to.setTime(from.getTime());
//            to.add(Calendar.MONTH, 1);
//        }
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        return prefix + "-" + sdf.format(from.getTime()) + "_" + sdf.format(to.getTime());
//    }
//
//    private static IndexTemplateMetaData getTemplate(ElasticsearchTemplate elasticsearchTemplate, String name) {
//        GetIndexTemplatesRequest request = new GetIndexTemplatesRequest(name);
//        ActionFuture<GetIndexTemplatesResponse> templates = null;
//        try {
//            templates = elasticsearchTemplate.getClient().admin().indices().getTemplates(request);
//        } catch (Exception e) {
//            LOGGER.error("", e);
//            throw e;
//        }
//        try {
//            List<IndexTemplateMetaData> indexTemplates = templates.actionGet().getIndexTemplates();
//            if (indexTemplates.size() == 1)
//                return indexTemplates.get(0);
//            LOGGER.warn("The template '{}' is more", name);
//            return null;
//        } catch (Exception e) {
//            LOGGER.warn("template " + name + " not found", e);
//            return null;
//        }
//    }
//
//    private static Map<String, Map<String, Map<String, String>>> getStringMppingMap(ObjectMapper mapper, String fileName) throws IOException {
//        URL resource = ElasticService.class.getResource("/" + fileName);
//        return mapper.readValue(resource, mapper.getTypeFactory().constructMapType(Map.class, String.class, Map.class));
//    }
//
//    private static boolean putTemplate(ElasticsearchTemplate elasticsearchTemplate, ObjectMapper mapper, String fileName, String templateName, String indexNameWildCard) throws IOException {
//        URL resource = ElasticsearchTemplate.class.getResource("/" + fileName);
//        Map<String, Map<String, Map<String, String>>> map = mapper.readValue(resource, mapper.getTypeFactory().constructMapType(Map.class, String.class, Map.class));
//        Map<String, Map<String, String>> mappingsMap = map.get("mappings");
//        Map<String, Map<String, String>> settingsMap = map.get("settings");
//        String mappingName = "";
//        String mappings = "";
//        for (Map.Entry<String, Map<String, String>> entry : mappingsMap.entrySet()) {
//            mappingName = entry.getKey();
//            mappings = mapper.writeValueAsString(entry.getValue());
//        }
//        String settings = mapper.writeValueAsString(settingsMap);
//        LOGGER.info("Putting mapping {}", mappings);
//        LOGGER.info("Putting settings {}", settings);
//        PutIndexTemplateRequest request = new PutIndexTemplateRequest();
//        request.settings(settings, XContentType.JSON);
//        request.template(indexNameWildCard);
//        request.name(templateName);
//        request.mapping(mappingName, mappings, XContentType.JSON);
//        ActionFuture<PutIndexTemplateResponse> putIndexTemplateResponseActionFuture = elasticsearchTemplate.getClient().admin().indices().putTemplate(request);
//        PutIndexTemplateResponse putIndexTemplateResponse = putIndexTemplateResponseActionFuture.actionGet();
//        return putIndexTemplateResponse.isAcknowledged();
//    }
//
//    private static boolean deleteTemplate(ElasticsearchOperations elasticsearchTemplate, String name) {
//        DeleteIndexTemplateRequest request = new DeleteIndexTemplateRequest(name);
//        ActionFuture<DeleteIndexTemplateResponse> deleteIndexTemplateResponseActionFuture = elasticsearchTemplate.getClient().admin().indices().deleteTemplate(request);
//        try {
//            return deleteIndexTemplateResponseActionFuture.actionGet().isAcknowledged();
//        } catch (Exception e) {
//            LOGGER.warn("The template " + name + " not deleted");
//            return false;
//        }
//    }
//
//    private QueryStringQueryBuilder like(String field, String value) {
//        QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(value);
//        queryBuilder.field(field);
//        return queryBuilder;
//    }
//
//    private TermsAggregationBuilder unique(String fieldName) {
//        TermsAggregationBuilder aggregationBuilder = new TermsAggregationBuilder(fieldName + "_Agg", ValueType.STRING);
//        aggregationBuilder.field(fieldName + ".keyword");
//        aggregationBuilder.size(getMaxUIResult());
//        return aggregationBuilder;
//    }
//}
