package me.iru.authy

import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


object AuthManager {
    private val authedPlayers = ConcurrentHashMap<UUID, Player>()

    fun isAuthed(player: Player): Boolean {
        return authedPlayers.containsKey(player.uniqueId)
    }

    fun addPlayer(player: Player) {
        authedPlayers[player.uniqueId] = player
    }

    fun removePlayer(player: Player) {
        authedPlayers.remove(player.uniqueId)
    }
}