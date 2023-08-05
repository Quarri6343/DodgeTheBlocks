package com.quarri6343.dodgetheblocks;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * コンフィグファイルを読み書きする
 */
public class Config {
    private static final String generatorPos1Str = "generatorPos1";
    private static final String generatorPos2Str = "generatorPos2";
    private static final String generatorDirectionStr = "generatorDirection";

    private static final String frequencyStr = "frequency";
    private static final String doShrinkPlatformStr = "doShrinkPlatform";
    private static final String platformPos1Str = "platformPos1";
    private static final String platformPos2Str = "platformPos2";

    /**
     * コンフィグファイル内のデータをデータクラスにコピーする
     */
    public void loadConfig() {
        JavaPlugin plugin = DodgeTheBlocks.instance;
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        DodgeTheBlocks.generatorPos1 = config.getLocation(generatorPos1Str);
        DodgeTheBlocks.generatorPos2 = config.getLocation(generatorPos2Str);
        DodgeTheBlocks.generatorDirection = config.getLocation(generatorDirectionStr);
        DodgeTheBlocks.frequency = config.getInt(frequencyStr);
        DodgeTheBlocks.doShrinkPlatform = config.getBoolean(doShrinkPlatformStr);
        DodgeTheBlocks.platformPos1 = config.getLocation(platformPos1Str);
        DodgeTheBlocks.platformPos2 = config.getLocation(platformPos2Str);
    }

    /**
     * データクラスの中身をコンフィグにセーブする
     */
    public void saveConfig() {
        resetConfig();//古いデータが混在しないように一旦コンフィグを消す

        JavaPlugin plugin = DodgeTheBlocks.instance;
        FileConfiguration config = plugin.getConfig();

        config.set(generatorPos1Str, DodgeTheBlocks.generatorPos1);
        config.set(generatorPos2Str, DodgeTheBlocks.generatorPos2);
        config.set(generatorDirectionStr, DodgeTheBlocks.generatorDirection);
        config.set(frequencyStr, DodgeTheBlocks.frequency);
        config.set(doShrinkPlatformStr, DodgeTheBlocks.doShrinkPlatform);
        config.set(platformPos1Str, DodgeTheBlocks.platformPos1);
        config.set(platformPos2Str, DodgeTheBlocks.platformPos2);

        plugin.saveConfig();
    }

    /**
     * コンフィグを全て削除する
     */
    public void resetConfig() {
        JavaPlugin plugin = DodgeTheBlocks.instance;
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (configFile.delete()) {
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
        }
    }
}
