package ru.learning.taskapitests.utils;

import io.qameta.allure.Allure;

public class AllureAttachments {

    private AllureAttachments(){

    }

    public static void attachRequest(String requestBody){
        Allure.addAttachment("Тело запроса","application/json", requestBody,".json");
    }

    public static void attachStatus(int statusCode){
        Allure.addAttachment(
                "HTTP-статус: " + statusCode,"text/plain","HTTP-статус: " + statusCode);
    }

    public static void attachResponse(String responseBody){
        Allure.addAttachment("Тело ответа","application/json", responseBody,".json");
    }
}
