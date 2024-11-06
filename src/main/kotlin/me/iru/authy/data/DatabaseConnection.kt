package me.iru.authy.data

import me.iru.authy.Authy
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.HashSet
import java.util.LinkedList

object DatabaseConnection {
    private val connections = LinkedList<Connection>()
    private val usedConnections = HashSet<Connection>()
    private var maxPoolSize = 10
    private var currentPoolSize = 0
    private var isInitialized = false

    fun init() {
        if(isInitialized) {
            Authy.instance.logger.warning("Database connection already initialized!")
            return
        }

        maxPoolSize = Authy.instance.config.getInt("database.maxPoolSize").coerceAtLeast(10)
        val type = Authy.instance.config.getString("database.type") ?: "sqlite"
        for (i in 0 until maxPoolSize) {
            connections.add(createConnection(type))
            currentPoolSize++
        }

        isInitialized = true

        Authy.instance.logger.info("Connected to database, pool size: $currentPoolSize")
    }

    fun shutdown() {
        if(!isInitialized) {
            Authy.instance.logger.warning("Database connection not initialized!")
            return
        }

        for(connection in connections) {
            connection.close()
        }
        for(connection in usedConnections) {
            connection.close()
        }
        connections.clear()
        usedConnections.clear()
        currentPoolSize = 0

        Authy.instance.logger.info("All database connections closed")
    }

    fun query(query: String, vararg params: Any?): ResultSet? {
        if(!isInitialized) {
            Authy.instance.logger.warning("Database connection not initialized!")
            return null
        }

        val connection = getConnection()
        val statement = connection.prepareStatement(query)
        for(i in params.indices) {
            statement.setObject(i + 1, params[i])
        }
        val result = statement.execute()
        statement.close()
        releaseConnection(connection)

        return if(result) statement.resultSet else null
    }

    private fun connectSQLite(): Connection {
        Class.forName("org.sqlite.JDBC")

        return DriverManager.getConnection("jdbc:sqlite:${Authy.instance.dataFolder}${File.separator}data.db")
    }

    private fun connectMySQL(): Connection {
        val config = Authy.instance.config

        val host = config.getString("database.host") ?: "localhost:3306"
        val database = config.getString("database.database") ?: "authy"
        val user = config.getString("database.user") ?: "root"
        val password = config.getString("database.password") ?: ""

        return DriverManager.getConnection("jdbc:mysql://$host/$database", user, password)
    }

    private fun getConnection(): Connection {
        if(connections.isNotEmpty()) {
            val connection = connections.poll()
            if(connection.isValid(5)) {
                usedConnections.add(connection)
                return connection
            } else {
                currentPoolSize--
            }
        }

        val type = Authy.instance.config.getString("database.type") ?: "sqlite"

        return if(currentPoolSize < maxPoolSize) {
            val connection = createConnection(type)
            usedConnections.add(connection)
            currentPoolSize++
            connection
        } else {
            throw SQLException("Maximum pool size reached, no available connections!")
        }
    }

    private fun releaseConnection(connection: Connection) {
        if(usedConnections.remove(connection)) {
            if(!connection.isClosed && connection.isValid(5)) {
                connections.add(connection)
            } else {
                currentPoolSize--
                connection.close()
            }
        }
    }

    private fun createConnection(type: String): Connection {
        return when (type) {
            "mysql" -> connectMySQL()
            else -> connectSQLite()
        }
    }

}