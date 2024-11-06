package me.iru.authy.data

import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

class AuthyPlayer(val uuid: UUID,
                  var username: String,
                  var ip: String,
                  var password: String,
                  var isPinEnabled: Boolean = false,
                  var pin: String? = null,
                  var lastLogin: Long = 0,
                  var x: Double = 0.0,
                  var y: Double = 0.0,
                  var z: Double = 0.0,
                  var yaw: Float = 0.0f,
                  var pitch: Float = 0.0f,
                  var world: String = "world"
) {
    companion object {
        fun from(player: Player): AuthyPlayer? {
            return DatabaseConnection.query(
                "SELECT * FROM players WHERE uuid = ?",
                player.uniqueId.toString()
            )?.use {
                if(it.next()) {
                    AuthyPlayer(
                        UUID.fromString(it.getString("uuid")),
                        it.getString("username"),
                        it.getString("ip"),
                        it.getString("password"),
                        it.getBoolean("isPinEnabled"),
                        it.getString("pin"),
                        it.getLong("lastLogin"),
                        it.getDouble("x"),
                        it.getDouble("y"),
                        it.getDouble("z"),
                        it.getFloat("yaw"),
                        it.getFloat("pitch"),
                        it.getString("world")
                    )
                } else {
                    null
                }
            }
        }

        fun init() {
            DatabaseConnection.query(
                """
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) NOT NULL PRIMARY KEY,
                    username VARCHAR(16) NOT NULL,
                    ip VARCHAR(15) NOT NULL,
                    password VARCHAR(64) NOT NULL,
                    isPinEnabled BOOLEAN NOT NULL DEFAULT FALSE,
                    pin VARCHAR(64),
                    lastLogin BIGINT NOT NULL DEFAULT 0,
                    x DOUBLE NOT NULL DEFAULT 0.0,
                    y DOUBLE NOT NULL DEFAULT 0.0,
                    z DOUBLE NOT NULL DEFAULT 0.0,
                    yaw FLOAT NOT NULL DEFAULT 0.0,
                    pitch FLOAT NOT NULL DEFAULT 0.0,
                    world VARCHAR(64) NOT NULL DEFAULT 'world'
                )
                """
            )
        }
    }

    fun save() {
        DatabaseConnection.query(
            "SELECT * FROM players WHERE uuid = ?",
            uuid.toString()
        )?.use {
            if(it.next()) {
                DatabaseConnection.query(
                    """
                    UPDATE players
                    SET username = ?,
                        ip = ?,
                        password = ?,
                        isPinEnabled = ?,
                        pin = ?,
                        lastLogin = ?,
                        x = ?,
                        y = ?,
                        z = ?,
                        yaw = ?,
                        pitch = ?,
                        world = ?
                    WHERE uuid = ?
                    """,
                    username,
                    ip,
                    password,
                    isPinEnabled,
                    pin,
                    lastLogin,
                    x,
                    y,
                    z,
                    yaw,
                    pitch,
                    world,
                    uuid.toString()
                )
            } else {
                DatabaseConnection.query(
                    """
                    INSERT INTO players (uuid, username, ip, password, isPinEnabled, pin, lastLogin, x, y, z, yaw, pitch, world)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    uuid.toString(),
                    username,
                    ip,
                    password,
                    isPinEnabled,
                    pin,
                    lastLogin,
                    x,
                    y,
                    z,
                    yaw,
                    pitch,
                    world
                )
            }
        }
    }


    fun setLastLocation(location: Location) {
        x = location.x
        y = location.y
        z = location.z
        yaw = location.yaw
        pitch = location.pitch
        world = location.world?.name ?: "world"
    }

}