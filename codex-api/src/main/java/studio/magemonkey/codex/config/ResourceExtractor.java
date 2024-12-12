package studio.magemonkey.codex.config;

import org.apache.commons.lang3.Validate;
import org.bukkit.plugin.java.JavaPlugin;
import studio.magemonkey.codex.util.FileUT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ResourceExtractor {
    private final JavaPlugin plugin;
    private final File       extractFolder;

    private final String folderPath;
    private final String regex;

    /**
     * You can extract complete folders of resources from your plugin jar to a target folder. You
     * can append a regex to match the file names.
     *
     * @param plugin        The plugin the files will be extracted from.
     * @param extractFolder The folder where the files will be extracted to.
     * @param folderPath    The path where the files are inside in the jar located.
     * @param regex         A regex to match the file names. This can be 'null' if you don't want to use it.
     */
    public ResourceExtractor(JavaPlugin plugin, File extractFolder, String folderPath, String regex) {
        Validate.notNull(plugin, "The plugin cannot be null!");
        Validate.notNull(plugin, "The extract folder cannot be null!");
        Validate.notNull(plugin, "The folder path cannot be null!");

        this.extractFolder = extractFolder;
        this.folderPath = folderPath;
        this.plugin = plugin;
        this.regex = regex;
    }

    /**
     * Starts extracting the files.
     *
     * @throws IOException
     */
    public void extract() throws IOException {
        this.extract(false, true);
    }

    /**
     * Starts extracting the files.
     *
     * @param override Whether you want to override the old files.
     * @throws IOException
     */
    public void extract(boolean override) throws IOException {
        this.extract(override, true);
    }

    /**
     * Starts extracting the files.
     *
     * @param override Whether you want to override the old files.
     * @param subPaths Whether you want to create sub folders if it's also found in the jar file.
     * @throws IOException
     */
    public void extract(boolean override, boolean subPaths) throws IOException {
        File jarfile = null;

        /*
         * Get the jar file from the plugin.
         */
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);

            jarfile = (File) method.invoke(this.plugin);
        } catch (Exception e) {
            throw new IOException(e);
        }

        /*
         * Make the folders if missing.
         */
        if (!this.extractFolder.exists()) {
            this.extractFolder.mkdirs();
        }

        JarFile jar = new JarFile(jarfile);

        /*
         * Loop through all the entries.
         */
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String   path  = entry.getName();

            /*
             * Not in the folder.
             */
            if (!path.startsWith(this.folderPath)) {
                continue;
            }

            if (entry.isDirectory()) {
                if (subPaths) {
                    File file = new File(this.extractFolder, entry.getName().replaceFirst(this.folderPath, ""));
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                            /*System.out.println("1 " + entry.getName());
                            ResourceExtractor extract = new ResourceExtractor(plugin, new File(plugin.getDataFolder() + File.separator + entry.getName()), entry.getName(), ".*\\.(yml)$");
            	            
                	        try {
                	            extract.extract(false, true);
                	        } catch (IOException e) {
                	            e.printStackTrace();
                	        } */
                }
            } else {
                File file;

                /*
                 * Use the right path.
                 */
                if (subPaths) {
                    file = new File(this.extractFolder, path.replaceFirst(this.folderPath, ""));
                } else {
                    file = new File(this.extractFolder, path.substring(path.indexOf(File.separatorChar)));
                }

                String name = file.getName();

                /*
                 * Be sure that the file is valid.
                 */
                if (this.regex == null || name.matches(this.regex)) {
                    if (file.exists() && override) {
                        file.delete();
                    }

                    if (!file.exists()) {
                        FileUT.create(file);
                        /*
                         * Copy the file to the path.
                         */
                        InputStream      is  = jar.getInputStream(entry);
                        FileOutputStream fos = new FileOutputStream(file);

                        while (is.available() > 0) {
                            fos.write(is.read());
                        }

                        fos.close();
                        is.close();
                    }
                }
            }
        }

        jar.close();
    }
}