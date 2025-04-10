package org.lee.cdc.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableInfo {


    private String defaultCharsetName;

    private List<String> primaryKeyColumnNames;

    private List<ColumnInfo> columns;

    private String comment;



}
