package diruptio.dynamite;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import diruptio.spikedog.HttpRequest;
import diruptio.spikedog.HttpResponse;
import java.util.List;
import java.util.function.BiConsumer;

public class ProjectServlet implements BiConsumer<HttpRequest, HttpResponse> {
    public void accept(HttpRequest request, HttpResponse response) {
        response.setHeader("Content-Type", "application/json");

        // Get project parameter
        String project = request.getParameter("project");
        if (project == null) {
            response.setStatus(400, "Bad Request");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Parameter 'project' was not provided");
            response.setContent(content.toString());
            return;
        }

        // Check if project exists
        if (!Dynamite.getProjects().contains(project)) {
            response.setStatus(404, "Not Found");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Project not found");
            response.setContent(content.toString());
            return;
        }

        // Get versions
        List<String> versions = Dynamite.getVersions(project);
        if (versions == null) {
            response.setStatus(500, "Internal Server Error");
            JsonObject content = new JsonObject();
            content.addProperty("error", "An internal error occurred");
            response.setContent(content.toString());
            return;
        }

        // Success
        response.setStatus(200, "OK");
        JsonObject content = new JsonObject();
        content.addProperty("project", project);
        JsonArray versionsJson = new JsonArray();
        versions.forEach(versionsJson::add);
        content.add("versions", versionsJson);
        response.setContent(content.toString());
    }
}
