package studio.magemonkey.codex.api.items;

public class PrefixHelper {
    /**
     * Removes the given prefix from the provided id, if applicable.
     *
     * @param prefix the provider prefix to remove
     * @param id     the item id, prefixed or not
     * @return the stripped id
     */
    public static String stripPrefix(String prefix, String id) {
        String[] split = id.split("_", 2);
        return split.length == 2 && split[0].equalsIgnoreCase(prefix) ? split[1] : id;
    }
}
