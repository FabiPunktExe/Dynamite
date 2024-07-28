package diruptio.dynamite;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;
import diruptio.dynamite.project.CreateServlet;
import diruptio.dynamite.project.version.DownloadServlet;
import diruptio.spikedog.Listener;
import diruptio.spikedog.Module;
import diruptio.spikedog.Spikedog;
import diruptio.spikedog.config.Config;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Dynamite implements Listener {
    private static Config config;
    private static Path projectsPath;
    private static final @NotNull List<Project> projects = new ArrayList<>();

    @Override
    public void onLoad(@NotNull Module self) {
        Path configFile = self.file().resolveSibling("Dynamite").resolve("config.yml");
        config = new Config(configFile, Config.Type.YAML);
        if (!config.contains("password")) {
            config.set("password", "YOUR_PASSWORD");
            config.save();
        }
        if (!config.contains("projects_path")) {
            config.set("projects_path", "projects");
            config.save();
        }
        projectsPath = Path.of(Objects.requireNonNull(config.getString("projects_path")));

        loadProjects(projectsPath);

        Spikedog.addServlet("/projects", new ProjectsServlet());
        Spikedog.addServlet("/project", new ProjectServlet());
        Spikedog.addServlet("/project/create", new CreateServlet());
        Spikedog.addServlet("/project/download", new DownloadServlet());
    }

    private void loadProjects(@NotNull Path path) {
        try {
            projects.clear();
            Path projectsFile = path.resolve("projects.json");
            if (!Files.exists(projectsFile)) {
                Files.write(
                        projectsFile,
                        new JsonObject().toString().getBytes(),
                        StandardOpenOption.CREATE_NEW);
                return;
            }
            BufferedReader reader = Files.newBufferedReader(projectsFile);
            JsonStreamParser parser = new JsonStreamParser(reader);
            JsonArray json = parser.next().getAsJsonArray();
            reader.close();
            for (JsonElement project : json) {
                if (project.isJsonObject()) {
                    Dynamite.projects.add(Project.fromJson(project.getAsJsonObject()));
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }
    }

    public static @NotNull Config getConfig() {
        return config;
    }

    public static @NotNull Path getProjectsPath() {
        return projectsPath;
    }

    public static @NotNull List<Project> getProjects() {
        return projects;
    }

    public static @Nullable List<String> getVersions(@NotNull String project) {
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

    public static @Nullable List<String> getDownloads(
            @NotNull String project, @NotNull String version) {
        try (Stream<Path> children =
                Files.list(Dynamite.getProjectsPath().resolve(project).resolve(version))) {
            List<String> downloads = new ArrayList<>();
            for (Path child : children.toList()) {
                if (Files.isRegularFile(child)) {
                    downloads.add(child.getFileName().toString());
                }
            }
            downloads.sort(String::compareTo);
            return downloads;
        } catch (IOException e) {
            return null;
        }
    }

    public static void save() {
        try {
            Path projectsFile = projectsPath.resolve("projects.json");
            Files.write(projectsFile, projects.toString().getBytes(), StandardOpenOption.CREATE);
            for (Project project : projects) {
                Path projectPath = projectsPath.resolve(project.name());
                if (!Files.exists(projectPath)) {
                    Files.createDirectories(projectPath);
                }
                Path projectFile = projectPath.resolve("project.json");
                Files.write(
                        projectFile,
                        project.toJson().toString().getBytes(),
                        StandardOpenOption.CREATE);
            }
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }
    }
}
