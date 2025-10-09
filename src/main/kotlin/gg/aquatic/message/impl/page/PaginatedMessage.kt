package gg.aquatic.message.impl.page

import gg.aquatic.message.Message
import gg.aquatic.message.containsPlaceholder
import gg.aquatic.message.replacePlaceholders
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class PaginatedMessage(
    override var messages: Collection<Message.MessageComponent>,
    val pageSize: Int = 10,
    val header: Component? = null,
    val footer: Component? = null,
) : Message {

    companion object {
        fun create(
            messages: Collection<Component>,
            pageSize: Int = 10,
            header: Component? = null,
            footer: Component? = null
        ): PaginatedMessage {
            return PaginatedMessage(
                messages.map { Message.MessageComponent(it, it.containsPlaceholder()) },
                pageSize,
                header,
                footer
            )
        }
    }

    override fun replace(updater: (String) -> String): PaginatedMessage {
        val mapped = messages.map {
            if (it.hasPlaceholders) Message.MessageComponent(
                it.component.replacePlaceholders(updater),
                true
            ) else it
        }
        return PaginatedMessage(mapped, pageSize, header, footer)
    }

    override fun replace(from: String, to: String): PaginatedMessage {
        val mapped = messages.map {
            if (it.hasPlaceholders) Message.MessageComponent(
                it.component.replacePlaceholders { str -> str.replace(from, to) },
                true
            ) else it
        }
        return PaginatedMessage(mapped, pageSize, header, footer)
    }

    fun send(sender: CommandSender, page: Int) {
        if (messages.isEmpty()) {
            return
        }
        if (messages.size == 1 && messages.first().component == Component.empty()) {
            return
        }
        val startIndex = page * pageSize
        val endIndex = startIndex + pageSize

        if (startIndex >= messages.size) {
            return
        }

        var first: Component? = null
        if (header != null) {
            first = header
        }
        for (i in startIndex until endIndex) {
            if (i >= messages.size) {
                break
            }
            val comp = messages.elementAt(i)
            val component = if (comp.hasPlaceholders) comp.component.update(page, sender) else comp.component
            if (first == null) {
                first = component
                continue
            } else {
                first = first.appendNewline().append(component)
            }
        }
        if (first == null) return

        if (footer != null) {
            first = first.appendNewline().append(footer.update(page, sender))
        }
        sender.sendMessage(first)
    }

    override fun send(sender: CommandSender) {
        send(sender, 0)
    }

    override fun broadcast() {
        broadcast(0)
    }

    fun broadcast(page: Int) {
        if (messages.size == 1 && messages.first().component == Component.empty() || messages.isEmpty()) {
            return
        }
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            send(onlinePlayer, page)
        }
    }

    private fun Component.update(page: Int, sender: CommandSender): Component {
        return this.replacePlaceholders { str ->
            str.replace("%aq-player%", if (sender is Player) sender.name else "*console")
                .replace("%aq-page%", page.toString())
                .replace("%aq-prev-page%", max((page - 1), 0).toString())
                .replace(
                    "%aq-next-page%",
                    min((ceil(messages.size.toDouble() / pageSize.toDouble()).toInt() - 1), page + 1).toString()
                )
        }
    }
}