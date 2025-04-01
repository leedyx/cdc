package org.lee.cdc.transformer.mysql;

import io.debezium.data.SchemaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.lee.cdc.message.DdlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DDLs {

    private static final Logger LOGGER = LoggerFactory.getLogger(DDLs.class);


    public static DdlMessage parseDDL(SourceRecord record) {

        DdlMessage res = new DdlMessage();

        Object key = record.key();
        Object value = record.value();

        if (Objects.nonNull(key)) {

            Struct keyStruct = (Struct) key;
            Schema keySchema = keyStruct.schema();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("key value detail : {}", SchemaUtil.asDetailedString(keyStruct));
                LOGGER.debug("key schema detail: {}", SchemaUtil.asDetailedString(keySchema));
            }
        }

        if (Objects.nonNull(value)) {
            Struct valueStruct = (Struct) value;
            Schema valueSchema = valueStruct.schema();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("valueSchema: {}", SchemaUtil.asDetailedString(valueSchema));
                LOGGER.debug("valueDetail: {}", SchemaUtil.asDetailedString(valueStruct));
            }


            Set<String> fieldNames = valueSchema.fields().stream().map(Field::name).collect(Collectors.toSet());
            //DdlMessage ddlMessage = new DdlMessage();
            if (fieldNames.contains("ddl")) {
                res.setDdl(valueStruct.getString("ddl"));
            }

            if (fieldNames.contains("databaseName")) {
                String databaseName = valueStruct.getString("databaseName");
                if (StringUtils.isNotBlank(databaseName)) {
                    res.setDatabase(databaseName);
                }
            }
        }

        return res;
    }


}
