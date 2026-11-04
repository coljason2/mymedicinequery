import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class TestFda {
    public static void main(String[] args) throws Exception {
        String jsonBody = "{\"data\":{\"licBaseId\":\"02022931\"}}";

        Connection.Response response = Jsoup.connect("https://lmspiq.fda.gov.tw/api/public/sh/piq/1000/licSearch")
                .header("token", "mwsKzg4NOWyRsvQjrrg4HSBVUkKRhqNuNPEVUfGDvlME7jrehkCkzw==")
                .header("Content-Type", "application/json")
                .ignoreContentType(true)
                .requestBody(jsonBody)
                .method(Connection.Method.POST)
                .execute();

        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
    }
}
