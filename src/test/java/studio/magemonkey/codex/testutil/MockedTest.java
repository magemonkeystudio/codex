package studio.magemonkey.codex.testutil;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.commands.CommandRegister;
import studio.magemonkey.codex.commands.api.IGeneralCommand;
import studio.magemonkey.codex.core.config.CoreLang;
import studio.magemonkey.codex.hooks.HookManager;
import studio.magemonkey.codex.mccore.commands.CommandManager;
import studio.magemonkey.codex.mccore.scoreboard.Board;
import studio.magemonkey.codex.nms.NMS;
import studio.magemonkey.codex.testutil.reflection.TestReflectionUtil;
import studio.magemonkey.codex.util.Reflex;
import studio.magemonkey.codex.util.actions.ActionsManager;
import studio.magemonkey.codex.util.reflection.ReflectionManager;
import studio.magemonkey.codex.util.reflection.ReflectionUtil;
import studio.magemonkey.codex.util.reflection.Reflection_1_17;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MockedTest {
    protected ServerMock                      server;
    protected CodexEngine                     plugin;
    protected List<PlayerMock>                players = new ArrayList<>();
    protected MockedStatic<Reflex>            reflex;
    protected MockedStatic<CommandRegister>   commandRegister;
    protected MockedStatic<ReflectionUtil>    mockedReflection;
    protected MockedStatic<Reflection_1_17>   mockedReflection17;
    protected MockedStatic<Board>             board;
    protected MockedStatic<ReflectionManager> reflectionManager;
    protected ReflectionUtil                  reflectionUtil;
    protected HookManager                     hookManager;
    protected NMS                             nms;
    protected ActionsManager                  actionsManager;
    protected CoreLang                        coreLang;

    @BeforeAll
    public void setupServer() {
        server = spy(MockBukkit.mock());
        reflectionUtil = spy(new TestReflectionUtil());
        reflectionManager = mockStatic(ReflectionManager.class);
        reflectionManager.when(ReflectionManager::getReflectionUtil)
                .thenReturn(reflectionUtil);
        commandRegister = mockStatic(CommandRegister.class);
        commandRegister.when(() -> CommandRegister.register(any(), any(IGeneralCommand.class)))
                .thenAnswer(ans -> {
                    Plugin          plugin  = ans.getArgument(0);
                    IGeneralCommand command = ans.getArgument(1);
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
            File itemsJar = new File(server.getPluginsFolder().getAbsolutePath(), "Codex-" + coreVersion + ".jar");
            if (!itemsJar.exists()) itemsJar.createNewFile();
            createZipArchive(itemsJar, "target/classes");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        board = mockStatic(Board.class);
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
        mockedReflection = mockStatic(ReflectionUtil.class);
        mockedReflection17 = mockStatic(Reflection_1_17.class);

        coreLang = mock(CoreLang.class);
        when(coreLang.getEnum(any()))
                .thenAnswer(args -> {
                    Enum<?> e    = args.getArgument(0);
                    String  path = e.getClass().getSimpleName() + "." + e.name();
                    return path;
                });

        hookManager = mock(HookManager.class);
        actionsManager = mock(ActionsManager.class);
        nms = mock(NMS.class);
        when(nms.fixColors(anyString()))
                .thenAnswer(args -> args.getArgument(0));
        when(nms.toBase64(any())).thenReturn("");

        plugin = MockBukkit.load(CodexEngine.class);
    }

    @AfterAll
    public void destroy() {
        reflex.close();
        commandRegister.close();
        mockedReflection.close();
        mockedReflection17.close();
        board.close();
        reflectionManager.close();
        CommandManager.unregisterAll();
        MockBukkit.unmock();
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
        server.getPluginManager().assertEventFired(clazz);
    }

    public <T extends Event> void assertEventFired(Class<T> clazz, Predicate<T> predicate) {
        server.getPluginManager().assertEventFired(clazz, predicate);
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
        File   subDir       = new File(srcFolder);
        String subdirList[] = subDir.list();
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
