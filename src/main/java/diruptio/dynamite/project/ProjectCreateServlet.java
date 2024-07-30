package diruptio.dynamite.project;

import com.google.gson.JsonObject;
import diruptio.dynamite.Dynamite;
import diruptio.dynamite.Project;
import diruptio.spikedog.HttpRequest;
import diruptio.spikedog.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.function.BiConsumer;

public class ProjectCreateServlet implements BiConsumer<HttpRequest, HttpResponse> {
    public void accept(HttpRequest request, HttpResponse response) {
        response.setHeader("Content-Type", "application/json");

        // Authorization
        String password = ":" + Dynamite.getConfig().getString("password");
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        String auth = "Basic " + Base64.getEncoder().encodeToString(bytes);

        if (!auth.equals(request.getHeader("Authorization"))) {
            // Unauthorized
            response.setStatus(401, "Unauthorized");
            response.setHeader("WWW-Authenticate", "Basic charset=\"UTF-8\"");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Unauthorized");
            response.setContent(content.toString());
            return;
        }

        // Get project parameter
        String projectParam = request.getParameter("project");
        if (projectParam == null) {
            response.setStatus(400, "Bad Request");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Parameter 'project' was not provided");
            response.setContent(content.toString());
            return;
        }

        // Check if project exists
        if (Dynamite.getProjects().stream()
                .anyMatch(project2 -> project2.name().equals(projectParam))) {
            JsonObject content = new JsonObject();
            content.addProperty("comment", "Project already exists");
            response.setContent(content.toString());
        } else {
            Dynamite.getProjects().add(new Project(projectParam, new ArrayList<>()));
            Dynamite.save();
            Dynamite.getLogger().info("Created project: " + projectParam);
        }

        // Success
        response.setStatus(200, "OK");
    }
}
