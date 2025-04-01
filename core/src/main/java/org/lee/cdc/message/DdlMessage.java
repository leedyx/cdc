package org.lee.cdc.message;


import lombok.Data;

/**
 * 针对 mysql
 * 有些语句是 数据库实例级别的
 * 例如设置字符集，全局的排序规则
 *
 * 还有一些则是database级别的，就是针对特定的数据库的
 *
 *
 *
 *
 *
 */



@Data
public class DdlMessage {

    private String database;

    private String ddl ;


}
