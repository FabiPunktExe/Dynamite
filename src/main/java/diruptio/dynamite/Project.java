package diruptio.dynamite;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public record Project(@NotNull String name, @NotNull List<Version> versions) {
    public @NotNull JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        JsonArray versions = new JsonArray();
        for (Version version : this.versions) {
            versions.add(version.toJson());
        }
        json.add("versions", versions);
        return json;
    }

    public static @NotNull Project fromJson(@NotNull JsonObject json) {
        String name = json.get("name").getAsString();
        List<Version> versions = new ArrayList<>();
        for (JsonElement version : json.getAsJsonArray("versions")) {
            versions.add(Version.fromJson(version.getAsJsonObject()));
        }
        return new Project(name, versions);
    }

    public record Version(@NotNull String name, @NotNull List<String> tags) {
        public @NotNull JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            JsonArray tags = new JsonArray();
            for (String tag : this.tags) {
                tags.add(tag);
            }
            json.add("tags", tags);
            return json;
        }

        public static @NotNull Version fromJson(@NotNull JsonObject json) {
            String name = json.get("name").getAsString();
            List<String> tags = new ArrayList<>();
            for (JsonElement tag : json.getAsJsonArray("tags")) {
                tags.add(tag.getAsString());
            }
            return new Version(name, tags);
        }
    }
}
