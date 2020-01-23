package io.izzel.taboolib.module.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.Ref;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author sky
 * @Since 2018-09-08 15:00
 */
public class TConfig extends YamlConfiguration {

    private static Map<String, List<File>> files = Maps.newHashMap();
    private File file;
    private List<Runnable> runnable = Lists.newArrayList();

    private TConfig(File file, Plugin plugin) {
        files.computeIfAbsent(plugin.getName(), name -> new ArrayList<>()).add(file);
        this.file = file;
        reload();
        TConfigWatcher.getInst().addSimpleListener(this.file, this::reload);
        TabooLibAPI.debug("Loaded TConfiguration \"" + file.getName() + "\" from Plugin \"" + plugin.getName() + "\"");
    }

    public static Map<String, List<File>> getFiles() {
        return files;
    }

    public static TConfig create(File file) {
        return new TConfig(file, Ref.getCallerPlugin(Ref.getCallerClass(3).orElse(TabooLib.class)));
    }

    public static TConfig create(File file, Plugin plugin) {
        return new TConfig(file, plugin);
    }

    public static TConfig create(Plugin plugin, String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            Files.releaseResource(plugin, path, false);
        }
        return create(file, plugin);
    }

    public String getStringColored(String path) {
        return TLocale.Translate.setColored(getString(path));
    }

    public String getStringColored(String path, String def) {
        return TLocale.Translate.setColored(getString(path, def));
    }

    public List<String> getStringListColored(String path) {
        return TLocale.Translate.setColored(getStringList(path));
    }

    public void release() {
        TConfigWatcher.getInst().removeListener(file);
    }

    public void reload() {
        try {
            load(file);
            runListener();
        } catch (IOException | InvalidConfigurationException e) {
            TLogger.getGlobalLogger().warn("Cannot load configuration from stream: " + e.toString());
        }
    }

    public void saveToFile() {
        try {
            save(file);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public File getFile() {
        return file;
    }

    public Runnable getListener() {
        return runnable.get(0);
    }

    public List<Runnable> getListeners() {
        return runnable;
    }

    public TConfig listener(Runnable runnable) {
        this.runnable.add(runnable);
        return this;
    }

    public void runListener() {
        for (Runnable listener : runnable) {
            try {
                listener.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}