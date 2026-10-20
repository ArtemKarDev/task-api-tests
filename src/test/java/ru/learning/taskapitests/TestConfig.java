package ru.learning.taskapitests;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestConfig {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = TestConfig.class
                                    .getClassLoader()
                                    .getResourceAsStream("config.properties")){
            if(input == null){throw new IllegalStateException("Файл config.properties не найден!");}

        PROPERTIES.load(input);
        } catch (IOException e){
            throw new IllegalStateException("Неудалось прочитать config.properties!", e);
        }
    }

    static  String getBaseUrl(){
        return PROPERTIES.getProperty("base.url");
    }


}
