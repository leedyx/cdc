package org.lee.cdc.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.data.Json;
import io.debezium.data.SchemaUtil;
import io.debezium.engine.ChangeEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.lee.cdc.enums.Database;
import org.lee.cdc.message.DdlMessage;
import org.lee.cdc.transformer.mysql.DDLs;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class Cores {


    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Cores.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void parse(ChangeEvent<String,String> record){

        String value = record.value();

        if(StringUtils.isBlank(value)){
            return;
        }

        try {
            JsonNode event = mapper.readTree(value);

            LOGGER.info("event: {}", event);

            JsonNode opNode = event.get("payload").get("op");
            if (Objects.isNull(opNode)){
                return;
            }

            JsonNode payloadNode = event.get("payload");

            String operation = opNode.asText();


            // 解析事件元数据
            JsonNode source = event.get("payload").get("source");
            if (Objects.isNull(source)){
                return;
            }

            JsonNode dbNode = source.get("db");
            if (Objects.isNull(dbNode)){
                return;
            }
            String dbName = dbNode.asText();
            if (StringUtils.isBlank(dbName)){
                return;
            }

            JsonNode tableNode = source.get("table");

            if (Objects.isNull(tableNode)){
                return;
            }
            String tableName = tableNode.asText();
            if (StringUtils.isBlank(tableName)){
                return;
            }


            // 根据操作类型处理数据
            switch (operation) {
                case "c": // 插入操作
                case "r": // 快照读取（初始数据同步）
                    JsonNode after = payloadNode.get("after");
                    System.out.printf("[INSERT] 数据库：%s 表：%s 新数据：%s%n",
                            dbName, tableName, after.toString());

                    String res = SQLS.generateInsertSQL(dbName,tableName,after);
                    System.out.println(res);
                    break;
                case "u": // 更新操作
                    JsonNode beforeUpdate = payloadNode.get("before");
                    JsonNode afterUpdate = payloadNode.get("after");
                    System.out.printf("[UPDATE] 数据库：%s 表：%s%n旧值：%s%n新值：%s%n",
                            dbName, tableName, beforeUpdate.toString(), afterUpdate.toString());
                    break;
                case "d": // 删除操作
                    JsonNode beforeDelete = payloadNode.get("before");
                    System.out.printf("[DELETE] 数据库：%s 表：%s 删除数据：%s%n",
                            dbName, tableName, beforeDelete.toString());
                    break;
                default:
                    System.out.println("未知操作类型: " + operation);
            }
        } catch (Exception e) {
            LOGGER.error("解析事件失败: {}", e.getMessage(), e);
        }



    }








    public static void parse(SourceRecord record) {

        String topic = record.topic();
        LOGGER.info("topic: {}", topic);

        Schema keySchema = record.keySchema();
        String keyName = keySchema.name();

        LOGGER.info("keyName: {}", keyName);

        Database database = null;
        if(StringUtils.contains(keyName,"mysql")){
            database = Database.MySQL;
        }

        boolean dataChange = true;
        if (StringUtils.endsWith(keyName,"SchemaChangeKey")){
            dataChange = false;
        }


        if (database == Database.MySQL && !dataChange){
            DdlMessage res = DDLs.parseDDL(record);

            LOGGER.info("res: {}", res);
        }



//        Object key = record.key();
//
//        if (Objects.nonNull(key)){
//            if (key instanceof Struct){
//                Struct struct = (Struct) key;
//                LOGGER.info("key value detail : {}", SchemaUtil.asDetailedString(struct));
//            }
//
//            Schema schema = keySchema;
//            if (Objects.nonNull(schema)){
//                LOGGER.info("key schema detail: {}", SchemaUtil.asDetailedString(schema));
//            }
//        }
//
//
//        Schema valueSchema = record.valueSchema();
//        if (Objects.nonNull(valueSchema)){
//
//            String detail = SchemaUtil.asDetailedString(valueSchema);
//
//            LOGGER.info("valueSchema: {}", detail);
//
//            String valueDetail = SchemaUtil.asDetailedString((Struct) record.value());
//
//            LOGGER.info("valueDetail: {}", valueDetail);
//
//        }



//        if (Objects.nonNull(key)){
//            LOGGER.info("key: {}", key);
//            if (key instanceof Struct){
//                Struct struct = (Struct) key;
//                Schema schema = struct.schema();
//                struct.schema().fields().forEach(field -> {
//                    Object fieldValue = struct.get(field);
//                    LOGGER.info("field: {} value: {}", field.name(), fieldValue);
//                });
//            }
//        }
//
//        Object value = record.value();
//        if (Objects.nonNull(value)){
//            LOGGER.info("value: {}", value);
//
//            // 新增SQL生成逻辑
//            if (value instanceof Struct) {
//                try {
//                    Struct struct = (Struct) value;
//
//                    List<Field> fieldList = struct.schema().fields();
//                    Set<String> nameSet = fieldList.stream().map(Field::name).collect(Collectors.toSet());
//
//                    /**
//                     * 首先，需要根据一些特殊字段来判断
//                     * 数据库类型
//                     *
//                     * 消息类型
//                     */
//                    if (nameSet.contains("op")){
//                        LOGGER.info("op: {}", struct.get("op"));
//                    }else if (nameSet.contains("ddl")){
//
//                    }
//                    struct.get("op");
//                    // 解析操作类型
//                    String op = struct.getString("op");
//                    // 解析数据源信息
//                    Struct source = struct.getStruct("source");
//                    String dbName = source.getString("db");
//                    String tableName = source.getString("table");
//
//                    switch (op) {
//                        case "c":
//                        case "r":
//                            Struct after = struct.getStruct("after");
//                            String insertSQL = SQLS.generateInsertSQL(dbName, tableName, after);
//                            System.out.println("[INSERT SQL] " + insertSQL);
//                            break;
//                        case "u":
//                            Struct beforeUpdate = struct.getStruct("before");
//                            Struct afterUpdate = struct.getStruct("after");
//                            String updateSQL = SQLS.generateUpdateSQL(dbName, tableName, beforeUpdate, afterUpdate);
//                            System.out.println("[UPDATE SQL] " + updateSQL);
//                            break;
//                        case "d":
//                            Struct beforeDelete = struct.getStruct("before");
//                            String deleteSQL = SQLS.generateDeleteSQL(dbName, tableName, beforeDelete);
//                            System.out.println("[DELETE SQL] " + deleteSQL);
//                            break;
//                    }
//                } catch (Exception e) {
//                    LOGGER.error("生成SQL失败: {}", e.getMessage(), e);
//                }
//            }
//
//        }
//
//
//
//        //Schema keySchema = record.keySchema();
//
//        if (Objects.nonNull(keySchema)){
//            LOGGER.info("keySchema: {}", keySchema);
//        }
//
//        Schema valueSchema = record.valueSchema();
//        if (Objects.nonNull(valueSchema)){
//
//            String detail = SchemaUtil.asDetailedString(valueSchema);
//
//            LOGGER.info("valueSchema: {}", detail);
//
//            String valueDetail = SchemaUtil.asDetailedString((Struct) record.value());
//
//            LOGGER.info("valueDetail: {}", valueDetail);
//
//        }


    }

}
