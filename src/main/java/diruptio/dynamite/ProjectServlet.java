package diruptio.dynamite;

import com.google.gson.JsonObject;
import diruptio.spikedog.HttpRequest;
import diruptio.spikedog.HttpResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ProjectServlet implements BiConsumer<HttpRequest, HttpResponse> {
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

        // Get filter parameter
        String filterParam = request.getParameter("filter");
        Predicate<Project.Version> filter;
        if (filterParam == null) {
            filter = version -> true;
        } else if (filterParam.startsWith("tags:")) {
            filterParam = filterParam.replaceFirst("tags:", "");
            List<String> tags = filterParam.isBlank() ? List.of() : List.of(filterParam.split(","));
            filter = version -> new HashSet<>(version.tags()).containsAll(tags);
        } else {
            response.setStatus(400, "Bad Request");
            JsonObject content = new JsonObject();
            content.addProperty("error", "Invalid filter");
            response.setContent(content.toString());
            return;
        }

        // Success
        response.setStatus(200, "OK");
        response.setContent(project.get().toJson(filter).toString());
    }
}
