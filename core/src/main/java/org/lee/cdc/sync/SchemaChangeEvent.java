package org.lee.cdc.sync;


import lombok.Data;

/**
 * 将数据库变更事件封装为一个对象，用于后续处理
 */

@Data
public class SchemaChangeEvent {

    private String ddl ;

    private String databaseName;





}
