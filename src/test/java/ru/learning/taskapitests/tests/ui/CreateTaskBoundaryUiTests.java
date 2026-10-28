package ru.learning.taskapitests.tests.ui;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import ru.learning.taskapitests.Tags;
import ru.learning.taskapitests.data.TaskDataFactory;
import ru.learning.taskapitests.generated.model.CreateTaskRequest;
import ru.learning.taskapitests.ui.UiTestBase;
import ru.learning.taskapitests.ui.components.TaskForm;
import ru.learning.taskapitests.ui.pages.CreateTaskPage;

import java.util.function.Function;
import java.util.stream.Stream;

@Tag(Tags.UI)
@Tag(Tags.REGRESSION)
@Tag(Tags.NEGATIVE)
@Epic("UI")
@Feature("Граничные значения формы создания")
public class CreateTaskBoundaryUiTests extends UiTestBase {


    @ParameterizedTest(name = "{0}: {1} символов")
    @CsvSource({"title, 255",
            "assignee, 1",
            "assignee, 30",
            "description, 5000"})
    @DisplayName("Допустимые граничные значения принимаются и сохраняются целиком")
    void shouldCreateTaskWithValidLength(String field, int length) {
        var request = requestWithFieldLength(field, length);
        long taskId = uiStep.createTaskViaUi(navStep.openCreateTaskPage(),request);
        dbStep.checkTaskSaved(taskId,request);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tooLongValues")
    @Tag(Tags.NEGATIVE)
    @DisplayName("Слишком длинные значения не принимаются")
    void shouldRejectTooLongValue(String caseName,
                                  CreateTaskRequest request,
                                  Function<TaskForm, SelenideElement> errorField,
                                  String expectedMessage) {
        CreateTaskPage page = submitInvalidCreateFormAndCheckNothingSaved(request, 1);

        uiStep.checkFieldError(errorField.apply(page.form()), expectedMessage);
    }

    static Stream<Arguments> tooLongValues() {
        return Stream.of(
                tooLongCase("название 256 символов",
                        TaskDataFactory.customTaskRequestDTO(
                                r -> r.title(TaskDataFactory.uniqueTitleOfLength(256))),
                        TaskForm::titleError,
                        "Название не должно превышать 255 символов"),
                tooLongCase("исполнитель 101 символ",
                        TaskDataFactory.customTaskRequestDTO(
                                r -> r.assignee(TaskDataFactory.stringOfLength(101))),
                        TaskForm::assigneeError,
                        "Имя исполнителя не должно превышать 30 символов"),
                tooLongCase("описание 5001 символ",
                        TaskDataFactory.customTaskRequestDTO(
                                r -> r.description(TaskDataFactory.stringOfLength(5001))),
                        TaskForm::descriptionError,
                        "Описание не должно превышать 5000 символов")
        );
    }

    private static Arguments tooLongCase(String caseName,
                                         CreateTaskRequest request,
                                         Function<TaskForm, SelenideElement> errorField,
                                         String expectedMessage) {
        return Arguments.of(caseName, request, errorField, expectedMessage);
    }

    private CreateTaskRequest requestWithFieldLength(String field, int length) {
        return TaskDataFactory.customTaskRequestDTO(request -> {
            switch (field) {
                case "title" -> request.title(TaskDataFactory.uniqueTitleOfLength(length));
                case "assignee" -> request.assignee(TaskDataFactory.stringOfLength(length));
                case "description" -> request.description(TaskDataFactory.stringOfLength(length));
                default -> throw new IllegalArgumentException("Неизвестное поле: " + field);
            }

        });
    }

}
