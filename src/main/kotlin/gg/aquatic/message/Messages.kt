package gg.aquatic.message

import gg.aquatic.message.handler.MessageHandler
import java.util.concurrent.ConcurrentHashMap

object Messages {

    val registeredMessages = ConcurrentHashMap<String, () -> Message>()

    inline fun <reified T> injectMessages(namespace: String) where T : MessageHandler, T : Enum<T> {
        for (t in enumValues<T>()) {
            registeredMessages["$namespace:${t.name.lowercase()}"] = { t.message }
        }
    }

    operator fun get(key: String): Message? {
        return registeredMessages[key]?.invoke()
    }
}
