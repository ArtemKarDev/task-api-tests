package ru.learning.taskapitests.tests.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.TestBase;
import ru.learning.taskapitests.data.TaskDataFactory;

@Tag(Tags.API)
@Tag(Tags.REGRESSION)
@Epic("API")
@Feature("Создание задачи: допустимые значения")
class CreateTaskOptionalFieldsApiTests extends TestBase {

    @ParameterizedTest(name = "{0}: {1} символов")
    @CsvSource({
            "title, 255",
            "assignee, 1",
            "assignee, 100",
            "description, 5000"
    })
    @DisplayName("Допустимые граничные значения принимаются и сохраняются целиком")
    void shouldAcceptAllowedLength(String field, int length) {
        var request = TaskDataFactory.requestWithFieldLength(field, length);

        Response response = step.createTaskDto(request);

        step.checkStatus(response, 201);
        step.checkResponseCreateTaskDataWithDto(response, request);
        dbStep.checkTaskSaved(response.jsonPath().getLong("id"), request);
    }

    @ParameterizedTest(name = "исполнитель: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "     "})
    @DisplayName("Пустой или пробельный исполнитель сохраняется как «не указан»")
    void shouldStoreBlankAssigneeAsNull(String assignee) {
        var request = TaskDataFactory.customTaskRequestDTO(r -> r.assignee(assignee));

        Response response = step.createTaskDto(request);

        step.checkStatus(response, 201);
        step.checkJsonFieldIsNull(response, "assignee");
        dbStep.checkOptionalFields(response.jsonPath().getLong("id"), request.getDescription(), null);
    }

    @ParameterizedTest(name = "описание: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "     "})
    @DisplayName("Пустое или пробельное описание сохраняется как «не указано»")
    void shouldStoreBlankDescriptionAsNull(String description) {
        var request = TaskDataFactory.customTaskRequestDTO(r -> r.description(description));

        Response response = step.createTaskDto(request);

        step.checkStatus(response, 201);
        step.checkJsonFieldIsNull(response, "description");
        dbStep.checkOptionalFields(response.jsonPath().getLong("id"), null, request.getAssignee());
    }
}