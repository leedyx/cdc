package org.lee.cdc;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import org.lee.cdc.task.SyncDataTask;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Properties;


@SpringBootApplication
public class SyncApplication {

    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SyncApplication.class);


    private static Properties loadConfig() {

        final Properties props = new Properties();
        props.setProperty("name", "syncData");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        props.setProperty("offset.storage.file.filename", "D:\\Data\\storage\\offsets.dat");
        props.setProperty("offset.flush.interval.ms", "1000");
        /* begin connector properties */
        props.setProperty("database.hostname", "192.168.5.4");
        //props.setProperty("database.serverTimezone","UTC" );
        props.setProperty("database.port", "33060");
        props.setProperty("database.user", "cdc");
        props.setProperty("database.password", "leeqian");
        props.setProperty("database.server.id", "223344");
        props.setProperty("topic.prefix", "sync-data-connector");
        props.setProperty("schema.history.internal", "io.debezium.storage.file.history.FileSchemaHistory");
        props.setProperty("schema.history.internal.file.filename", "D:\\Data\\storage\\schemahistory.dat");

        return props;

    }


    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(SyncApplication.class);

        ApplicationContext context = application.run(args);

        SyncDataTask syncDataTask = SyncDataTask.builder().build();


        Properties props = loadConfig();
        try (DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create(Json.class)
                .using(props)
                .notifying(syncDataTask
                ).build()
        ) {
            // Run the engine asynchronously ...
//            ExecutorService executor = Executors.newSingleThreadExecutor();
//            executor.execute(engine);


            engine.run();


            Thread.currentThread().join();
            // Do something else or wait for a signal or an event
        }
        catch (Exception e){

        }
    }
}
