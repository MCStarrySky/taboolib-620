package io.izzel.taboolib.module.lite;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.locale.TLocaleLoader;
import io.izzel.taboolib.module.inject.TFunction;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.nms.nbt.NBTCompound;
import io.izzel.taboolib.util.Files;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Optional;

/**
 * @Author 坏黑
 * @Since 2019-05-22 1:16
 */
public class SimpleI18n {

    private static FileConfiguration lang;
    private static boolean released;

    @TFunction.Init
    static void init() {
        File localeFile = getLocaleFile(TabooLib.getPlugin());
        if (localeFile == null) {
            lang = new YamlConfiguration();
        } else {
            lang = Files.load(localeFile);
        }
        if (lang.getInt("version") < 3 && !released) {
            released = true;
            Files.deepDelete(new File(TabooLib.getPlugin().getDataFolder(), "simpleI18n"));
            init();
        }
    }

    public static String getCustomName(Entity entity) {
        return entity != null ? Optional.ofNullable(entity.getCustomName()).orElse(getName(entity)) : getName(entity);
    }

    public static String getCustomName(ItemStack item) {
        if (item != null) {
            ItemMeta itemMeta = item.getItemMeta();
            return itemMeta != null && itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : getName(item);
        }
        return getName(item);
    }

    public static String getName(Entity entity) {
        return entity == null ? "-" : lang.getString(NMS.handle().getName(entity).replace(".", "_"), entity.getName());
    }

    public static String getName(ItemStack item) {
        if (item == null) {
            return "-";
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof BookMeta && ((BookMeta) itemMeta).getTitle() != null) {
            return ((BookMeta) itemMeta).getTitle();
        }
        if (!Version.isAfter(Version.v1_11)) {
            if (item.getType().name().equals("MONSTER_EGG")) {
                NBTCompound nbtCompound = NMS.handle().loadNBT(item);
                if (nbtCompound.containsKey("EntityTag")) {
                    return lang.getString("item_monsterPlacer_name") + " " + lang.getString("entity_" + nbtCompound.get("EntityTag").asCompound().get("id").asString() + "_name");
                }
                return lang.getString("item_monsterPlacer_name");
            }
        } else if (!Version.isAfter(Version.v1_13)) {
            if (itemMeta instanceof SpawnEggMeta) {
                String spawnEggType = lang.getString("entity_" + ((SpawnEggMeta) itemMeta).getSpawnedType().getEntityClass().getSimpleName().replace(".", "_") + "_name");
                if (spawnEggType != null) {
                    return lang.getString(NMS.handle().getName(item).replace(".", "_"), item.getType().name().toLowerCase().replace("_", "")) + " " + spawnEggType;
                }
            }
        }
        return lang.getString(NMS.handle().getName(item).replace(".", "_"), item.getType().name().toLowerCase().replace("_", ""));
    }

    private static File getLocaleFile(Plugin plugin) {
        TLocaleLoader.getLocalePriority(plugin).forEach(localeName -> Files.releaseResource(plugin, "simpleI18n/" + getVersion() + "/" + localeName + ".yml", false));
        return TLocaleLoader.getLocalePriority(plugin).stream().map(localeName -> new File("plugins/TabooLib/simpleI18n/" + getVersion() + "/" + localeName + ".yml")).filter(File::exists).findFirst().orElse(null);
    }

    private static String getVersion() {
        return Version.isAfter(Version.v1_13) ? "high" : "low";
    }
}