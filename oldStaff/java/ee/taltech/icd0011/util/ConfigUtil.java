package ee.taltech.icd0011.util;

import java.util.Properties;

public class ConfigUtil {

    public static ConnectionInfo readConnectionInfo() {
        Properties properties = PropertyLoader.loadApplicationProperties();

        return new ConnectionInfo(
                properties.getProperty("dbUrl"),
                properties.getProperty("dbUser"),
                properties.getProperty("dbPassword"));
    }

}
