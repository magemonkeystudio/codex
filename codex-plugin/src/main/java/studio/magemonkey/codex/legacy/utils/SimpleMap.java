package studio.magemonkey.codex.legacy.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SimpleMap extends HashMap {


    public SimpleMap(Map<String, Object> map) {
        super.putAll(map);
    }

    public Optional<String> getString(String key) {
        if (super.get(key) != null)
            try {
                return Optional.ofNullable((String) super.get(key));
            } catch (ClassCastException e) {

            }
        return Optional.empty();
    }

    public Optional<Integer> getInt(String key) {
        if (super.get(key) != null)
            try {
                return Optional.ofNullable((Integer) super.get(key));
            } catch (ClassCastException e) {

            }
        return Optional.empty();
    }

    public Optional<Double> getDouble(String key) {
        if (super.get(key) != null)
            try {
                return Optional.ofNullable((Double) super.get(key));
            } catch (ClassCastException e) {

            }
        return Optional.empty();
    }

    public Optional<UUID> getUUID(String key) {
        if (super.get(key) != null) {
            try {
                return Optional.of(UUID.fromString((String) super.get(key)));
            } catch (ClassCastException e) {

            }
        }

        return Optional.empty();
    }

    public boolean getBoolean(String key) {
        if (super.get(key) != null)
            try {
                return (Boolean) super.get(key);
            } catch (ClassCastException e) {

            }

        return false;
    }
}
