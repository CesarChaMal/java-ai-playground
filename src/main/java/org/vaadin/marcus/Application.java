package org.vaadin.marcus;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


//@SpringBootApplication
@SpringBootApplication(
        excludeName = {
                "dev.langchain4j.openai.spring.AutoConfig"
        }
)
@Theme(value = "customer-support-agent")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
