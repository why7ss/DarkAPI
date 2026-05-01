package ru.civworld.darkAPI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class DarkAPI extends JavaPlugin {
    private static final Map<Plugin, String> pluginPrefixes = new HashMap<>();
    private static final String DEFAULT_PREFIX = "<gray>[<blue>DarkAPI<gray>] <white>";

    @Override
    public void onEnable() {
        log("DarkAPI enabled!");
    }

    public static void registerPlugin(Plugin plugin, String prefix) {
        pluginPrefixes.put(plugin, prefix);
        if(plugin != null) plugin.getLogger().info("Hooking into DarkAPI...");
    }

    private static JavaPlugin getCallingPlugin() {
        try {
            return StackWalker
                    .getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .walk(stream -> stream
                            .map(StackWalker.StackFrame::getDeclaringClass)
                            .filter(c ->
                                    !c.getName().startsWith("ru.civworld.darkAPI") &&
                                            !c.getName().startsWith("org.bukkit") &&
                                            !c.getName().startsWith("io.papermc")
                            )
                            .map(JavaPlugin::getProvidingPlugin)
                            .findFirst()
                            .orElse(null)
                    );
        } catch (Exception e) {
            return null;
        }
    }

    public static Component parse(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        Plugin plugin = getCallingPlugin();
        if (plugin == null) {
            DarkAPI.error("Failed to determine calling plugin for command registration.");
        }
        String prefix = plugin != null ? pluginPrefixes.getOrDefault(plugin, DEFAULT_PREFIX) : DEFAULT_PREFIX;
        return MiniMessage.miniMessage().deserialize("<!i>" + text.replace("<prefix>", prefix));
    }

    public static void broadcast(String text) {
        JavaPlugin plugin = getCallingPlugin();
        if (plugin != null) plugin.getServer().broadcast(parse(text));
    }

    public static void log(String message) {
        JavaPlugin plugin = getCallingPlugin();
        if (plugin != null) plugin.getLogger().info(message);
    }

    public static void error(String message) {
        JavaPlugin plugin = getCallingPlugin();
        if (plugin != null) plugin.getLogger().severe(message);
    }

    public static void setCommand(String command, CommandExecutor executor) {
        try {
            JavaPlugin plugin = getCallingPlugin();

            if (plugin == null) {
                DarkAPI.error("Failed to determine calling plugin for command registration.");
                return;
            }
            var cmd = plugin.getCommand(command);
            if (cmd == null) {
                plugin.getLogger().severe("Command " + command + " not found in plugin.yml");
                return;
            }

            cmd.setExecutor(executor);

        } catch (Exception e) {
            DarkAPI.error("Failed to register command: " + e.getMessage());
        }
    }

    public static void setCommand(String command, CommandExecutor executor, TabCompleter tabCompleter) {
        try {
            JavaPlugin plugin = getCallingPlugin();

            if (plugin == null) {
                DarkAPI.error("Failed to determine calling plugin for command registration.");
                return;
            }
            var cmd = plugin.getCommand(command);
            if (cmd == null) {
                plugin.getLogger().severe("Command " + command + " not found in plugin.yml");
                return;
            }

            cmd.setTabCompleter(tabCompleter);
            cmd.setExecutor(executor);
        } catch (Exception e) {
            DarkAPI.error("Failed to register command: " + e.getMessage());
        }
    }
}