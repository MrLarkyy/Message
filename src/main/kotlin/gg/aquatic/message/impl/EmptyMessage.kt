package gg.aquatic.message.impl

import gg.aquatic.message.Message
import org.bukkit.command.CommandSender

object EmptyMessage: Message {
    override val messages: Collection<Message.MessageComponent> = emptyList()

    override fun replace(updater: (String) -> String): Message {
        return this
    }

    override fun replace(from: String, to: String): Message {
        return this
    }

    override fun send(sender: CommandSender) {
        return
    }

    override fun broadcast() {
        return
    }
}