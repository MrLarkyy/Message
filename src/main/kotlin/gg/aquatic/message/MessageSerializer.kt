package gg.aquatic.message

import gg.aquatic.message.impl.page.PaginatedMessage
import gg.aquatic.message.impl.SimpleMessage
import gg.aquatic.cosmogenerics.util.message.parser.MessageParser
import org.bukkit.configuration.ConfigurationSection

object MessageSerializer {

    fun loadMessageInstance(cfg: ConfigurationSection, path: String, def: Any): () -> Message {
        val value = cfg.get(path, def)
        if (cfg.isString(path)) {
            return { SimpleMessage(value as String) }
        } else if (cfg.isList(path)) {
            val mapped = (value as List<*>).map { it.toString().toMMComponent() }
            return { SimpleMessage.create(mapped) }
        } else {
            val section = cfg.getConfigurationSection(path)
            if (section == null) {
                if (value is List<*>) {
                    val mapped = value.map { it.toString().toMMComponent() }
                    return { SimpleMessage.create(mapped) }
                }
                return { SimpleMessage(value.toString()) }
            }
            if (section.contains("messages")) {
                val messagesList = section.getList("messages")
                if (messagesList is List<*>) {
                    return MessageParser.parse(section)
                }
            }

            if (section.contains("paginated")) {
                val messageList =
                    section.getList("paginated") ?: emptyList<String>()
                val messages = MessageParser.parse(messageList).map { it.toMMComponent() }
                val pageSize = section.getInt("page-size", 10)
                val header = section.getString("header")
                val footer = section.getString("footer")

                return { PaginatedMessage.create(messages, pageSize, header?.toMMComponent(), footer?.toMMComponent()) }
            } else return { SimpleMessage(emptyList()) }
        }
    }
}