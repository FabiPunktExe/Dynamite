package diruptio.dynamite;

import diruptio.spikedog.Listener;
import diruptio.spikedog.Module;
import diruptio.spikedog.config.Config;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class Dynamite implements Listener {
    private static Config config;

    @Override
    public void onLoad(@NotNull Module self) {
        Path configFile = self.file().resolveSibling("Dynamite").resolve("config.yml");
        config = new Config(configFile, Config.Type.YAML);
    }

    public static Config getConfig() {
        return config;
    }
}
