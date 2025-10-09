package gg.aquatic.message.handler

import gg.aquatic.message.Message
import org.bukkit.command.CommandSender

interface MessageHandler {

    val message: Message

    fun send(sender: CommandSender) {
        message.send(sender)
    }
}