package com.learningREST_API_Testing;

import org.junit.Test;
import utils.RequestHelper;

import java.io.*;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

public class ApiChallengeTest {
    private static final String baseURL = "http://localhost:4567";
    private static final String toDoAPI = baseURL + "/todos";
    private static final String toDoIDFormat = toDoAPI + "/{id}";

    @Test
    public void postCreatedSuccessfullyWithoutBody() {
        when().post(baseURL + "/challenger").then().statusCode(201);
    }

    @Test
    public void getChallengesSuccess() {
        when().get(baseURL + "/challenges").then().statusCode(200);
    }

    @Test
    public void getTodosSuccess() {
        when().get(toDoAPI).then().statusCode(200);
    }

    @Test
    public void invalidTodosEndpoint() {
        when().get(baseURL + "/todo").then().statusCode(404);
    }

    @Test
    public void getTodoByIdSuccess() {
        int validId = 1;
        when().get(toDoIDFormat, validId).then().statusCode(200);
    }

    @Test
    public void getTodoByInvalidIDNotFound() {
        int invalidId = 99999;
        when().get(toDoIDFormat, invalidId).then().statusCode(404);
    }

    // need to somehow create a done to do
//    @Test
//    public void filterDoneTodos() {
//        String filteredTodos = baseURL + toDoAPI + "?status=true";
//        when().get(filteredTodos).then().body("todos", hasSize(0));;
//    }

    @Test
    public void filterNotDoneTodos() {
        given().when().param("status", "false").get(toDoAPI)
                .then().body("todos.doneStatus", everyItem(is(false)));
    }

    @Test
    public void todoHeadRequestSuccess() {
        when().head(toDoAPI).then()
                .statusCode(200);
    }

    @Test
    public void optionsRequest() {
        when().options(toDoAPI).then()
                .statusCode(200)
                .header("Allow", "OPTIONS, GET, HEAD, POST");
    }

    @Test
    public void createNewTodoSuccess() {
        String requestBody = "{'title':'hi im new'}";
        given().body(requestBody).when().post(toDoAPI).then().statusCode(201);
    }

    @Test
    public void createToDoFailsOnDoneStatus() {
        String requestBody = "{'title': 'failsOnStatus', 'doneStatus': 'iShouldBeBool'}";
        given().body(requestBody).when().post(toDoAPI).then().statusCode(400);
    }

    @Test
    public void updateTodoByID() throws IOException {
        String requestBody = "{'title': 'upToDateToDoBody', 'doneStatus': false}";
        int someId = RequestHelper.getBody(toDoAPI, requestBody).getInt("id");
        requestBody = "{'title': 'newTitle', 'doneStatus': false}";
        given().body(requestBody)
                .when().post(toDoIDFormat, someId)
                .then().statusCode(200);

        given().header("Content-Type", "application/json")
                .when().get(toDoIDFormat, someId)
                .then().body("todos[0].title", hasToString("newTitle"));
    }
}