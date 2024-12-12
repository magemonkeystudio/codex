package studio.magemonkey.codex.testutil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.commands.CommandRegister;
import studio.magemonkey.codex.commands.api.IGeneralCommand;
import studio.magemonkey.codex.compat.NMS;
import studio.magemonkey.codex.compat.VersionManager;
import studio.magemonkey.codex.core.config.CoreLang;
import studio.magemonkey.codex.hooks.HookManager;
import studio.magemonkey.codex.mccore.commands.CommandManager;
import studio.magemonkey.codex.mccore.scoreboard.Board;
import studio.magemonkey.codex.util.InventoryUtil;
import studio.magemonkey.codex.util.Reflex;
import studio.magemonkey.codex.util.actions.ActionsManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventFilterMatcher.hasFiredFilteredEvent;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MockedTest {
    protected ServerMock                    server;
    protected CodexEngine                   plugin;
    protected List<PlayerMock>              players = new ArrayList<>();
    protected MockedStatic<Reflex>          reflex;
    protected MockedStatic<CommandRegister> commandRegister;
    protected MockedStatic<Board>           board;
    protected MockedStatic<InventoryUtil>   inventoryUtil;
    protected HookManager                   hookManager;
    protected ActionsManager                actionsManager;
    protected CoreLang                      coreLang;

    @BeforeAll
    public void setupServer() {
        server = spy(MockBukkit.mock());
        commandRegister = mockStatic(CommandRegister.class);
        commandRegister.when(() -> CommandRegister.register(any(), any(IGeneralCommand.class)))
                .thenAnswer(ans -> {
                    Plugin             plugin  = ans.getArgument(0);
                    IGeneralCommand<?> command = ans.getArgument(1);
                    CommandRegister cmd = new CommandRegister(command.labels(),
                            command.description(),
                            command.usage(),
                            command,
                            plugin);
                    cmd.setTabCompleter(command);
                    cmd.setPermission(command.getPermission());

                    server.getCommandMap().register(plugin.getDescription().getName(), cmd);
                    return null;
                });
        String coreVersion = System.getProperty("CODEX_VERSION");

        try {
            File itemsJar = new File(server.getPluginsFolder().getAbsolutePath(), "CodexCore-" + coreVersion + ".jar");
            if (!itemsJar.exists()) itemsJar.createNewFile();
            createZipArchive(itemsJar, "target/classes");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        board = mockStatic(Board.class);

        inventoryUtil = mockStatic(InventoryUtil.class);
        inventoryUtil.when(() -> InventoryUtil.getTopInventory(any(Player.class)))
                .thenAnswer(ans -> {
                    Player    player = ((Player) ans.getArgument(0));
                    Inventory inv    = player.getOpenInventory().getTopInventory();
                    //noinspection ConstantValue
                    if (inv != null) return inv;

                    // It shouldn't be possible to have a null topInventory, but is for MockBukkit
                    return player.getInventory();
                });

        NMS nms = mock(NMS.class);
        when(nms.getVersion()).thenReturn("test");
        when(nms.fixColors(anyString())).thenAnswer(ans -> ans.getArgument(0));

        VersionManager.setNms(nms);

        reflex = mockStatic(Reflex.class);
        reflex.when(() -> Reflex.getClass(startsWith("studio.magemonkey"), anyString()))
                .thenCallRealMethod();
        reflex.when(() -> Reflex.getClass(startsWith("studio.magemonkey")))
                .thenCallRealMethod();
        reflex.when(() -> Reflex.getFields(any()))
                .thenAnswer(ans -> {
                    Class<?> clazz = ans.getArgument(0);
                    if (clazz.getSuperclass() != null
                            && clazz.getSuperclass().getSimpleName().equals("ILangTemplate")) {
                        return ans.callRealMethod();
                    }
                    return List.of();
                });

        coreLang = mock(CoreLang.class);
        when(coreLang.getEnum(any()))
                .thenAnswer(args -> {
                    Enum<?> e    = args.getArgument(0);
                    String  path = e.getClass().getSimpleName() + "." + e.name();
                    return path;
                });

        hookManager = mock(HookManager.class);
        actionsManager = mock(ActionsManager.class);
        plugin = MockBukkit.load(CodexEngine.class);
    }

    @AfterAll
    public void destroy() {
        if (reflex != null) reflex.close();
        if (commandRegister != null) commandRegister.close();
        if (board != null) board.close();
        CommandManager.unregisterAll();
        MockBukkit.unmock();
        if (inventoryUtil != null) inventoryUtil.close();
    }

    @AfterEach
    public void clearData() {
        clearEvents();
        players.clear();
    }

    public PlayerMock genPlayer(String name) {
        return genPlayer(name, true);
    }

    public PlayerMock genPlayer(String name, boolean op) {
//        PlayerMock pm = server.addPlayer(name);
        PlayerMock pm = new PlayerMock(server, name, UUID.randomUUID());
        server.addPlayer(pm);
        players.add(pm);
        pm.setOp(op);

        return pm;
    }

    public <T extends Event> void assertEventFired(Class<T> clazz) {
        hasFiredEventInstance(clazz).matches(server.getPluginManager());
    }

    public <T extends Event> void assertEventFired(Class<T> clazz, Predicate<T> predicate) {
        hasFiredFilteredEvent(clazz, predicate).matches(server.getPluginManager());
    }

    public void clearEvents() {
        server.getPluginManager().clearEvents();
    }

    private final static int BUFFER = 2048;

    public boolean createZipArchive(File destFile, String srcFolder) {
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destFile)))) {
            addFolder(srcFolder, "", out);
        } catch (Exception e) {
            System.out.println("createZipArchive threw exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void addFolder(String srcFolder, String baseFolder, ZipOutputStream out) throws IOException {
        File     subDir     = new File(srcFolder);
        String[] subdirList = subDir.list();
        for (String sd : subdirList) {
            // get a list of files from current directory
            File f = new File(srcFolder + "/" + sd);
            if (f.isDirectory())
                addFolder(f.getAbsolutePath(), baseFolder + "/" + sd, out);
            else {//it is just a file
                addFile(new FileInputStream(f), baseFolder + "/" + sd, out);
            }
        }
    }

    @NotNull
    private void addFile(FileInputStream f, String sd, ZipOutputStream out) throws IOException {
        byte            data[] = new byte[BUFFER];
        FileInputStream fi     = f;
        try (BufferedInputStream origin = new BufferedInputStream(fi, BUFFER)) {
            ZipEntry entry = new ZipEntry(sd);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
                out.flush();
            }
        }
    }
}
