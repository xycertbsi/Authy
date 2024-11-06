package me.iru.authy

import net.md_5.bungee.api.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Localization {

    enum class PrefixType {
        WARNING,
        ERROR,
        LOGIN,
        PIN,
        REGISTER,
        REMEMBER,
        UNREGISTER
    }

    enum class ParseMode {
        ResetAndTranslate,
        Translate,
        None
    }

    const val VERSION = 10
    private val langFolder = File(Authy.instance.dataFolder, "lang${File.separator}")
    private val defaultsFolder = File(langFolder, "defaults${File.separator}")
    private val locales = arrayOf(
        "en_us", "cs_cz",
        "es_es", "pl_pl",
        "ru_ru", "tr_tr",
        "zh_tw", "zh_cn",
        "pt_br", "ua_uk",
        "sk_sk"
    )

    private var isInitialized = false
    private val loadedTranslations = HashMap<String, String>()

    fun get(key: String, mode: ParseMode = ParseMode.ResetAndTranslate): String {
        if(!isInitialized) {
            Authy.instance.logger.warning("Localization not initialized!")
            return key
        }
        val value = loadedTranslations[key] ?: getFallback(key)
        return when(mode) {
            ParseMode.ResetAndTranslate -> ChatColor.translateAlternateColorCodes('&', "&r$value")
            ParseMode.Translate ->  ChatColor.translateAlternateColorCodes('&', value)
            ParseMode.None -> value
        }
    }

    fun getFallback(key: String): String {
        val locale = getSelectedLocale()
        Authy.instance.server.consoleSender.sendMessage("${Authy.instance.prefix} ${ChatColor.RED}" +
                "Missing translation for key ${ChatColor.GOLD}$key ${ChatColor.RED}in locale ${ChatColor.GOLD}$locale${ChatColor.RED}!")
        val file = localeFile("en_us")
        val config = YamlConfiguration.loadConfiguration(file)
        val fallbackValue = config.getString(key)
        if(fallbackValue == null) {
            Authy.instance.server.consoleSender.sendMessage("${Authy.instance.prefix} ${ChatColor.RED}" +
                    "Missing fallback for key ${ChatColor.GOLD}$key ${ChatColor.RED}in locale ${ChatColor.GOLD}en_us${ChatColor.RED}! " +
                    "Please update file ${ChatColor.GOLD}plugins/Authy/lang/en_us.yml${ChatColor.RED}!")
            return ChatColor.translateAlternateColorCodes('&', "&cTranslation error, please contact an administrator!")
        }
        return fallbackValue
    }

    fun init() {
        if (isInitialized) {
            Authy.instance.logger.warning("Localization already initialized!")
            return
        }

        updateDefaults()

        val locale = getSelectedLocale()
        load(locale)

        isInitialized = true

        Authy.instance.logger.info("Loaded locale: $locale, version: ${get("version", ParseMode.None)}")
    }

    private fun updateDefaults() {
        defaultsFolder.mkdirs()
        for(locale in locales) {
            val file = localeFile(locale)
            if(!file.exists()) {
                Authy.instance.saveResource("lang${File.separator}$locale.yml", false)
            }
            Authy.instance.saveResource("lang${File.separator}defaults${File.separator}$locale.yml", true)
        }
    }

    private fun load(locale: String) {
        var file = localeFile(locale)
        if(!file.exists()) {
            Authy.instance.server.consoleSender.sendMessage("${Authy.instance.prefix} ${ChatColor.RED}" +
                    "No locale file for $locale found! Saving default...\")")
            updateDefaults()
        }
        if(hasOldVersion(locale)) {
            Authy.instance.server.consoleSender.sendMessage("${Authy.instance.prefix} ${ChatColor.RED}" +
                    "Locale file for $locale is outdated! Loading default...\")")
            file = localeFile(locale, true)
            if(!file.exists()) {
                Authy.instance.server.consoleSender.sendMessage("${Authy.instance.prefix} ${ChatColor.RED}" +
                        "No default locale file for $locale found! Using en_us...\")")
                file = localeFile("en_us", true)
            }
        }
        loadedTranslations.clear()
        val config = YamlConfiguration.loadConfiguration(file)
        for(key in config.getKeys(false)) {
            loadedTranslations[key] = config.getString(key) ?: ""
        }
    }

    private fun localeFile(locale: String, default: Boolean = false): File {
        return File(if(default) defaultsFolder else langFolder, "$locale.yml")
    }

    private fun getSelectedLocale(): String {
        return Authy.instance.config.getString("lang") ?: "en_us"
    }

    private fun hasOldVersion(locale: String): Boolean {
        val file = localeFile(locale)
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getInt("version", 0) < VERSION
    }
}