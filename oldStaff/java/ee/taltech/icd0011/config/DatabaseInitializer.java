package ee.taltech.icd0011.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;

@WebListener
public class DatabaseInitializer implements ServletContextListener {

    private ApplicationContext springContext;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        springContext = new AnnotationConfigApplicationContext(AppConfig.class);

        sce.getServletContext().setAttribute("springContext", springContext);

        DataSource dataSource = springContext.getBean(DataSource.class);
        sce.getServletContext().setAttribute("dataSource", dataSource);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (springContext instanceof AnnotationConfigApplicationContext) {
            ((AnnotationConfigApplicationContext) springContext).close();
        }
    }
}