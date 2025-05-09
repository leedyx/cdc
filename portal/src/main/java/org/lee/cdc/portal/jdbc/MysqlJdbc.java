package org.lee.cdc.portal.jdbc;

import org.lee.cdc.portal.database.MySQLs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * mysql 数据库jdbc相关
 * <p>
 * 一个 jdbc版本对应一个实例
 */
public class MysqlJdbc {

    private final static Logger LOGGER = LoggerFactory.getLogger(MysqlJdbc.class);


    public MysqlJdbc() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
    }


    /**
     * 查询数据库下的所有schema
     * @param host
     * @param port
     * @param user
     * @param password
     * @return
     * @throws Exception
     */
    public List<String> querySchemas(String host, int port, String user, String password) throws Exception {

        Connection connection = null;
        ResultSet resultSet = null;
        try {
            String url = String.format("jdbc:mysql://%s:%d", host, port);
            connection = DriverManager.getConnection(url, user, password);
            DatabaseMetaData metaData = connection.getMetaData();
            List<String> schemas = new ArrayList<>();
            resultSet = metaData.getCatalogs();
            while (resultSet.next()) {
                String catalog = resultSet.getString(1);
                if (MySQLs.DEFAULT_DATABASE.contains(catalog)) {
                    continue;
                }
                schemas.add(catalog);
            }

            return schemas;

        } finally {

            if (Objects.nonNull(resultSet)) {

                resultSet.close();
            }

            if (Objects.nonNull(connection)) {
                connection.close();
            }

        }
    }

    /**
     * 查询数据库版本
     * @param host
     * @param port
     * @param user
     * @param password
     * @return
     * @throws Exception
     */
    public String queryVersion(String host, int port, String user, String password) throws Exception {
        Connection connection = null;
        try {
            String url = String.format("jdbc:mysql://%s:%d", host, port);
            connection = DriverManager.getConnection(url, user, password);
            DatabaseMetaData metaData = connection.getMetaData();
            String version = metaData.getDatabaseProductVersion();
            return version;
        } finally {

            if (Objects.nonNull(connection)) {
                connection.close();
            }

        }
    }


    public List<String> queryTables(String host, int port, String user, String password,String schema) throws Exception {

        Connection connection = null;
        ResultSet resultSet = null;
        try {
            String url = String.format("jdbc:mysql://%s:%d/%s", host, port,schema);
            connection = DriverManager.getConnection(url, user, password);
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getTables(schema, schema, null, new String[]{"TABLE"});
            List<String> tables = new ArrayList<>();
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String tableType = resultSet.getString("TABLE_TYPE");
                LOGGER.info("tableName:{} -- tableType:{}", tableName, tableType);
            }

            return tables;
        }finally {

            if (Objects.nonNull(resultSet)) {
                resultSet.close();
            }

            if (Objects.nonNull(connection)) {
                connection.close();
            }
        }
    }


    public static void main(String[] args) throws Exception {
        MysqlJdbc mysqlJdbc = new MysqlJdbc();

        mysqlJdbc.queryTables("192.168.5.4",
                33060,
                "root",
                "leeqian",
        "cdc");
    }




}
