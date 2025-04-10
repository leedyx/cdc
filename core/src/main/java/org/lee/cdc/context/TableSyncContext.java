package org.lee.cdc.context;


import lombok.Data;
import org.lee.cdc.core.TableInfo;


/**
 * 存储上下文
 * 核心的是table的元数据
 * 这些元数据会指导后续数据的转化
 * 否则很多数据是没法转化的
 *
 */
@Data
public class TableSyncContext {

    private String id ;

    private TableInfo tableInfo;



}
