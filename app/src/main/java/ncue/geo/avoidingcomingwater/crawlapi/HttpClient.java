package ncue.geo.avoidingcomingwater.crawlapi;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

    public String get(String uri) {
        Request request = new Request.Builder()
                .url(uri)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        return "FAIL REQUEST";
    }

    public String post(String uri, String json) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(uri)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
        } catch (IOException ignore) {
        }
        return "";
    }

    public String post(String uri, HashMap<?, ?> dataPair){
        return post(uri, new Gson().toJson(dataPair));
    }
}
