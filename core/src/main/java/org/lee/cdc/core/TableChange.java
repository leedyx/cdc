package org.lee.cdc.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableChange {


    private String type;

    private String id;

    private TableInfo table;

}
