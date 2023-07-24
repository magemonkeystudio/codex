package mc.promcteam.engine.api.menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class FileExplorerMenu extends Menu {
    protected final String               root;
    protected final String               path;
    private final   Function<File, Slot> fileSlotFunction;

    public FileExplorerMenu(Player player, String root, int rows, String title, Function<File, Slot> fileSlotFunction) {
        super(player, rows, title);
        this.root = root + (root.endsWith(File.separator) || root.endsWith("/") ?
                "" : File.separator);
        this.path = "";
        this.fileSlotFunction = fileSlotFunction;
    }

    private FileExplorerMenu(Player player, String root, String path, int rows, String title, Function<File, Slot> fileSlotFunction) {
        super(player, rows, title);
        this.root = root;
        this.path = path + (path.endsWith(File.separator) || path.endsWith("/") ?
                "" : File.separator);
        this.fileSlotFunction = fileSlotFunction;
    }

    public String getRoot() {return root;}

    public String getPath() {return path;}

    public String getFullPath() {return root + path;}

    @Override
    public final void setContents() {
        int        lastDirectory = 0;
        File[]     fileArray     = Objects.requireNonNull(new File(getFullPath()).listFiles()); // Alphabetical order
        List<File> files         = new ArrayList<>(fileArray.length);
        for (File file : fileArray) { // Place directories first
            if (file.isDirectory()) {
                files.add(lastDirectory++, file);
            } else {
                files.add(file);
            }
        }
        int i = 0;
        for (File file : files) {
            i++;
            if (i % this.inventory.getSize() == 53) {
                this.setSlot(i, getNextButton());
                i++;
            } else if (i % 9 == 8) {i++;}
            if (i % this.inventory.getSize() == 45) {
                this.setSlot(i, getPrevButton());
                i++;
            } else if (i % 9 == 0) {i++;}
            if (file.isDirectory()) {
                this.setSlot(i, new DirectorySlot(file.getName()));
            } else {
                Slot slot = this.fileSlotFunction.apply(file);
                if (slot != null) {this.setSlot(i, slot);}
            }
        }
        this.setSlot(this.getPages() * this.inventory.getSize() - 9, getPrevButton());
        this.setSlot(this.getPages() * this.inventory.getSize() - 1, getNextButton());
    }

    public class DirectorySlot extends Slot {
        private final String name;

        public DirectorySlot(String name) {
            super(new ItemStack(Material.BOOK));
            this.name = name;
            ItemMeta meta = this.itemStack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RESET + name);
                this.itemStack.setItemMeta(meta);
            }
        }

        @Override
        public void onLeftClick() {
            openSubMenu(new FileExplorerMenu(
                    FileExplorerMenu.this.player,
                    FileExplorerMenu.this.root,
                    FileExplorerMenu.this.path + this.name,
                    FileExplorerMenu.this.inventory.getSize() / 9,
                    FileExplorerMenu.this.title,
                    FileExplorerMenu.this.fileSlotFunction));
        }
    }
}
