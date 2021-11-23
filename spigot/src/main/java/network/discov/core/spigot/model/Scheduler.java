package network.discov.core.spigot.model;

import network.discov.core.spigot.Core;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    List<Integer> tasks = new ArrayList<>();
    
    public void cancelTask(int i) {
        Bukkit.getScheduler().cancelTask(i);
    }
    
    public void cancelTasks() {
        for (int taskId : tasks) {
            cancelTask(taskId);
        }
    }
    
    public @NotNull BukkitTask runTask(@NotNull Runnable runnable) {
        BukkitTask task = Bukkit.getScheduler().runTask(Core.getInstance(), runnable);
        tasks.add(task.getTaskId());
        return task;
    }
    
    public @NotNull BukkitTask runTaskAsynchronously(@NotNull Runnable runnable) {
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), runnable);
        tasks.add(task.getTaskId());
        return task;
    }
    
    public @NotNull BukkitTask runTaskLater(@NotNull Runnable runnable, long l) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(Core.getInstance(), runnable, l);
        tasks.add(task.getTaskId());
        return task;
    }
    
    public @NotNull BukkitTask runTaskLaterAsynchronously(@NotNull Runnable runnable, long l) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(Core.getInstance(), runnable, l);
        tasks.add(task.getTaskId());
        return task;
    }
    
    public @NotNull BukkitTask runTaskTimer(@NotNull Runnable runnable, long l, long l1) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Core.getInstance(), runnable, l, l1);
        tasks.add(task.getTaskId());
        return task;
    }
    
    public @NotNull BukkitTask runTaskTimerAsynchronously(@NotNull Runnable runnable, long l, long l1) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getInstance(), runnable, l, l1);
        tasks.add(task.getTaskId());
        return task;
    }
}
