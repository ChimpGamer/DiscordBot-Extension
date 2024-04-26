package nl.chimpgamer.networkmanager.extensions.discordbot.shared

import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin
import nl.chimpgamer.networkmanager.api.Scheduler
import nl.chimpgamer.networkmanager.api.event.EventBus
import nl.chimpgamer.networkmanager.api.manager.NMCloudCommandManager
import java.io.File
import java.io.InputStream
import java.util.function.Supplier
import java.util.logging.Logger

interface Platform {
    val dataFolder: File

    val networkManager: NetworkManagerPlugin

    val scheduler: Scheduler

    val eventBus: EventBus

    val cloudCommandManager: NMCloudCommandManager

    val logger: Logger

    fun getResource(filename: String?): InputStream?

    fun info(message: String)

    fun warn(message: String)

    fun error(message: String)

    fun debug(message: Any?)

    fun debug(message: Supplier<Any?>)

    fun disable()
}