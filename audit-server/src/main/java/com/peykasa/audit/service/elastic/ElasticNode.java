package com.peykasa.audit.service.elastic;

import lombok.Data;

@Data
public class ElasticNode {
    String ip;
    Integer port;
    Boolean master;
}
