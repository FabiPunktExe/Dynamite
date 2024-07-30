package diruptio.dynamite.project.version;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import diruptio.dynamite.Dynamite;
import diruptio.dynamite.Project;
import diruptio.spikedog.HttpRequest;
import diruptio.spikedog.HttpResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class VersionDownloadServlet implements BiConsumer<HttpRequest, HttpResponse> {
    public void accept(HttpRequest request, HttpResponse response) {
        response.setHeader("Content-Type", "application/json");

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
        Path versionPath = projectPath.resolve(versionParam);

        // Check if version exists
        Optional<Project.Version> version = project.get().versions().stream()
                .filter(version2 -> version2.name().equals(versionParam))
                .findFirst();
        if (version.isEmpty()) {
            response.setStatus(404, "Not Found");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Version not found");
            response.setContent(content.toString());
            return;
        }

        // Get downloads
        List<String> downloads = Dynamite.getDownloads(projectParam, versionParam);
        if (downloads == null) {
            response.setStatus(500, "Internal Server Error");
            JsonObject content = new JsonObject();
            content.addProperty("error", "An internal error occurred");
            response.setContent(content.toString());
            return;
        }

        String download;
        if (downloads.isEmpty()) {
            response.setStatus(404, "Not Found");
            JsonObject content = new JsonObject();
            content.addProperty("error", "No downloads found");
            response.setContent(content.toString());
            return;
        } else if (downloads.size() == 1) {
            download = downloads.get(0);
        } else {
            download = request.getParameter("download");
            if (download == null) {
                response.setStatus(400, "Bad Request");
                JsonObject content = new JsonObject();
                content.addProperty("error", "Multiple downloads found and no parameter 'download' was not provided");
                JsonArray downloadsJson = new JsonArray();
                downloads.forEach(downloadsJson::add);
                content.add("downloads", downloadsJson);
                response.setContent(content.toString());
                return;
            } else if (!Files.exists(versionPath.resolve(download))) {
                response.setStatus(404, "Not Found");
                JsonObject content = new JsonObject();
                content.addProperty("error", "Download not found");
                response.setContent(content.toString());
                return;
            }
        }

        String fileContent;
        try {
            fileContent = new String(Files.readAllBytes(versionPath.resolve(download)));
        } catch (IOException exception) {
            Dynamite.getLogger().log(Level.SEVERE, "Failed to read download file", exception);
            response.setStatus(500, "Internal Server Error");
            JsonObject content = new JsonObject();
            content.addProperty("error", "An internal error occurred");
            response.setContent(content.toString());
            return;
        }

        // Success
        response.setStatus(200, "OK");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + download + "\"");
        response.setContent(fileContent);
    }
}
