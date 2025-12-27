package ee.taltech.icd0011.util;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public class ConnectionPoolFactory {

    public DataSource createConnectionPool() {
        ConnectionInfo connectionInfo = ConfigUtil.readConnectionInfo();

        BasicDataSource pool = new BasicDataSource();
        pool.setDriverClassName("org.postgresql.Driver");
        pool.setUrl(connectionInfo.url());
        pool.setUsername(connectionInfo.user());
        pool.setPassword(connectionInfo.pass());
        pool.setMaxTotal(2);
        pool.setInitialSize(1);

        try {
            pool.getLogWriter();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return pool;
    }
}