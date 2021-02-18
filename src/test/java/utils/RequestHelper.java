package utils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RequestHelper {
    public static JsonObject getBody(String webAddress, String requestBody) throws IOException {
        URL url = new URL(webAddress);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");

        con.setRequestProperty("Content-length", requestBody.getBytes().length + "");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setConnectTimeout(10);

        OutputStream outputStream = con.getOutputStream();
        outputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
        outputStream.close();

        con.connect();

        InputStream inputStream = con.getInputStream();
        JsonReader reader = Json.createReader(new InputStreamReader(inputStream));

        var output =  reader.readObject();
        con.disconnect();
        return output;
    }
}
