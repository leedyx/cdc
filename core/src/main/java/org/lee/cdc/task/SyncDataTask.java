package org.lee.cdc.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import lombok.Builder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.lee.cdc.context.TableSyncContext;
import org.lee.cdc.core.TableChange;
import org.lee.cdc.sync.SchemaChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 个人理解
 * 该类的核心方法，应该是单线程在执行，所以不需要考虑线程安全的问题
 */
@Builder
public class SyncDataTask implements DebeziumEngine.ChangeConsumer<ChangeEvent<String, String>> {


    private final static Logger LOGGER = LoggerFactory.getLogger(SyncDataTask.class);


    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ObjectReader TABLE_CHANGE_READER = MAPPER.readerForListOf(TableChange.class);
    /**
     * 数据库变更事件
     */


    @Builder.Default
    private final Map<String, TableSyncContext> tableSyncContextMap = new HashMap<>();

    /**
     * 消息条目统计
     */
    @Builder.Default
    private int count = 0;


    /**
     * 解析schemaChangeEvent
     *
     * @param value
     * @return
     * @throws Exception
     */
    private SchemaChangeEvent parseSchemaChangeEvent(String value) throws Exception {

        SchemaChangeEvent schemaChangeEvent = new SchemaChangeEvent();

        JsonNode valueNode = MAPPER.readTree(value);

        if (!valueNode.has("payload")) {
            LOGGER.info("no payload !");
            return null;
        }

        JsonNode payloadNode = valueNode.get("payload");

        if (!payloadNode.has("tableChanges")) {

            //JsonNode tableChangesNode = payloadNode.get("tableChanges");
            return null;
        }

        String databaseName = payloadNode.get("databaseName").asText();
        String ddl = payloadNode.get("ddl").asText();
        schemaChangeEvent.setDdl(ddl);
        schemaChangeEvent.setDatabaseName(databaseName);


        if (payloadNode.has("tableChanges")) {
            JsonNode tableChangesNode = payloadNode.get("tableChanges");
            List<TableChange> tableChangeList = TABLE_CHANGE_READER.readValue(tableChangesNode);
            schemaChangeEvent.setTableChangeList(tableChangeList);
        }

        return schemaChangeEvent;
    }


    @Override
    public void handleBatch(List<ChangeEvent<String, String>> records, DebeziumEngine.RecordCommitter<ChangeEvent<String, String>> committer) throws InterruptedException {

        if (CollectionUtils.isEmpty(records)) {
            return;
        }


        for (ChangeEvent<String, String> record : records) {

            try {

                String key = record.key();
                String value = record.value();

                if (LOGGER.isDebugEnabled()) {

                    LOGGER.debug(" -- key : {}", key);
                    LOGGER.debug(" -- value : {}", value);
                }

                JsonNode keyNode = MAPPER.readTree(key);

                JsonNode nameNode = keyNode.path("schema").path("name");

                String name = nameNode.asText();
                LOGGER.info(" -- name : {} ", name);

                if (StringUtils.endsWith(name, "SchemaChangeKey")) {

                    /**
                     * 解析schemaChangeEvent
                     */
                    SchemaChangeEvent schemaChangeEvent = parseSchemaChangeEvent(value);
                    LOGGER.info(" -- schemaChangeEvent : {} ", schemaChangeEvent);

                    List<TableChange> tableChangeList = schemaChangeEvent.getTableChangeList();

                    if (CollectionUtils.isNotEmpty(tableChangeList)){
                        for (TableChange tableChange : tableChangeList){
                            if(Objects.nonNull(tableChange.getTable())){
                                String id = tableChange.getId();
                                TableSyncContext tableSyncContext = TableSyncContext.builder()
                                        .id(id)
                                        .tableInfo(tableChange.getTable())
                                        .build();
                                TableSyncContext old = tableSyncContextMap.put(id,tableSyncContext);
                                if(Objects.nonNull(old)){
                                    LOGGER.info(" CHANGE -- old : {} -- new : {}",MAPPER.writeValueAsString(old),MAPPER.writeValueAsString(tableSyncContext));
                                }else {
                                    LOGGER.info(" ADD -- new : {}",MAPPER.writeValueAsString(tableSyncContext));
                                }
                            }
                        }
                    }

                } else {

                }

            } catch (Exception e) {
                LOGGER.error(" -- 消息处理异常", e);
            }finally {
                committer.markProcessed(record);
            }
        }
        committer.markBatchFinished();
    }
}
