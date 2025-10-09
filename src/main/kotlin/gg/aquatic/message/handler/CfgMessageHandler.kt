package gg.aquatic.message.handler

import gg.aquatic.message.Message
import gg.aquatic.message.MessageSerializer
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration

interface CfgMessageHandler {
    val path: String
    val def: Any

    val config: FileConfiguration

    val message: Message

    fun createMessage(): () -> Message {
        return MessageSerializer.loadMessageInstance(config, path, def)
    }

    fun send(sender: CommandSender) {
        message.send(sender)
    }
}