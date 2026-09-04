package me.uuun.darkAPI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DarkAPI extends JavaPlugin {
    private static final Map<Plugin, String> pluginPrefixes = new HashMap<>();
    private static final Map<Plugin, Map<String, String>> pluginParses = new HashMap<>();

    private static final String DEFAULT_PREFIX =
            "<gray>[<blue>DarkAPI<gray>] <white>";

    @Override
    public void onEnable() {
        log("DarkAPI enabled!");
    }

    public static void registerPlugin(Plugin plugin, String prefix) {
        pluginPrefixes.put(plugin, prefix);
        pluginParses.putIfAbsent(plugin, new HashMap<>());

        if (plugin != null) {
            plugin.getLogger().info("Hooking into DarkAPI...");
            plugin.getLogger().info("Successfully hooked!");
        }
    }

    private static boolean hasWarnedHideTooltip = false;
    private static boolean hasWarnedItemModel = false;

    public static void createDisplayModel(Inventory inv, int slot, String model, Material material, String displayName, String... lore){
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if(displayName != null){
                meta.displayName(parse(displayName));
            }
            try {
                org.bukkit.NamespacedKey namespacedKey = NamespacedKey.fromString(model);
                var method = meta.getClass().getMethod("setItemModel", org.bukkit.NamespacedKey.class);
                method.invoke(meta, namespacedKey);
            } catch (NoSuchMethodException e) {
                if (!hasWarnedItemModel) {
                    error("ItemMeta#setItemModel is not available on this server version (<1.21.4).");
                    hasWarnedItemModel = true;
                }
            } catch (Exception e) {
                if (!hasWarnedItemModel) {
                    error("Failed to invoke setItemModel via reflection: " + e.getMessage());
                    hasWarnedItemModel = true;
                }
            }

            if (lore != null && lore.length > 0) {
                List<Component> loreList = new ArrayList<>(lore.length);
                for (String line : lore) {
                    loreList.add(parse(line));
                }
                meta.lore(loreList);
            }
            item.setItemMeta(meta);
        }

        inv.setItem(slot, item);
    }

    public static void createDisplay(Inventory inv, int slot, Material material, String displayName, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (displayName != null) {
                meta.displayName(parse(displayName));
            }

            if (lore != null && lore.length > 0) {
                List<Component> loreList = new ArrayList<>(lore.length);
                for (String line : lore) {
                    loreList.add(parse(line));
                }
                meta.lore(loreList);
            }

            item.setItemMeta(meta);
        }

        inv.setItem(slot, item);
    }

    public static void createDisplayBackground(Inventory inv, int slot, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            try {
                var method = meta.getClass().getMethod("setHideTooltip", boolean.class);
                method.invoke(meta, true);
            } catch (NoSuchMethodException e) {
                if (!hasWarnedHideTooltip) {
                    error("ItemMeta#setHideTooltip(boolean) is not available on this server version (<1.21).");
                    hasWarnedHideTooltip = true;
                }
            } catch (Exception e) {
                if (!hasWarnedHideTooltip) {
                    error("Failed to invoke setHideTooltip via reflection: " + e.getMessage());
                    hasWarnedHideTooltip = true;
                }
            }
            item.setItemMeta(meta);
        }

        inv.setItem(slot, item);
    }

    public static void setParse(String key, String text) {
        Plugin plugin = getCallingPlugin();
        if (plugin == null) return;

        pluginParses
                .computeIfAbsent(plugin, p -> new HashMap<>())
                .put(key, text);
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
            DarkAPI.error("Failed to determine calling plugin for parse().");
        }

        String prefix = plugin != null
                ? pluginPrefixes.getOrDefault(plugin, DEFAULT_PREFIX)
                : DEFAULT_PREFIX;

        Map<String, String> parses = plugin != null
                ? pluginParses.getOrDefault(plugin, Map.of())
                : Map.of();

        String result = text;

        for (Map.Entry<String, String> entry : parses.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        result = result.replace("<prefix>", prefix);

        return MiniMessage.miniMessage().deserialize("<!i>" + result);
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

    public static void setCommand(String command,
                                  CommandExecutor executor,
                                  TabCompleter tabCompleter) {
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