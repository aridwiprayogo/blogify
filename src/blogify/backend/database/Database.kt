package blogify.backend.database

import blogify.backend.config.Configs
import blogify.backend.util.BException

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import org.jetbrains.exposed.sql.Database

/**
 * Meta object regrouping setup and utility functions for PostgreSQL.
 */
object Database {

    lateinit var instance: Database

    private val config = Configs.Database

    private fun configureHikariCP(envDbHost: String, envDbPort: Int, envDbUser: String, envDbPass: String): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName        = "org.postgresql.Driver"
        config.jdbcUrl                = "jdbc:postgresql://$envDbHost:$envDbPort/postgres"
        config.maximumPoolSize        = 24
        config.minimumIdle            = 6
        config.validationTimeout      = 10 * 1000
        config.connectionTimeout      = 10 * 1000
        config.maxLifetime            = 30 * 60 * 1000
        config.leakDetectionThreshold = 60 * 1000
        config.isAutoCommit           = false
        config.transactionIsolation   = "TRANSACTION_REPEATABLE_READ"
        config.username               = envDbUser
        config.password               = envDbPass
        config.validate()
        return HikariDataSource(config)
    }

    fun init() {
        instance = Database.connect(configureHikariCP(config.host, config.port, config.username, config.password))
    }

    open class Exception(causedBy: kotlin.Exception) : BException(causedBy) {

        class NotFound(causedBy: BException) : Exception(causedBy)

        class MultipleFound(causedBy: BException) : Exception(causedBy)

    }

}