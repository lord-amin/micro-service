package com.peykasa.audit.service.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.peykasa.audit.domain.model.AuditModel;
import com.peykasa.audit.domain.model.Order;
import com.peykasa.audit.domain.model.SearchCTO;
import com.peykasa.audit.exception.GlobalException;
import com.peykasa.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.peykasa.audit.service.elastic.ElasticConfig.DATE_FORMAT;

/**
 * @author Pouya Rezaei
 */
@RequiredArgsConstructor
@Service
public class ElasticService implements AuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticService.class);
    private final ElasticsearchClient elasticsearchClient;
    private final ElasticConfig elasticConfig;

    @Override
    public void log(AuditModel body) throws JsonProcessingException, GlobalException {
        IndexRequest<AuditModel> indexRequest = new IndexRequest.Builder<AuditModel>()
                .index(elasticConfig.partitionRelatedTo(elasticConfig.getElasticPartitionType(), body.getTime()))
                .document(body)
                .opType(OpType.Index)
                .build();
        try {
            IndexResponse index = elasticsearchClient.index(indexRequest);
            LOGGER.info("saved {}", index.id());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }


    }

    @Override
    public Page<AuditModel> search(SearchCTO searchCTO) {
        List<Query> queries = new ArrayList<>();
        RangeQuery.Builder range = new RangeQuery.Builder().field(AuditModel.TIME_FIELD).format("epoch_millis");
        Query.Builder finalQuery = new Query.Builder();

        if (!ObjectUtils.isEmpty(searchCTO.getActor()))
            queries.add(termQuery(AuditModel.ACTOR_FIELD, searchCTO.getActor()));

        if (!ObjectUtils.isEmpty(searchCTO.getContext()))
            queries.add(termQuery(AuditModel.CONTEXT_FIELD, searchCTO.getContext()));

        if (!ObjectUtils.isEmpty(searchCTO.getEvent()))
            queries.add(termQuery(AuditModel.EVENT_FIELD, searchCTO.getEvent()));

        if (!ObjectUtils.isEmpty(searchCTO.getPrimary()))
            queries.add(termQuery(AuditModel.PRIMARY_FIELD, searchCTO.getPrimary()));

        if (!ObjectUtils.isEmpty(searchCTO.getStatus()))
            queries.add(termQuery(AuditModel.STATUS_FIELD, searchCTO.getStatus()));

        if (!ObjectUtils.isEmpty(searchCTO.getRemoteAddress()))
            queries.add(termQuery(AuditModel.REMOTE_ADDRESS_FIELD, searchCTO.getRemoteAddress()));

        if (!ObjectUtils.isEmpty(searchCTO.getExtraInfo()))
            queries.add(termQuery(AuditModel.EXTRA_INFO_FIELD, searchCTO.getExtraInfo()));

        if ((searchCTO.getStart() != null || searchCTO.getEnd() != null)) {
            if (searchCTO.getStart() != null) {
                LOGGER.info("Start={}", new SimpleDateFormat(DATE_FORMAT).format(searchCTO.getStart()));
                range.gt(JsonData.of(searchCTO.getStart().getTime()));
            }
            if (searchCTO.getEnd() != null) {
                LOGGER.info("End={}", new SimpleDateFormat(DATE_FORMAT).format(searchCTO.getEnd()));
                range.lt(JsonData.of(searchCTO.getEnd().getTime()));
            }
            queries.add(range.build()._toQuery());
        }

        if (queries.isEmpty()) {
            finalQuery.matchAll(builder -> builder);
        } else {
            finalQuery.bool(BoolQuery.of(boolQueryBuilder -> boolQueryBuilder.must(queries)));
        }
        List<SortOptions> sorts = new ArrayList<>();
        if (!searchCTO.getOrders().isEmpty()) {
            for (Order order : searchCTO.getOrders()) {
                Sort.Direction direction = order.getDirection();
                if (useKeyword(order.getProperty())) {
                    SortOrder sortOrder = direction == Sort.Direction.ASC ? SortOrder.Asc : SortOrder.Desc;
                    SortOptions.Builder sortOptionsBuilders = new SortOptions.Builder();
                    sortOptionsBuilders.field(builder -> builder.field(order.getProperty()).order(sortOrder));
                    sorts.add(sortOptionsBuilders.build());
                }
            }
        }
        int from = searchCTO.getPage() * searchCTO.getSize();
        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                .index(elasticConfig.getAllIndexFor(null, null))
                .query(finalQuery.build())
                .sort(sorts)
                .from(from)
                .size(searchCTO.getSize());


        SearchRequest searchRequest = searchRequestBuilder.build();
        try {
            SearchResponse<AuditModel> search = elasticsearchClient.search(searchRequest, AuditModel.class);
            List<AuditModel> collect = search.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
            return new PageImpl<>(collect, Pageable.ofSize(searchCTO.getSize()).withPage(searchCTO.getPage()), search.hits().total().value());
        } catch (IOException e) {
            LOGGER.error("IOException search {} msg {}", searchCTO, e.getMessage());
            return Page.empty();
        }
    }

    @Override
    public List<String> distinct(String fieldName) {
        TermsAggregation termsAggregation = distinctTermAgg(fieldName);
        Aggregation unique = new Aggregation.Builder().terms(termsAggregation).build();
        SearchRequest searchRequest = new SearchRequest.Builder().index(elasticConfig.getAllIndexFor(null, null)).aggregations(fieldName, unique).build();
        try {
            SearchResponse<Object> searchResponse = elasticsearchClient.search(searchRequest, Object.class);
            List<StringTermsBucket> bucket = getBucket(searchResponse, fieldName);
            return bucket.stream().map(StringTermsBucket::key).collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("IOException field {} msg {}", fieldName, e.getMessage());
            return Collections.emptyList();
        } catch (NullPointerException ex1) {
            LOGGER.info("Not found field {} msg {}", fieldName, ex1.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> distinctBy(final String outer, final String inner, final String outerFilter) {
        TermsAggregation innerTermsAggregation = distinctTermAgg(inner);

        Aggregation unique = new Aggregation.Builder().filters(builder -> builder.filters(queryBuilder -> queryBuilder.keyed(Collections.singletonMap(outer, Query.of(builder13 -> builder13.term(builder12 -> builder12.field(
                outer).value(builder1 -> builder1.stringValue(outerFilter)))))))).aggregations(inner, new Aggregation.Builder().terms(innerTermsAggregation).build()).build();
        SearchRequest searchRequest = new SearchRequest.Builder().index(elasticConfig.getAllIndexFor(null, null)).aggregations(outer, unique).build();
        try {
            SearchResponse<Object> searchResponse = elasticsearchClient.search(searchRequest, Object.class);
            List<String> result = searchResponse.aggregations().get(outer).filters().buckets().keyed().get(outer).aggregations().get(inner).sterms().buckets().array().stream().map(StringTermsBucket::key).collect(Collectors.toList());
            return result;
        } catch (IOException e) {
            LOGGER.error("IOException field {} msg {}", inner, e.getMessage());
            return Collections.emptyList();
        } catch (NullPointerException ex1) {
            LOGGER.info("Not found field {} msg {}", inner, ex1.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public int getMaxUIResult() {
        return elasticConfig.getMaxResultWindow();
    }

    private TermsAggregation distinctTermAgg(String fieldName) {
        return new TermsAggregation.Builder().field(fieldName).build();
    }

    private Query termQuery(String field, Boolean value) {
        return Query.of(queryBuilder -> queryBuilder.term(TermQuery.of(termQueryBuilder -> termQueryBuilder.field(field).value(value))));
    }

    private Query termQuery(String field, String value) {
        return Query.of(queryBuilder -> queryBuilder.term(TermQuery.of(termQueryBuilder -> termQueryBuilder.field(field).value(value))));
    }

    private boolean useKeyword(String name) {
        List<String> fields = Arrays.stream(AuditModel.class.getDeclaredFields()).map(Field::getName).map(String::toLowerCase).collect(Collectors.toList());
        return fields.contains(name.toLowerCase());
    }

    private List<StringTermsBucket> getBucket(SearchResponse response, String aggName) throws NullPointerException {
        return ((Aggregate) response.aggregations().get(aggName)).sterms().buckets().array();
    }
}
