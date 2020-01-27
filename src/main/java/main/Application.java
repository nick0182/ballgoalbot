package main;

import org.springframework.beans.factory.InitializingBean;
import org.telegram.telegrambots.ApiContextInitializer;

public class Application implements InitializingBean {

    public void afterPropertiesSet() {
        ApiContextInitializer.init();
    }
}
