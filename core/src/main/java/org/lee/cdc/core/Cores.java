package org.lee.cdc.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.data.Json;
import io.debezium.engine.ChangeEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Objects;


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



}
