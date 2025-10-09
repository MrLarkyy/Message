package gg.aquatic.cosmogenerics.util.message.parser

import gg.aquatic.message.Message
import gg.aquatic.message.impl.SimpleMessage
import gg.aquatic.message.impl.view.MessageView
import gg.aquatic.message.parser.click.ClickAction
import gg.aquatic.execute.action.ActionSerializer
import gg.aquatic.execute.createConfigurationSectionFromMap
import gg.aquatic.execute.getSectionList
import gg.aquatic.message.toMMComponent
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

object MessageParser {

    fun parse(section: ConfigurationSection): () -> Message {
        val actions = ActionSerializer.fromSections<Player>(section.getSectionList("actions"))
        //val messageSections = section.getSectionList("messages") + section.getSectionList("message")
        val messageList =
            section.getList("messages") ?: (emptyList<Any>() + section.getList("message"))
        val messages = parse(messageList).map { it.toMMComponent() }
        val view = MessageView.load(section)

        //val messages = messageSections.map { parseMessage(it) }.toList()
        return { SimpleMessage.create(messages, actions, view) }
    }

    fun parse(list: List<*>): List<String> {
        val messages = ArrayList<String>()
        for (any in list) {
            if (any is String) {
                messages.add(any)
                continue
            }
            if (any is ConfigurationSection) {
                messages.add(parseMessage(any))
                continue
            } else if (any is Map<*, *>) {
                messages.add(parseMessage(createConfigurationSectionFromMap(any)))
                continue
            }
        }
        return messages
    }

    private fun parseMessage(section: ConfigurationSection): String {
        val componentSections = section.getSectionList("components")
        val components = componentSections.mapNotNull { parseComponent(it) }
        return components.joinToString("<reset>")
    }

    private fun parseComponent(section: ConfigurationSection): String? {
        var text = section.getString("text") ?: return null
        val clickAction = section.getConfigurationSection("click")?.let {
            ClickAction.load(it)
        }
        val hover = section.getStringList("hover")
        text = clickAction?.bind(text) ?: text
        if (hover.isNotEmpty()) {
            text = "<hover:show_text:'${hover.joinToString("<newline>")}'>$text</hover>"
        }
        return text
    }

}