package kilim.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            String host = "www.w3.org";
            List<Task> tasks = new ArrayList<Task>();
            tasks.add(get(host, "/TR/html401/html40.txt", this.client));
            tasks.add(get(host, "/TR/2002/REC-xhtml1-20020801/xhtml1.pdf", this.client));
            tasks.add(get(host, "/TR/REC-html32.html", this.client));
            tasks.add(get(host, "/TR/2000/REC-DOM-Level-2-Core-20001113/DOM2-Core.txt", this.client));
            for (Task task : tasks) {
                System.out
                    .println("--------------------------------------------------------------------------------------------------------------------------");
                final HttpResponse resp = (HttpResponse) task.join().task.exitResult;
                System.out.println("status:"+resp.status());
                //System.out.println("content:"+resp.content());
            }
        }
    }


    public static Task get(final String host, final String path, final HttpClient client) throws Pausable {
        Task task = new Task() {
            @Override
            public void execute() throws Pausable, Exception {
                HttpResponse resp = client.get("http://" + host + path);
                this.exitResult = resp;
            }
        };
        task.start();
        return task;
    }


    public static void main(String[] args) throws IOException {
        HttpClient client = new HttpClient();

        long start = System.currentTimeMillis();
        SimpleTask task = new SimpleTask(client);
        task.start();
        while (!task.isDone()) {
            task.resume();
        }
        System.out.println(System.currentTimeMillis() - start);

    }
}
