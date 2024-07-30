package diruptio.dynamite.project.version;

import com.google.gson.JsonObject;
import diruptio.dynamite.Dynamite;
import diruptio.dynamite.Project;
import diruptio.spikedog.HttpRequest;
import diruptio.spikedog.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class VerisonCreateServlet implements BiConsumer<HttpRequest, HttpResponse> {
    public void accept(HttpRequest request, HttpResponse response) {
        response.setHeader("Content-Type", "application/json");

        // Authorization
        String password = ":" + Dynamite.getConfig().getString("password");
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        String auth = "Basic " + Base64.getEncoder().encodeToString(bytes);

        if (!auth.equals(request.getHeader("Authorization"))) {
            // Unauthorized
            response.setStatus(401, "Unauthorized");
            response.setHeader("Content-Type", "text/html");
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
        Optional<Project> project =
                Dynamite.getProjects().stream()
                        .filter(project2 -> project2.name().equals(projectParam))
                        .findFirst();
        if (project.isEmpty()) {
            response.setStatus(404, "Not Found");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Project not found");
            response.setContent(content.toString());
            return;
        }

        // Get version parameter
        String versionParam = request.getParameter("version");
        if (versionParam == null) {
            response.setStatus(400, "Bad Request");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Parameter 'version' was not provided");
            response.setContent(content.toString());
            return;
        }

        // Check if version exists
        if (project.get().versions().stream()
                .anyMatch(version2 -> version2.name().equals(versionParam))) {
            response.setStatus(200, "OK");
            return;
        }

        // Get tags parameter
        String tagsParam = request.getParameter("tags");
        if (tagsParam == null) {
            response.setStatus(400, "Bad Request");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Parameter 'tags' was not provided");
            response.setContent(content.toString());
            return;
        }
        List<String> tags = tagsParam.isBlank() ? new ArrayList<>() : List.of(tagsParam.split(","));

        project.get().versions().add(new Project.Version(versionParam, tags));
        Dynamite.save();
        String log = "Created version %s with tags %s for project %s";
        Dynamite.getLogger().info(log.formatted(versionParam, tags, projectParam));

        // Success
        response.setStatus(200, "OK");
    }
}
