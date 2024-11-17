package studio.magemonkey.codex.modules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.core.config.CoreConfig;
import studio.magemonkey.codex.manager.api.Loadable;
import studio.magemonkey.codex.modules.IExternalModule.LoadPriority;
import studio.magemonkey.codex.util.FileUT;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleManager<P extends CodexPlugin<P>> implements Loadable {

    @NotNull
    private P                        plugin;
    private Map<String, IModule<P>>  modules;
    private List<IExternalModule<P>> externalCache;

    public ModuleManager(@NotNull P plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setup() {
        this.modules = new LinkedHashMap<>();
        this.externalCache = new ArrayList<>();

        // Prepare external module instances from .jar files.
        this.plugin.getConfigManager()
                .extractFullPath(plugin.getDataFolder() + CoreConfig.MODULES_PATH_EXTERNAL, "jar");
        FileUT.getFiles(plugin.getDataFolder() + CoreConfig.MODULES_PATH_EXTERNAL, false).forEach(file -> {
            IExternalModule<P> module = this.loadFromFile(file);
            if (module != null) this.externalCache.add(module);
        });
        this.plugin.info("Found " + this.externalCache.size() + " external module(s).");
    }

    @Override
    public void shutdown() {
        for (IModule<P> module : new HashMap<>(this.modules).values()) {
            this.unregister(module);
        }
        this.modules.clear();
    }

    /**
     * @param module Module instance.
     * @return An object instance of registered module. Returns NULL if module hasn't been registered.
     */
    @Nullable
    public IModule<P> register(@NotNull IModule<P> module) {
        if (!module.isEnabled()) return null;

        String id = module.getId();
        if (this.modules.containsKey(id)) {
            this.plugin.error("Could not register " + id + " module! Module with such id already registered!");
            return null;
        }

        long loadTook = System.currentTimeMillis();
        module.load();
        loadTook = System.currentTimeMillis() - loadTook;

        if (!module.isLoaded()) {
            this.plugin.error("Failed module load: " + module.name() + " v" + module.version());
            return null;
        }

        this.plugin.info("Loaded module: " + module.name() + " v" + module.version() + " in " + loadTook + " ms.");
        this.modules.put(id, module);
        return module;
    }

    public void registerExternal(@NotNull LoadPriority priority) {
        this.externalCache.removeIf(module -> {
            if (module.getPriority() == priority) {
                this.register(module);
                return true;
            }
            return false;
        });
    }

    public void unregister(@NotNull IModule<?> module) {
        String id = module.getId();
        if (this.modules.remove(id) != null) {
            this.plugin.info("Unloaded module: " + module.name() + " v" + module.version());
        }
        module.unload();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends IModule<P>> T getModule(@NotNull Class<T> clazz) {
        for (IModule<?> module : this.modules.values()) {
            if (clazz.isAssignableFrom(module.getClass())) {
                return (T) module;
            }
        }
        return null;
    }

    @Nullable
    public IModule<P> getModule(@NotNull String id) {
        return this.modules.get(id.toLowerCase());
    }

    @NotNull
    public Collection<IModule<P>> getModules() {
        return this.modules.values();
    }

    @SuppressWarnings({"resource", "unchecked"})
    @Nullable
    public IExternalModule<P> loadFromFile(@NotNull File jar) {
        if (!jar.getName().endsWith(".jar")) return null;

        try {
            JarFile               jarFile  = new JarFile(jar);
            Enumeration<JarEntry> jarEntry = jarFile.entries();
            URL[]                 urls     = {new URL("jar:file:" + jar.getPath() + "!/")};
            ClassLoader           loader   = URLClassLoader.newInstance(urls, plugin.getClazzLoader());

            while (jarEntry.hasMoreElements()) {
                JarEntry entry = (JarEntry) jarEntry.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;

                String   className = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
                Class<?> clazz     = Class.forName(className, false, loader); // second was 'true'
                if (IExternalModule.class.isAssignableFrom(clazz)) {
                    Class<? extends IExternalModule<P>> mainClass =
                            (Class<? extends IExternalModule<P>>) clazz.asSubclass(IExternalModule.class);
                    Constructor<? extends IExternalModule<P>> con    = mainClass.getConstructor(plugin.getClass());
                    IExternalModule<P>                        module = con.newInstance(plugin);
                    if (module == null) continue;

                    return module;
                    //this.plugin.info("Loaded External Module: " + module.getId() + " [" + jar.getName() + "]");
                }
            }
            //jarFile.close();
        } catch (Exception e) {
            this.plugin.error("Could not load external module: " + jar.getName());
            e.printStackTrace();
        }
        return null;
    }
}
