package gg.aquatic.message.impl

import gg.aquatic.message.Message
import gg.aquatic.message.impl.view.MessageView
import gg.aquatic.execute.action.ActionHandle
import gg.aquatic.message.containsPlaceholder
import gg.aquatic.message.replacePlaceholders
import gg.aquatic.message.toMMComponent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SimpleMessage(
    override val messages: Collection<Message.MessageComponent>,
    val actions: Collection<ActionHandle<Player>> = emptyList(),
    val view: MessageView = MessageView.Chat
) : Message {

    companion object {
        fun create(messages: Collection<Component>,
                   actions: Collection<ActionHandle<Player>> = emptyList(),
                   view: MessageView = MessageView.Chat): SimpleMessage {
            return SimpleMessage(messages.map { Message.MessageComponent(it, it.containsPlaceholder()) }, actions, view)
        }
    }

    constructor(message: String?) : this(message?.let {
        val component = it.toMMComponent()
        val hasPlaceholder = component.containsPlaceholder()
        mutableListOf(Message.MessageComponent(component, hasPlaceholder))
    } ?: mutableListOf())

    override fun replace(updater: (String) -> String): SimpleMessage {
        val mapped = messages.map {
            if (it.hasPlaceholders) Message.MessageComponent(
                it.component.replacePlaceholders(updater),
                true
            ) else it
        }
        return SimpleMessage(mapped, actions, view)
    }

    override fun replace(from: String, to: String): SimpleMessage {
        val mapped = messages.map {
            if (it.hasPlaceholders) Message.MessageComponent(
                it.component.replacePlaceholders { str -> str.replace(from, to) },
                true
            ) else it
        }
        return SimpleMessage(mapped, actions, view)
    }

    override fun send(sender: CommandSender) {
        view.send(sender, messages.map { it.component })
        if (sender is Player) {
            for (handle in actions) {
                handle.execute(sender) { _, str -> str}
            }
        }
    }

    override fun broadcast() {
        if (messages.size == 1 && messages.first().component == Component.empty() || messages.isEmpty()) {
            return
        }
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            view.send(onlinePlayer, messages.map { it.component })
            for (handle in actions) {
                handle.execute(onlinePlayer) { _, str -> str}
            }
        }
    }
}