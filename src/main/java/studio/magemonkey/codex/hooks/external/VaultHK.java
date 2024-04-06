package studio.magemonkey.codex.hooks.external;

import com.google.common.base.Preconditions;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.hooks.HookState;
import studio.magemonkey.codex.hooks.NHook;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class VaultHK extends NHook<CodexEngine> {
    private Economy    economy;
    private Permission permission;
    private Chat       chat;

    public VaultHK(@NotNull CodexEngine plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        this.setPermission();
        this.setEconomy();
        this.setChat();
        this.registerListeners();

        return HookState.SUCCESS;
    }

    private void setPermission() {
        RegisteredServiceProvider<Permission> pp =
                plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (pp == null) return;

        this.permission = pp.getProvider();
        this.plugin.info("Successfully hooked with " + permission.getName() + " permissions");
    }

    private void setEconomy() {
        RegisteredServiceProvider<Economy> pp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (pp == null) return;

        this.economy = pp.getProvider();
        this.plugin.info("Successfully hooked with " + economy.getName() + " economy");
    }

    private void setChat() {
        RegisteredServiceProvider<Chat> ch = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (ch == null) return;

        this.chat = ch.getProvider();
        this.plugin.info("Successfully hooked with " + chat.getName() + " chat");
    }

    @Override
    public void shutdown() {
        this.unregisterListeners();
    }

    // Some plugins load too late, so we need to listen to them to be able to hook into.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEconomyFix(ServiceRegisterEvent e) {
        Object provider = e.getProvider().getProvider();

        if (!this.hasEconomy() && provider instanceof Economy) {
            this.setEconomy();
            return;
        }

        if (!this.hasPermissions() && provider instanceof Permission) {
            this.setPermission();
            return;
        }

        if (!this.hasChat() && provider instanceof Chat) {
            this.setChat();
        }
    }

    public boolean hasPermissions() {
        return this.getPermissions() != null;
    }

    @Nullable
    public Permission getPermissions() {
        return this.permission;
    }

    public boolean hasChat() {
        return this.getChat() != null;
    }

    @Nullable
    public Chat getChat() {
        return this.chat;
    }

    public boolean hasEconomy() {
        return this.getEconomy() != null;
    }

    @Nullable
    public Economy getEconomy() {
        return this.economy;
    }

    @NotNull
    public String getEconomyName() {
        return this.hasEconomy() ? this.economy.getName() : "null";
    }

    @NotNull
    public String getPlayerGroup(@NotNull Player p) {
        if (this.permission == null || !this.permission.hasGroupSupport()) return "";

        String group = this.permission.getPrimaryGroup(p);
        return group == null ? "" : group;
    }

    @NotNull
    public Set<String> getPlayerGroups(@NotNull Player p) {
        if (this.permission == null || !this.permission.hasGroupSupport()) return Collections.emptySet();

        String[] groups1 = this.permission.getPlayerGroups(p);
        if (groups1 == null) return Collections.emptySet();

        Set<String> groups = Arrays.asList(groups1).stream()
                .map(String::toLowerCase).collect(Collectors.toSet());
        return groups;
    }

    @NotNull
    public String getPrefix(@NotNull Player p) {
        return this.hasChat() ? this.chat.getPlayerPrefix(p) : "";
    }

    @NotNull
    public String getSuffix(@NotNull Player p) {
        return this.hasChat() ? this.chat.getPlayerSuffix(p) : "";
    }

    public double getBalance(@NotNull OfflinePlayer p) {
        return this.economy.getBalance(p);
    }

    public boolean give(@NotNull OfflinePlayer p, double amount) {
        return this.economy.depositPlayer(p, amount).transactionSuccess();
    }

    public boolean take(@NotNull OfflinePlayer p, double amount) {
        return this.economy.withdrawPlayer(p, Math.min(Math.abs(amount), this.getBalance(p))).transactionSuccess();
    }

    public boolean canPay(@NotNull OfflinePlayer p, double amount) {
        Preconditions.checkArgument(hasEconomy(), "No economy plugin found");
        return this.economy.has(p, amount);
    }

    public void reset(final OfflinePlayer player) {
        this.economy.depositPlayer(player, getBalance(player));
        try {
            this.economy.createPlayerAccount(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String format(double input) {
        return this.economy.format(input);
    }
}
