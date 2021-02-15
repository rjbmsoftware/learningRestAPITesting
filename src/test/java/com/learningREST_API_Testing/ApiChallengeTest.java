package com.learningREST_API_Testing;

import org.junit.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

public class ApiChallengeTest {
    private static final String baseURL = "http://localhost:4567";
    private static final String toDoAPI = "/todos";
    private static final String toDoIDFormat = baseURL + toDoAPI + "/{id}";

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
        when().get(baseURL + toDoAPI).then().statusCode(200);
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
        String filteredTodos = baseURL + toDoAPI + "?status=false";
        when().get(filteredTodos).then().body("todos.doneStatus", everyItem(is(false)));
    }

    @Test
    public void todoHeadRequestSuccess() {
        when().head(baseURL + toDoAPI).then()
                .statusCode(200);
    }

    @Test
    public void optionsRequest() {
        when().options(baseURL + toDoAPI).then()
                .statusCode(200)
                .header("Allow", "OPTIONS, GET, HEAD, POST");
    }
}


















