package org.lee.cdc.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SQLS {

    public static String generateInsertSQL(String dbName, String tableName, Struct data) {
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();

        data.schema().fields().forEach(field -> {
            if (fields.length() > 0) {
                fields.append(", ");
                values.append(", ");
            }
            String fieldName = field.name();
            Object value = data.get(fieldName);
            fields.append("`").append(fieldName).append("`");
            values.append(escapeValue(value, field.schema()));
        });

        return String.format("INSERT INTO `%s`.`%s` (%s) VALUES (%s)",
               dbName, tableName, fields, values);
    }

    public static String generateUpdateSQL(String dbName, String tableName,
                                          Struct before, Struct after) {
        StringBuilder setClause = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();

        // 构建SET子句
        after.schema().fields().forEach(field -> {
            String fieldName = field.name();
            Object value = after.get(fieldName);
            if (setClause.length() > 0) setClause.append(", ");
            setClause.append("`").append(fieldName).append("` = ")
                    .append(escapeValue(value, field.schema()));
        });

        // 构建WHERE子句（使用主键）
        Map<String, Object> primaryKeys = extractPrimaryKey(before);
        primaryKeys.forEach((k, v) -> {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append("`").append(k).append("` = ")
                      .append(escapeValue(v, before.schema().field(k).schema()));
        });

        return String.format("UPDATE `%s`.`%s` SET %s WHERE %s",
               dbName, tableName, setClause, whereClause);
    }

    public static String generateDeleteSQL(String dbName, String tableName, Struct data) {
        StringBuilder whereClause = new StringBuilder();
        Map<String, Object> primaryKeys = extractPrimaryKey(data);

        primaryKeys.forEach((k, v) -> {
            Schema schema = data.schema().field(k).schema();
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append("`").append(k).append("` = ")
                      .append(escapeValue(v, schema));
        });

        return String.format("DELETE FROM `%s`.`%s` WHERE %s",
               dbName, tableName, whereClause);
    }

    // 修改为处理Struct类型
    public static Map<String, Object> extractPrimaryKey(Struct data) {
        Map<String, Object> pk = new LinkedHashMap<>();
        Struct source = data.getStruct("source");
        if (source != null) {
            Object pkFields = source.get("pk_fields");
            if (pkFields instanceof List) {
                ((List<?>) pkFields).forEach(field -> {
                    String fieldName = field.toString();
                    Object value = data.get(fieldName);
                    if (value != null) {
                        pk.put(fieldName, value);
                    }
                });
            }
        }
        return pk;
    }

    // 新增Struct值处理重载方法
    public static String escapeValue(Object value, Schema schema) {
        if (value == null) return "NULL";

        // 根据schema类型处理不同数据类型
        switch (schema.type()) {
            case STRING:
                return "'" + value.toString().replace("'", "''") + "'";
            case INT32:
            case INT64:
            case FLOAT32:
            case FLOAT64:
                return value.toString();
            case BOOLEAN:
                return (Boolean) value ? "1" : "0";
            default:
                return "'" + value.toString().replace("'", "''") + "'";
        }
    }

    // 保留原有JSON处理版本（如果需要兼容）
    public static String generateInsertSQL(String dbName, String tableName, JsonNode data) {

        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();

        data.fields().forEachRemaining(entry -> {
            if (fields.length() > 0) {
                fields.append(", ");
                values.append(", ");
            }
            fields.append("`").append(entry.getKey()).append("`");
            values.append(escapeValue(entry.getValue()));
        });

        return String.format("INSERT INTO `%s`.`%s` (%s) VALUES (%s)",
                dbName, tableName, fields, values);
    }

    public static String generateUpdateSQL(String dbName, String tableName,
                                            JsonNode before, JsonNode after) {
        StringBuilder setClause = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();

        // 构建SET子句
        after.fields().forEachRemaining(entry -> {
            if (setClause.length() > 0) setClause.append(", ");
            setClause.append("`").append(entry.getKey()).append("` = ")
                    .append(escapeValue(entry.getValue()));
        });

        // 构建WHERE子句（使用主键）
        Map<String, String> primaryKeys = extractPrimaryKey(before);
        primaryKeys.forEach((k, v) -> {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append("`").append(k).append("` = ").append(v);
        });

        return String.format("UPDATE `%s`.`%s` SET %s WHERE %s",
                dbName, tableName, setClause, whereClause);
    }

    public static String generateDeleteSQL(String dbName, String tableName, JsonNode data) {
        StringBuilder whereClause = new StringBuilder();
        Map<String, String> primaryKeys = extractPrimaryKey(data);

        primaryKeys.forEach((k, v) -> {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append("`").append(k).append("` = ").append(v);
        });

        return String.format("DELETE FROM `%s`.`%s` WHERE %s",
                dbName, tableName, whereClause);
    }

    // 辅助方法：提取主键
    public static Map<String, String> extractPrimaryKey(JsonNode data) {
        Map<String, String> pk = new LinkedHashMap<>();
        JsonNode source = data.get("source");
        if (source != null) {
            JsonNode pkFields = source.get("pk_fields");
            if (pkFields != null && pkFields.isArray()) {
                pkFields.forEach(field -> {
                    String fieldName = field.asText();
                    JsonNode value = data.get(fieldName);
                    if (value != null) {
                        pk.put(fieldName, escapeValue(value));
                    }
                });
            }
        }
        return pk;
    }

    // 辅助方法：转义特殊字符
    public static String escapeValue(JsonNode value) {

        if (value.isTextual()) {
            return "'" + value.asText().replace("'", "''") + "'";
        }
        return value.asText();
    }

}
