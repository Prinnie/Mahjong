package pl.krakow.riichi.chombot.commands

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class AkagiInflationRate : Command {
    companion object {
        const val MIN_VALUE: Long = 1000L
        const val MAX_VALUE: Long = 99_999_999_999L
    }

    private fun findNumber(message: String): Long? {
        return message.split(Regex("\\W+"))
            .mapNotNull { word -> word.toLongOrNull() }
            .find { number -> number in MIN_VALUE..MAX_VALUE }
    }

    private fun roundDiv(p: Long, q: Long): Long {
        val div: Long = p / q
        val mod: Long = p % q
        if (2 * mod >= q)
            return div + 1
        return div
    }

    private fun formatNumber(number: Long): String {
        if (number >= 1_000_000_000)
            return "${roundDiv(number, 1_000_000_000)} billion"
        if (number >= 1_000_000)
            return "${roundDiv(number, 1_000_000)} million"
        return "${roundDiv(number, 1_000)} thousand"
    }

    override fun execute(event: MessageCreateEvent): Mono<Void> {
        val number: Long = findNumber(event.message.content.get())!!
        return event.message.channel.flatMap { channel ->
            channel.createMessage("This ${formatNumber(number)} in 1965 would equate to " +
                                  "${formatNumber(number * 10)} today.")
        }.then()
    }

    override fun isApplicable(event: MessageCreateEvent, commandName: String): Boolean {
        if (!event.message.content.isPresent)
            return false
        return findNumber(event.message.content.get()) != null
    }
}