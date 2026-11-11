package ru.learning.taskapitests.client;


import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;
import ru.learning.taskapitests.auth.Role;
import ru.learning.taskapitests.generated.model.CreateUserRequest;
import ru.learning.taskapitests.generated.model.ResetPasswordRequest;
import ru.learning.taskapitests.generated.model.UpdateUserRequest;
import ru.learning.taskapitests.specs.ApiSpec;
import ru.learning.taskapitests.utils.AllureAttachments;
import ru.learning.taskapitests.utils.JsonUtils;

import static io.restassured.RestAssured.given;

@Component
public class AdminApiClient {

    public static final String ADMIN_API_USERS = "/api/admin/users";
    private final ApiSpec apiSpec;

    public AdminApiClient(ApiSpec apiSpec) {
        this.apiSpec = apiSpec;
    }

    @Step("Создать пользователя")
    public Response createUser(CreateUserRequest request) {
        AllureAttachments.attachRequest(JsonUtils.toJson(request));
        Response response = given()
                .spec(apiSpec.requestSpec(Role.ADMIN))
                .body(request)
                .when()
                .post(ADMIN_API_USERS);

        attachResponseDetails(response);

        return response;
    }

    @Step("Получить список пользователей")
    public Response listUsers() {
        Response response = given()
                .spec(apiSpec.requestSpec(Role.ADMIN))
                .when()
                .get(ADMIN_API_USERS);

        attachResponseDetails(response);

        return response;
    }

    @Step("Обновить данные пользователя {userId}")
    public Response updateUser(long userId, UpdateUserRequest request) {
        AllureAttachments.attachRequest(JsonUtils.toJson(request));
        Response response = given()
                .spec(apiSpec.requestSpec(Role.ADMIN))
                .body(request)
                .when()
                .patch(ADMIN_API_USERS + "/{id}", userId);

        attachResponseDetails(response);

        return response;
    }

    @Step("Сбросить пароль пользователя {userId}")
    public Response resetPassword(long userId, ResetPasswordRequest request) {
        AllureAttachments.attachRequest(JsonUtils.toJson(request));
        Response response = given()
                .spec(apiSpec.requestSpec(Role.ADMIN))
                .body(request)
                .when()
                .put(ADMIN_API_USERS + "/{id}/password", userId);

        attachResponseDetails(response);

        return response;
    }

    @Step("Удалить пользователя {userId}")
    public Response deleteUser(long userId) {

        Response response = given()
                .spec(apiSpec.requestSpec(Role.ADMIN))
                .when()
                .delete(ADMIN_API_USERS + "/{id}", userId);

        attachResponseDetails(response);

        return response;
    }

    private void attachResponseDetails(Response response) {
        AllureAttachments.attachStatus(response.statusCode());

        String responseBody = response.asPrettyString();
        if (!responseBody.isBlank()) {
            AllureAttachments.attachResponse(responseBody);
        }
    }

}
