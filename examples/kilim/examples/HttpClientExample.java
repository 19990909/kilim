package kilim.examples;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kilim.Pausable;
import kilim.Task;
import kilim.http.HttpClient;
import kilim.http.HttpResponse;


public class HttpClientExample {

    static class SimpleTask extends Task {
        HttpClient client;


        public SimpleTask(HttpClient client) {
            super();
            this.client = client;
        }


        @Override
        public void execute() throws Pausable, Exception {

            // HttpResponse httpResponse =
            // this.client.get("http://news.163.com/10/1103/18/6KJ7SQ2J00014JB5.html");
            // System.out.println(httpResponse.status());
            // System.out.println(httpResponse.content());

            Map<String, CharSequence> params = new HashMap<String, CharSequence>();
            params.put("firstname", "dennis");
            params.put("lastname", "zhuang");
            HttpClient.CHARSET = "utf-8";
            HttpResponse httpResponse =
                    this.client.post("http://localhost:8080/examples/servlets/servlet/RequestParamExample", params);
            System.out.println(httpResponse.status());
            System.out.println(httpResponse.content());

        }
    }


    public static void main(String[] args) throws IOException {
        HttpClient client = new HttpClient();
        SimpleTask task = new SimpleTask(client);
        task.start();

    }
}
