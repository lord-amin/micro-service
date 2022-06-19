package com.peykasa.audit.domain.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author kamran
 */
@Data
//@Api
public class SearchCTO {
    //    @ApiModelProperty(value= "yyyy-MM-dd-HH-mm-ss",example = "")
    @DateTimeFormat(pattern = "yyyy-MM-dd-HH-mm-ss")
    private Date start;
    //    @ApiModelProperty(value= "yyyy-MM-dd-HH-mm-ss",example = "")
    @DateTimeFormat(pattern = "yyyy-MM-dd-HH-mm-ss")
    private Date end;
    private String event;
    private String context;
    private String actor;
    private String status;
    private String remoteAddress;
    private String extraInfo;
    private Boolean primary;
    private int page;
    private int size;
    private List<Order> orders = new ArrayList<>();


}
