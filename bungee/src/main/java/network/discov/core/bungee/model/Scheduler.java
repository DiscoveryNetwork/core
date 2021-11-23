package network.discov.core.bungee.model;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import network.discov.core.bungee.Core;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    List<Integer> tasks = new ArrayList<>();

    public void cancelTask(int i) {
        ProxyServer.getInstance().getScheduler().cancel(i);
    }

    public void cancelTasks() {
        for (int taskId : tasks) {
            cancelTask(taskId);
        }
    }

    public @NotNull ScheduledTask runAsync(@NotNull Runnable runnable) {
        ScheduledTask task = ProxyServer.getInstance().getScheduler().runAsync(Core.getInstance(), runnable);
        tasks.add(task.getId());
        return task;
    }

    public @NotNull ScheduledTask schedule(@NotNull Runnable runnable, long l, TimeUnit timeUnit) {
        ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(Core.getInstance(), runnable, l, timeUnit);
        tasks.add(task.getId());
        return task;
    }

    public @NotNull ScheduledTask schedule(@NotNull Runnable runnable, long l, long l1, TimeUnit timeUnit) {
        ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(Core.getInstance(), runnable, l, l1, timeUnit);
        tasks.add(task.getId());
        return task;
    }
}
