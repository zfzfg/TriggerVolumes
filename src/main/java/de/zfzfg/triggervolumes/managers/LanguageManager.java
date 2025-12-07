package de.zfzfg.triggervolumes.managers;

import de.zfzfg.triggervolumes.TriggerVolumesPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages language files and translations for the plugin.
 * Supports multiple languages with fallback to English.
 * 
 * @author zfzfg
 */
public class LanguageManager {

    private final TriggerVolumesPlugin plugin;
    private final Map<String, FileConfiguration> languages;
    private String currentLanguage;
    private FileConfiguration currentConfig;

    /**
     * Creates a new LanguageManager.
     * 
     * @param plugin The plugin instance
     */
    public LanguageManager(TriggerVolumesPlugin plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
        this.currentLanguage = "en";
    }

    /**
     * Loads all language files and sets the current language.
     */
    public void load() {
        // Create lang directory if it doesn't exist
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        // Load all available languages
        String[] availableLanguages = {"en", "de", "fr", "pl", "ru", "ja", "es"};
        
        for (String lang : availableLanguages) {
            String fileName = lang + ".yml";
            File langFile = new File(langDir, fileName);
            
            // Save default language file if it doesn't exist
            if (!langFile.exists()) {
                plugin.saveResource("lang/" + fileName, false);
            }
            
            // Load language file
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
                
                // Load defaults from resource
                InputStream defConfigStream = plugin.getResource("lang/" + fileName);
                if (defConfigStream != null) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
                    config.setDefaults(defConfig);
                }
                
                languages.put(lang, config);
                plugin.getLogger().info("Loaded language: " + lang);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load language file: " + fileName);
            }
        }

        // Set current language from config
        String configLang = plugin.getConfig().getString("language.default", "en");
        setLanguage(configLang);
    }

    /**
     * Sets the current language.
     * 
     * @param language The language code (e.g., "en", "de")
     * @return True if the language was set successfully
     */
    public boolean setLanguage(String language) {
        if (languages.containsKey(language.toLowerCase())) {
            this.currentLanguage = language.toLowerCase();
            this.currentConfig = languages.get(this.currentLanguage);
            plugin.getLogger().info("Language set to: " + this.currentLanguage);
            return true;
        } else {
            plugin.getLogger().warning("Language not found: " + language + ", using English");
            this.currentLanguage = "en";
            this.currentConfig = languages.get("en");
            return false;
        }
    }

    /**
     * Gets a message from the current language file.
     * 
     * @param key The message key
     * @return The translated message with color codes
     */
    public String getMessage(String key) {
        if (currentConfig == null) {
            return key;
        }

        String message = currentConfig.getString(key);
        
        // Fallback to English if message not found
        if (message == null && !currentLanguage.equals("en")) {
            FileConfiguration enConfig = languages.get("en");
            if (enConfig != null) {
                message = enConfig.getString(key);
            }
        }
        
        // Final fallback
        if (message == null) {
            return key;
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Gets a message with the prefix prepended.
     * 
     * @param key The message key
     * @return The translated message with prefix
     */
    public String getMessageWithPrefix(String key) {
        String prefix = getMessage("prefix");
        String message = getMessage(key);
        return prefix + message;
    }

    /**
     * Gets a message and replaces placeholders.
     * 
     * @param key The message key
     * @param replacements Pairs of placeholder and replacement (placeholder1, value1, placeholder2, value2, ...)
     * @return The translated message with replacements
     */
    public String getMessage(String key, String... replacements) {
        String message = getMessage(key);
        
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String placeholder = replacements[i];
            String value = replacements[i + 1];
            message = message.replace(placeholder, value);
        }
        
        return message;
    }

    /**
     * Gets a message with prefix and replaces placeholders.
     * 
     * @param key The message key
     * @param replacements Pairs of placeholder and replacement
     * @return The translated message with prefix and replacements
     */
    public String getMessageWithPrefix(String key, String... replacements) {
        String prefix = getMessage("prefix");
        String message = getMessage(key, replacements);
        return prefix + message;
    }

    /**
     * Gets the current language code.
     * 
     * @return The current language code
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Reloads all language files.
     */
    public void reload() {
        languages.clear();
        load();
    }

    /**
     * Gets all available language codes.
     * 
     * @return Array of available language codes
     */
    public String[] getAvailableLanguages() {
        return languages.keySet().toArray(new String[0]);
    }
}
