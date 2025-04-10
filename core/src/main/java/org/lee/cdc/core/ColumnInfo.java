package org.lee.cdc.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ColumnInfo {

    private String name;

    private int position;

    private int jdbcType;

    private int nativeType;

    private String typeName;

    private String typeExpression;

    private String charsetName;

    private int length;

    private Integer scale;

    private boolean optional;

    private boolean autoIncremented;

    private boolean generated;

    private String defaultValueExpression;

    private boolean hasDefaultValue;

    private List<String> enumValues;

    private String comment;

}
