package gg.aquatic.message

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

interface Message {
    val messages: Collection<MessageComponent>

    fun replace(updater: (String) -> String): Message
    fun replace(from: String, to: String): Message

    fun send(sender: CommandSender)

    fun broadcast()

    class MessageComponent(
        val component: Component,
        val hasPlaceholders: Boolean
    )
}