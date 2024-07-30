package diruptio.dynamite.project.version;

import com.google.gson.JsonObject;
import diruptio.dynamite.Dynamite;
import diruptio.dynamite.Project;
import diruptio.spikedog.HttpRequest;
import diruptio.spikedog.HttpResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class VersionUploadServlet implements BiConsumer<HttpRequest, HttpResponse> {
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
        Optional<Project> project = Dynamite.getProjects().stream()
                .filter(project2 -> project2.name().equals(projectParam))
                .findFirst();
        if (project.isEmpty()) {
            response.setStatus(404, "Not Found");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Project not found");
            response.setContent(content.toString());
            return;
        }
        Path projectPath = Dynamite.getProjectsPath().resolve(projectParam);

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
                .noneMatch(version2 -> version2.name().equals(versionParam))) {
            response.setStatus(404, "Not Found");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Version not found");
            response.setContent(content.toString());
            return;
        }
        Path versionPath = projectPath.resolve(versionParam);

        // Get file parameter
        String fileParam = request.getParameter("file");
        if (fileParam == null) {
            response.setStatus(400, "Bad Request");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Parameter 'file' was not provided");
            response.setContent(content.toString());
            return;
        }

        try {
            if (!Files.exists(versionPath)) {
                Files.createDirectories(versionPath);
            }
            Files.write(versionPath.resolve(fileParam), request.getContent().getBytes(), StandardOpenOption.CREATE);
            String log = "Uploaded file %s for version %s of project %s";
            Dynamite.getLogger().info(log.formatted(fileParam, versionParam, projectParam));
        } catch (IOException exception) {
            Dynamite.getLogger().log(Level.SEVERE, "Failed to write download file", exception);
            response.setStatus(500, "Internal Server Error");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Failed to write file");
            response.setContent(content.toString());
            return;
        }

        // Success
        response.setStatus(200, "OK");
    }
}
