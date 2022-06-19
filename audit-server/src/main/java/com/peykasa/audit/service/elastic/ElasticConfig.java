package com.peykasa.audit.service.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.elasticsearch.indices.PutIndexTemplateRequest;
import co.elastic.clients.elasticsearch.indices.put_index_template.IndexTemplateMapping;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.peykasa.audit.domain.model.AuditModel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author pouya rezaei
 */
@Configuration
@ConfigurationProperties("app.audit.elastic")
@Getter
@Setter
public class ElasticConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticConfig.class);
    public static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss";
    private String elasticPartitionType = "DAY";
    private String indexPrefix = "audit";
    private String username = "elastic";
    private String password = "admin123";
    private String templateName = "audit-template";
    private String clusterName = "audit-cluster";
    private boolean searchForAllIndex = false;
    private Integer indexMaxResultWindowSize = 10000;
    private boolean inited = false;
    private final List<ElasticNode> clusters = new ArrayList<>();


    public ElasticPartitionType getElasticPartitionType() {
        return ElasticPartitionType.valueOf(elasticPartitionType);
    }


    @SneakyThrows
    @Bean
    public RestClient restClient() {
        List<HttpHost> nodes = clusters.stream()
                .map(elasticNode -> new HttpHost(elasticNode.ip, elasticNode.port, "http"))
                .collect(Collectors.toList());
        HttpHost[] httpHosts = new HttpHost[nodes.size()];
        nodes.toArray(httpHosts);
        CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts)
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(5000)
                        .setConnectionRequestTimeout(5000)
                        .setSocketTimeout(60000));

        return restClientBuilder.build();
    }

    @Bean
    @Autowired
    public ElasticsearchClient elasticsearchClient(RestClient restClient) throws IOException {
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient elasticsearchClient = new ElasticsearchClient(transport);
        HealthResponse health = elasticsearchClient.cluster().health();
        LOGGER.info("elasticsearchClient.cluster.clusterName {}", health.clusterName());
        LOGGER.info("elasticsearchClient.cluster.status {}", health.status().jsonValue());
        LOGGER.info("elasticsearchClient.cluster.numberOfNodes {}", health.numberOfNodes());
        LOGGER.info("elasticsearchClient.cluster.activeShards {}", health.activeShards());
        elasticsearchClient.indices().putIndexTemplate(getAuditMapping());
        return elasticsearchClient;
    }

    public PutIndexTemplateRequest getAuditMapping() {
        Map<String, Property> propertyMap = new HashMap<>();
        propertyMap.put(AuditModel.TIME_FIELD, new Property.Builder().date(builder -> builder.format("epoch_millis")).build());
        propertyMap.put(AuditModel.TO_STATE_FIELD, new Property.Builder().object(builder -> builder).build());
        propertyMap.put(AuditModel.FROM_STATE_FIELD, new Property.Builder().object(builder -> builder).build());
        propertyMap.put(AuditModel.CONTEXT_FIELD, new Property.Builder().keyword(builder -> builder).build());
        propertyMap.put(AuditModel.ACTOR_FIELD, new Property.Builder().keyword(builder -> builder).build());
        propertyMap.put(AuditModel.EVENT_FIELD, new Property.Builder().keyword(builder -> builder).build());
        propertyMap.put(AuditModel.STATUS_FIELD, new Property.Builder().keyword(builder -> builder).build());
        propertyMap.put(AuditModel.REMOTE_ADDRESS_FIELD, new Property.Builder().ip(builder -> builder).build());
        propertyMap.put(AuditModel.CLIENT_FIELD, new Property.Builder().keyword(builder -> builder).build());
        propertyMap.put(AuditModel.EXTRA_INFO_FIELD, new Property.Builder().keyword(builder -> builder.ignoreAbove(1).docValues(false)).build());
        TypeMapping typeMapping = new TypeMapping.Builder()
                .properties(propertyMap)
                .build();
        IndexTemplateMapping indexTemplateMapping = new IndexTemplateMapping.Builder()
                .mappings(typeMapping)
                .settings(builder -> builder.maxResultWindow(indexMaxResultWindowSize))
                .build();
        PutIndexTemplateRequest putMappingRequest = new PutIndexTemplateRequest.Builder()
                .indexPatterns(getAllIndexFor(null, null))
                .name(templateName)
                .template(indexTemplateMapping)
                .build();
        return putMappingRequest;
    }

    public int getMaxResultWindow() {
        return indexMaxResultWindowSize;
    }

    public String partitionRelatedTo(ElasticConfig.ElasticPartitionType traceElasticPartitionType, Date time) {
        Calendar from = Calendar.getInstance();
        from.setTime(time);
        from.set(Calendar.MILLISECOND, 0);
        from.set(Calendar.SECOND, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.HOUR, 0);
        Calendar to = Calendar.getInstance();
        if (traceElasticPartitionType == ElasticConfig.ElasticPartitionType.DAY) {
            to.setTime(from.getTime());
            to.add(Calendar.DAY_OF_YEAR, 1);
        } else if (traceElasticPartitionType == ElasticConfig.ElasticPartitionType.WEEK) {
            from.set(Calendar.DAY_OF_WEEK, 1);
            to.setTime(from.getTime());
            to.add(Calendar.WEEK_OF_YEAR, 1);
        } else if (traceElasticPartitionType == ElasticConfig.ElasticPartitionType.MONTH) {
            from.set(Calendar.DAY_OF_MONTH, 1);
            to.setTime(from.getTime());
            to.add(Calendar.MONTH, 1);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return indexPrefix + "-" + sdf.format(from.getTime()) + "_" + sdf.format(to.getTime());
    }

    public List<String> getAllIndexFor(Long from, Long to) {
        if (searchForAllIndex || from == null || to == null)
            return Collections.singletonList(indexPrefix + "*");
        Date start = new Date(from);
        Date end = new Date(to);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        Set<String> dayIndices = new TreeSet<>();
        Set<String> weekIndices = new TreeSet<>();
        Set<String> monthIndices = new TreeSet<>();
        while (calendar.getTime().before(end)) {
            dayIndices.add(partitionRelatedTo(ElasticConfig.ElasticPartitionType.DAY, calendar.getTime()));
            weekIndices.add(partitionRelatedTo(ElasticConfig.ElasticPartitionType.WEEK, calendar.getTime()));
            monthIndices.add(partitionRelatedTo(ElasticConfig.ElasticPartitionType.MONTH, calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        dayIndices.add(partitionRelatedTo(ElasticConfig.ElasticPartitionType.DAY, calendar.getTime()));
        weekIndices.add(partitionRelatedTo(ElasticConfig.ElasticPartitionType.WEEK, calendar.getTime()));
        monthIndices.add(partitionRelatedTo(ElasticConfig.ElasticPartitionType.MONTH, calendar.getTime()));

        LOGGER.debug("The day indices is {}", dayIndices);
        LOGGER.debug("The week indices is {}", weekIndices);
        LOGGER.debug("The month indices is {}", monthIndices);

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(dayIndices);
        arrayList.addAll(weekIndices);
        arrayList.addAll(monthIndices);
        return arrayList;

    }

    public enum ElasticPartitionType {
        DAY, WEEK, MONTH
    }


}
