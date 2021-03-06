package org.apache.kafka.connect.binary;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * BinarySourceConnector implements the connector interface
 * to write on Kafka binary files
 *
 * @author Alex Piermatteo
 */
public class BinarySourceConnector extends SourceConnector {
    public static final String USE_DIRWATCHER = "use.java.dirwatcher";
    public static final String DIR_PATH = "tmp.path";
    public static final String CHCK_DIR_MS = "check.dir.ms";
    public static final String SCHEMA_NAME = "schema.name";
    public static final String TOPIC = "topic";
    public static final String FILE_PATH = "filename.path";

    private String tmp_path;
    private String check_dir_ms;
    private String schema_name;
    private String topic;
    private String use_dirwatcher;
    private String filename_path;

    private static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(USE_DIRWATCHER, Type.STRING, "true",
                    Importance.HIGH, "Whether to use the dir watcher")
            .define(DIR_PATH, Type.STRING, "./tmp", Importance.HIGH,
                    "Path to watch")
            .define(SCHEMA_NAME, Type.STRING, "filebinaryschema", Importance.MEDIUM,
                    "The name of schema")
            .define(TOPIC, Type.STRING, "file-binary", Importance.HIGH,
                    "The topic to write data")
            .define(FILE_PATH, Type.STRING, Importance.MEDIUM,
                    "The target file (not using the dir watcher)")
            .define(CHCK_DIR_MS, Type.INT, "1000", Importance.LOW,
                    "The maximum number of records the Source task can read from file one time");


    /**
     * Get the version of this connector.
     *
     * @return the version, formatted as a String
     */
    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }


    /**
     * Start this Connector. This method will only be called on a clean Connector, i.e. it has
     * either just been instantiated and initialized or {@link #stop()} has been invoked.
     *
     * @param props configuration settings
     */
    @Override
    public void start(Map<String, String> props) {
        use_dirwatcher = props.get(USE_DIRWATCHER);
        if(use_dirwatcher == null || use_dirwatcher.isEmpty())
            throw new ConnectException("missing use.java.dirwatcher");
        schema_name = props.get(SCHEMA_NAME);
        if(schema_name == null || schema_name.isEmpty())
            throw new ConnectException("missing schema.name");
        topic = props.get(TOPIC);
        if(topic == null || topic.isEmpty())
            throw new ConnectException("missing topic");

        if (use_dirwatcher == "true") {
            tmp_path = props.get(DIR_PATH);
            if(tmp_path == null || tmp_path.isEmpty())
                throw new ConnectException("missing tmp.path");
            check_dir_ms = props.get(CHCK_DIR_MS);
            if(check_dir_ms == null || check_dir_ms.isEmpty())
                check_dir_ms = "1000";
            filename_path = props.get(FILE_PATH);
            if(filename_path == null || filename_path.isEmpty())
                filename_path = "";
        }
        else if (use_dirwatcher == "false") {
            tmp_path = props.get(DIR_PATH);
            if(tmp_path == null || tmp_path.isEmpty())
                tmp_path = "";
            check_dir_ms = props.get(CHCK_DIR_MS);
            if(check_dir_ms == null || check_dir_ms.isEmpty())
                check_dir_ms = "";
            filename_path = props.get(FILE_PATH);
            if(filename_path == null || filename_path.isEmpty())
                throw new ConnectException("missing filename.path");
        }

    }


    /**
     * Returns the Task implementation for this Connector.
     *
     * @return tha Task implementation Class
     */
    @Override
    public Class<? extends Task> taskClass() {
        return BinarySourceTask.class;
    }


    /**
     * Returns a set of configurations for the Task based on the current configuration.
     * It always creates a single set of configurations.
     *
     * @param maxTasks maximum number of configurations to generate
     * @return configurations for the Task
     */
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();
        for(int i = 0; i < maxTasks; i++) {
            Map<String, String> config = new HashMap<>();
            config.put(USE_DIRWATCHER, use_dirwatcher);
            config.put(DIR_PATH, tmp_path);
            config.put(CHCK_DIR_MS, check_dir_ms);
            config.put(FILE_PATH, filename_path);
            config.put(SCHEMA_NAME, schema_name);
            config.put(TOPIC, topic);
            configs.add(config);
        }
        return configs;
    }


    /**
     * Stop this connector.
     */
    @Override
    public void stop() {

    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }
}
