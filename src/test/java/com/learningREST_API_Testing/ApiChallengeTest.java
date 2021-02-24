package com.learningREST_API_Testing;

import io.restassured.http.ContentType;
import io.restassured.http.Method;
import org.apache.http.HttpStatus;
import org.junit.Test;
import utils.RequestHelper;

import java.io.IOException;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.matcher.RestAssuredMatchers.matchesXsdInClasspath;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class ApiChallengeTest {
    private static final String baseURL = "http://localhost:4567";
    private static final String toDoAPI = baseURL + "/todos";
    private static final String toDoIDFormat = toDoAPI + "/{id}";
    private static final String heartbeatAPI = baseURL + "/heartbeat";
    private static final String secretAPI = baseURL + "/secret";
    private static final String secretToken = secretAPI + "/token";
    private static final String secretNote = secretAPI + "/note";
    private static final String userName = "admin";
    private static final String password = "password";

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
        given().when().get(toDoAPI).then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getTodoNoAcceptDefaultJSONResponse() {
        // todo still makes request with accept header
        var headerMap = new HashMap<String, String>();
        given().headers(headerMap).when().get(toDoAPI).then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON);
    }

    @Test
    public void invalidTodosEndpoint() {
        when().get(baseURL + "/todo").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void getTodoByIdSuccess() {
        int validId = 1;
        when().get(toDoIDFormat, validId).then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getTodoByInvalidIDNotFound() {
        int invalidId = 99999;
        when().get(toDoIDFormat, invalidId).then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void filterTodos() {
        // Given done and not done exist
        String requestBodyFormat = "{'title': '%s', 'doneStatus': %b}";
        given().body(String.format(requestBodyFormat, "notDone", false)).when().post(toDoAPI)
                .then().statusCode(HttpStatus.SC_CREATED);
        given().body(String.format(requestBodyFormat, "done", true)).when().post(toDoAPI)
                .then().statusCode(HttpStatus.SC_CREATED);

        given().param("doneStatus", true)
                .when().get(toDoAPI)
                .then().body("todos", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    public void filterNotDoneTodos() {
        given().when().param("doneStatus", "false").get(toDoAPI)
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
        given().body(requestBody).when().post(toDoAPI).then().statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void createNewTodoSuccessXMLWithJSONAccept() {
        String requestBody = "<todo><title>I am an XML todo</title></todo>";
        given().body(requestBody).contentType(ContentType.XML)
                .accept(ContentType.JSON)
                .when().post(toDoAPI)
                .then().statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void createNewTodoSuccessJSONWithXMLAccept() {
        String requestBody = "{'title':'hi im new'}";
        given().body(requestBody).contentType(ContentType.JSON)
                .accept(ContentType.XML)
                .when().post(toDoAPI)
                .then().statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void createNewTodoSuccessXMLAccept() {
        String requestBody = "<todo><title>I am an XML todo which accepts XML</title></todo>";
        given().body(requestBody).contentType(ContentType.XML)
                .accept(ContentType.XML)
                .when().post(toDoAPI)
                .then().statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void unsupportedContentTypePost() {
        given().contentType(ContentType.BINARY)
                .when().post(toDoAPI)
                .then().statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void createToDoFailsOnDoneStatus() {
        String requestBody = "{'title': 'failsOnStatus', 'doneStatus': 'iShouldBeBool'}";
        given().body(requestBody).when().post(toDoAPI).then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void updateTodoByID() throws IOException {
        String requestBody = "{'title': 'upToDateToDoBody', 'doneStatus': false}";
        int someId = RequestHelper.getBody(toDoAPI, requestBody).getInt("id");
        requestBody = "{'title': 'newTitle', 'doneStatus': false}";
        given().body(requestBody)
                .when().post(toDoIDFormat, someId)
                .then().statusCode(HttpStatus.SC_OK);

        given().contentType(ContentType.JSON)
                .when().get(toDoIDFormat, someId)
                .then().body("todos[0].title", hasToString("newTitle"));
    }

    @Test
    public void deleteTodoByID() throws IOException {
        //given item exists
        String requestBody = "{'title': 'delete me', 'doneStatus': false}";
        int id = RequestHelper.getBody(toDoAPI, requestBody).getInt("id");

        given().when().delete(toDoIDFormat, id).then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void toDoInJSON() {
        given().header("Accept", "application/json")
                .when().get(toDoAPI)
                .then().body(matchesJsonSchemaInClasspath("todoGET.json"));
    }

    @Test
    public void todoInXML() {
        given().header("Accept", "application/xml")
                .when().get(toDoAPI)
                .then().body(matchesXsdInClasspath("todoGET.xml"));
    }

    @Test
    public void todoInXML_AcceptJSON_AndXML() {
        given().header("Accept", "application/xml, application/json")
                .when().get(toDoAPI)
                .then().contentType(ContentType.XML);
    }

    @Test
    public void getTodoUnacceptable() {
        given().header("Accept", "application/gzip")
                .when().get(toDoAPI)
                .then().statusCode(HttpStatus.SC_NOT_ACCEPTABLE);
    }

    @Test
    public void deleteOnHeartbeatNotAllowed() {
        given().when().delete(heartbeatAPI).then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void serverErrorPatchHeartbeat() {
        given().when().patch(heartbeatAPI).then().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void traceNotImplementedOnHeartbeat() {
        given().when().request(Method.TRACE, heartbeatAPI).then().statusCode(HttpStatus.SC_NOT_IMPLEMENTED);
    }

    @Test
    public void getHeartBeatNoContent() {
        given().when().get(heartbeatAPI).then()
                .statusCode(HttpStatus.SC_NO_CONTENT)
                .body(emptyString());
    }

    @Test
    public void postInvalidSecretToken() {
        String invalidPassword = password + "123!";
        given().auth().basic(userName, invalidPassword)
                .when().post(secretToken)
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void validLogin() {
        given().auth().basic(userName, password)
                .when().post(secretToken)
                .then().statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void invalidAuthToken() {
        given().header("X-AUTH-TOKEN", "IM_AN_INVALID_TOKEN")
                .when().get(secretNote)
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void getSecretNoteUnauthorisedNoHeader() {
        given().when().get(secretNote).then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void postInvalidSecretNoteForbidden() {
        String requestBody = "{\"note\":\"my note\"}";
        given().header("X-AUTH-TOKEN", "IM_AN_INVALID_TOKEN").body(requestBody)
                .when().post(secretNote)
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void postSecretNoteWithoutAuthTokenUnauthorised() {
        String requestBody = "{\"note\":\"my note\"}";
        given().body(requestBody)
                .when().post(secretNote)
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}