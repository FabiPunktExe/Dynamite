package diruptio.dynamite;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import diruptio.spikedog.HttpRequest;
import diruptio.spikedog.HttpResponse;
import java.util.function.BiConsumer;

public class ProjectsServlet implements BiConsumer<HttpRequest, HttpResponse> {
    public void accept(HttpRequest request, HttpResponse response) {
        response.setStatus(200, "OK");
        response.setHeader("Content-Type", "application/json");
        JsonObject content = new JsonObject();
        JsonArray projects = new JsonArray();
        Dynamite.getProjects().forEach(projects::add);
        content.add("projects", projects);
        response.setContent(content.toString());
    }
}
