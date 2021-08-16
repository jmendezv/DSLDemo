package cat.copernic.mendez.dsl

import java.time.LocalDateTime

enum class TalkType {
    CONFERENCE, MASTERCLASS
}

data class Talk(
    val topic: String,
    val speaker: String,
    val time: LocalDateTime,
    val type: TalkType
)

class Conference(val name: String, val venue: String) {
    private val schedule = mutableListOf<Talk>()
    val talks
        get() = schedule.toList()

    fun addTalk(talk: Talk) {
        schedule.add(talk)
    }



}

/** DSL **/

inline fun conference(config: ConferenceConfig.() -> Unit): Conference {

    val conferenceConfig: ConferenceConfig =
        ConferenceConfig().apply(config)

    return Conference(conferenceConfig.name, conferenceConfig.venue)
        .apply {
            conferenceConfig.talkList.forEach(::addTalk)
        }
}

@ConfDSLMarker
class ConferenceConfig {

    private val _talkList = mutableListOf<Talk>()
    val talkList
        get() = _talkList.toList()

    lateinit var name: String
    lateinit var venue: String

    val talkConfig = TalkConfig()

    @ConfDSLMarker
    inner class TalkConfig {
        private val _talkList = this@ConferenceConfig._talkList

        operator fun invoke(config: TalkConfig.() -> Unit) {
            apply(config)
        }

        fun conferenceTalk(topic: String, speaker: String, time: LocalDateTime): Unit {
            _talkList.add(Talk(topic, speaker, time, TalkType.CONFERENCE))
        }

        fun masterclassTalk(topic: String, speaker: String, time: LocalDateTime): Unit {
            _talkList.add(Talk(topic, speaker, time, TalkType.MASTERCLASS))
        }

        val conferenceTalk: EmptyTalk
            get() = EmptyTalk(TalkType.CONFERENCE)

        val masterclassTalk: EmptyTalk
            get() = EmptyTalk(TalkType.MASTERCLASS)

        inner class EmptyTalk(val type: TalkType) {
            infix fun at(timeString: String) = TimedTalk(this, LocalDateTime.parse(timeString))
        }

        inner class TimedTalk(
            val previous: EmptyTalk,
            val time: LocalDateTime
        ) {
            infix fun by(speaker: String) = TimedAndAuthorTalk(this, speaker)
        }

        inner class TimedAndAuthorTalk(
            private val previous: TimedTalk,
            private val speaker: String
        ) {
            infix fun title(topic: String) =
                _talkList.add(Talk(topic, speaker, previous.time, previous.previous.type))
        }

        operator fun Talk.unaryPlus() = _talkList.add(this)
    }
}

/** DSL ENDS **/

fun main() {
    // The Java way
    val conference1 = Conference("Kotlin DSL", "Terrassa")
    val talk1 = Talk("Programmimg", "Pep", LocalDateTime.now(), TalkType.MASTERCLASS)
    val talk2 = Talk("DB", "Pep", LocalDateTime.now(), TalkType.CONFERENCE)
    conference1.addTalk(talk1)
    conference1.addTalk(talk2)
    println(conference1)
    // The Kotlin way
    val conference2 = Conference("Kotlin DSL", "Terrassa").apply {
        addTalk(Talk("Programmimg", "Pep", LocalDateTime.now(), TalkType.MASTERCLASS))
        addTalk(Talk("DB", "Pep", LocalDateTime.now(), TalkType.CONFERENCE))
    }
    println(conference2)
    // Advanced Kotlin: DSL way
    val conference3 = conference {
        name = "Insight"
        venue = "Terrassa"
        talkConfig {
            conferenceTalk("Programming", "Pep", LocalDateTime.now())
            masterclassTalk at "2024-03-11T09:00" by "Pep" title "It's over my friend"
            +Talk("Android", "Pep", LocalDateTime.now(), TalkType.CONFERENCE)
        }
        talkConfig.conferenceTalk("Programming II", "Pep", LocalDateTime.now())
    }
    println(conference3)
}