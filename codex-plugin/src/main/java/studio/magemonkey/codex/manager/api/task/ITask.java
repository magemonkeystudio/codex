package studio.magemonkey.codex.manager.api.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;

@RequiredArgsConstructor
public abstract class ITask<P extends CodexPlugin<P>> {
    @NotNull
    protected final P       plugin;
    protected final long    interval;
    protected final boolean async;
    @Getter
    protected       int     id;

    public ITask(@NotNull P plugin, int interval, boolean async) {
        this(plugin, interval * 20L, async);
    }

    public abstract void action();

    public void start() {
        if (this.interval <= 0) return;

        if (async) this.async();
        else this.sync();
    }

    private void sync() {
        this.id = plugin.getServer()
                .getScheduler()
                .runTaskTimer(plugin, this::action, 1L, interval)
                .getTaskId();
    }

    private void async() {
        this.id = plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(plugin, this::action, 1L, interval)
                .getTaskId();
    }

    public void stop() {
        if (this.interval <= 0) return;

        this.plugin.getServer().getScheduler().cancelTask(this.getId());
    }
}
