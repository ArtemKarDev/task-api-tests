package ru.learning.taskapitests.config;

import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

public class ProfileGuard {

    static final List<String> ALLOWED = List.of("local", "dev", "stage");

    public ProfileGuard(Environment environment){
        String[] active = environment.getActiveProfiles();
        if (active.length != 1 || !ALLOWED.contains(active[0])){
            throw new IllegalStateException("Неизвестный стенд" + Arrays.toString(active) + ". Допустимо: " + ALLOWED + " (-Denv=local|dev|stage или переменная TEST_ENV)" );
        }
    }
}
