package diruptio.dynamite;

import diruptio.spikedog.Listener;
import diruptio.spikedog.Module;
import diruptio.spikedog.Spikedog;
import diruptio.spikedog.config.Config;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

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
    }

    public static @NotNull Path getProjectsPath() {
        return projectsPath;
    }

    public static @NotNull List<String> getProjects() {
        return projects;
    }
}
