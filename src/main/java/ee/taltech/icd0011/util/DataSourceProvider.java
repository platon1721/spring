package ee.taltech.icd0011.util;


import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DataSourceProvider {

    private static ConnectionInfo connectionInfo;
    private static BasicDataSource dataSource;

    public static void setConnectionInfo(ConnectionInfo connectionInfo) {
        DataSourceProvider.connectionInfo = connectionInfo;
    }

    public static void closePool() {
        if (dataSource == null) {
            return;
        }

        try {
            dataSource.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static DataSource getDataSource() {
        if (dataSource != null) {
            return dataSource;
        }

        if (connectionInfo == null) {
            throw new IllegalStateException(
                    "Connection info is not configured. Use setConnectionInfo()");
        }

        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(connectionInfo.url());
        dataSource.setUsername(connectionInfo.user());
        dataSource.setPassword(connectionInfo.pass());
        dataSource.setMaxTotal(2);

        return dataSource;
    }
}
