package me.iru.authy

import me.iru.authy.data.DatabaseConnection
import net.md_5.bungee.api.ChatColor
import org.bukkit.plugin.java.JavaPlugin


class Authy : JavaPlugin() {
    val version = this.description.version
    var latestVersion = this.version

    private val pluginName = this.description.name

    private val prefix: String = ChatColor.translateAlternateColorCodes('&', "&8[&6$pluginName&8]&7")

    private var initialized = false

    companion object {
        lateinit var instance: Authy private set
        lateinit var databaseConnection: DatabaseConnection private set
    }

    override fun onEnable() {
        instance = this

        if(server.onlineMode) {
            val message = "$prefix ${ChatColor.RED}Disabling $pluginName... Server is in online mode! Switch to offline mode and restart the server!"
            server.consoleSender.sendMessage(message)
            for(player in server.onlinePlayers) {
                player.sendMessage("")
                player.sendMessage("")
                player.sendMessage(message)
                player.sendMessage("")
                player.sendMessage("")
            }
            server.pluginManager.disablePlugin(this)
            return
        }


        saveDefaultConfig()
        config.options().copyDefaults(true)
        saveConfig()

        databaseConnection = DatabaseConnection()

        initialized = true

        server.consoleSender.sendMessage("$prefix ${ChatColor.GREEN}Enabled $version")
    }

    override fun onDisable() {
        if(initialized) {
            databaseConnection.shutdown()

            initialized = false
        }
        server.consoleSender.sendMessage("$prefix ${ChatColor.RED}Disabled $version")
    }

}