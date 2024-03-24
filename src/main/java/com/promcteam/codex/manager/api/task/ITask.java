package com.promcteam.codex.manager.api.task;

import com.promcteam.codex.CodexPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class ITask<P extends CodexPlugin<P>> {

    @NotNull
    protected P       plugin;
    protected int     id;
    protected long    interval;
    protected boolean async;

    public ITask(@NotNull P plugin, int interval, boolean async) {
        this(plugin, interval * 20L, async);
    }

    public ITask(@NotNull P plugin, long interval, boolean async) {
        this.plugin = plugin;
        this.interval = interval;
        this.async = async;
    }

    public abstract void action();

    public void start() {
        if (this.interval <= 0) return;

        if (async) {
            this.async();
        } else {
            this.sync();
        }
    }

    private void sync() {
        this.id = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            this.action();
        }, 1L, interval).getTaskId();
    }

    private void async() {
        this.id = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            this.action();
        }, 1L, interval).getTaskId();
    }

    public void stop() {
        if (this.interval <= 0) return;

        this.plugin.getServer().getScheduler().cancelTask(this.getId());
    }

    public int getId() {
        return id;
    }
}
