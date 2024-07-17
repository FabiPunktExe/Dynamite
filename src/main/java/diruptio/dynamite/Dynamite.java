package diruptio.dynamite;

import diruptio.dynamite.project.DownloadServlet;
import diruptio.spikedog.Listener;
import diruptio.spikedog.Module;
import diruptio.spikedog.Spikedog;
import diruptio.spikedog.config.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Dynamite implements Listener {
    private static Path projectsPath;
    private static List<String> projects;

    @Override
    public void onLoad(@NotNull Module self) {
        Path configFile = self.file().resolveSibling("Dynamite").resolve("config.yml");
        Config config = new Config(configFile, Config.Type.YAML);
        if (!config.contains("projects_path")) {
            config.set("projects_path", "projects");
            config.save();
        }
        projectsPath = Path.of(Objects.requireNonNull(config.getString("projects_path")));
        if (!config.contains("projects")) {
            config.set("projects", List.of());
            config.save();
        }
        projects = config.getList("projects", List.of());

        Spikedog.addServlet("/projects", new ProjectsServlet());
        Spikedog.addServlet("/project", new ProjectServlet());
        Spikedog.addServlet("/project/download", new DownloadServlet());
    }

    public static @NotNull Path getProjectsPath() {
        return projectsPath;
    }

    public static @NotNull List<String> getProjects() {
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
}
