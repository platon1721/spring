package ee.taltech.icd0011.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import ee.taltech.icd0011.util.ConnectionInfo;
import ee.taltech.icd0011.util.DataSourceProvider;
import ee.taltech.icd0011.util.FileUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@WebListener
public class DatabaseInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Class.forName("org.postgresql.Driver");

            ConnectionInfo connectionInfo = new ConnectionInfo(
                    "jdbc:postgresql://db.mkalmo.eu/platon1721",
                    "platon1721",
                    "3ef9132a"
            );
            DataSourceProvider.setConnectionInfo(connectionInfo);

            DataSource dataSource = DataSourceProvider.getDataSource();

            sce.getServletContext().setAttribute("dataSource", dataSource);

            initializeDatabase(dataSource);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void initializeDatabase(DataSource dataSource) throws Exception {
        String schema = FileUtil.readFileFromClasspath("schema.sql");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(schema);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DataSourceProvider.closePool();
    }
}