package diruptio.dynamite;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import diruptio.spikedog.HttpRequest;
import diruptio.spikedog.HttpResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectServlet implements BiConsumer<HttpRequest, HttpResponse> {
    public void accept(HttpRequest request, HttpResponse response) {
        // Get project parameter
        String project = request.getParameter("project");
        if (project == null) {
            response.setStatus(400, "Bad Request");
            response.setHeader("Content-Type", "application/json");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Parameter 'project' was not provided");
            response.setContent(content.toString());
            return;
        }

        // Check if project exists
        if (!Dynamite.getProjects().contains(project)) {
            response.setStatus(404, "Not Found");
            response.setHeader("Content-Type", "application/json");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Project not found");
            response.setContent(content.toString());
            return;
        }

        // Get versions
        List<String> versions = getVersions(project);
        if (versions == null) {
            response.setStatus(500, "Internal Server Error");
            response.setHeader("Content-Type", "application/json");
            JsonObject content = new JsonObject();
            content.addProperty("error", "An internal error occurred");
            response.setContent(content.toString());
            return;
        }

        // Success
        response.setStatus(200, "OK");
        response.setHeader("Content-Type", "application/json");
        JsonObject content = new JsonObject();
        content.addProperty("project", project);
        JsonArray versionsJson = new JsonArray();
        versions.forEach(versionsJson::add);
        content.add("versions", versionsJson);
        response.setContent(content.toString());
    }

    private @Nullable List<String> getVersions(@NotNull String project) {
        try (Stream<Path> children = Files.list(Dynamite.getProjectsPath().resolve(project))) {
            List<String> versions = new ArrayList<>();
            for (Path child : children.toList()) {
                if (Files.isDirectory(child)) {
                    versions.add(child.getFileName().toString());
                }
            }
            versions.sort(String::compareTo);
            return versions;
        } catch (IOException e) {
            return null;
        }
    }
}
