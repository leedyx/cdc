package org.lee.cdc.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import lombok.Builder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.lee.cdc.context.TableSyncContext;
import org.lee.cdc.sync.SchemaChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 个人理解
 * 该类的核心方法，应该是单线程在执行，所以不需要考虑线程安全的问题
 */
@Builder
public class SyncDataTask implements DebeziumEngine.ChangeConsumer<ChangeEvent<String, String>> {


    private final static Logger LOGGER = LoggerFactory.getLogger(SyncDataTask.class);


    private static final ObjectMapper MAPPER = new ObjectMapper();


    @Builder.Default
    private final Map<String, TableSyncContext> tableSyncContextMap = new HashMap<>();

    /**
     * 消息条目统计
     */
    @Builder.Default
    private int count = 0;




    private SchemaChangeEvent parseSchemaChangeEvent(String value) {


        return null;

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

                if(StringUtils.endsWith(name,"SchemaChangeKey")){

                    /**
                     * 解析schemaChangeEvent
                     */


                }else {

                }

            } catch (Exception e) {
                LOGGER.error(" -- 消息处理异常", e);
            }


        }


    }
}
