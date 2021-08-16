package cat.copernic.mendez.dsl

import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.random.nextULong

enum class TipusEvent {
    TALLER, XERRADA
}

data class Event(
    val id: ULong = Random.nextULong(),
    val nom: String,
    val autor: String,
    val aula: UInt,
    val data: LocalDateTime = LocalDateTime.now(),
    val tipus: TipusEvent = TipusEvent.XERRADA
)

class Conferencia(val nom: String, val localitat: String) {
    private val _conferencies = mutableListOf<Event>()
    val conferencies
        get() = _conferencies.toList()

    fun alta(event: Event) = _conferencies.add(event)

    override fun toString(): String {
        val buffer = StringBuilder()
        with(buffer) {
            append("Nom: '$nom' Localitat: '$localitat' ")
            conferencies.forEach {
                append(it.toString())
            }
        }
        return buffer.toString()
    }
}


// ***** DSL *****

inline fun conferencia(configuracioDeConferencia: ConfiguracioDeConferencia.() -> Unit): Conferencia {

    val configuracioDeConferencia: ConfiguracioDeConferencia =
        ConfiguracioDeConferencia().apply(configuracioDeConferencia)

    return Conferencia(
        configuracioDeConferencia.nom,
        configuracioDeConferencia.localitat
    ).apply {
        configuracioDeConferencia.events.forEach(::alta)
    }
}

class ConfiguracioDeConferencia {

    private val _events = mutableListOf<Event>()
    val events
        get() = _events

    lateinit var nom: String
    lateinit var localitat: String

    val configuracioDeEvents = ConfiguracioDeEvent()

    inner class ConfiguracioDeEvent {

        private val _events = this@ConfiguracioDeConferencia._events

        operator fun invoke(configuracioDeEvent: ConfiguracioDeEvent.() -> Unit) {
            apply(configuracioDeEvent)
        }

        fun nouEventXerrada(nom: String, autor: String, aula: UInt, data: LocalDateTime = LocalDateTime.now()): Unit {
            _events.add(Event(nom = nom, autor = autor, aula = aula, data = data))
        }

        fun nouEventTaller(nom: String, autor: String, aula: UInt, data: LocalDateTime = LocalDateTime.now()): Unit {
            _events.add(Event(nom = nom, autor = autor, aula = aula, data = data, tipus = TipusEvent.TALLER))
        }

        val nouEventXerrada: EventPerEmplenar
            get() = EventPerEmplenar(TipusEvent.XERRADA)

        val nouEventTaller: EventPerEmplenar
            get() = EventPerEmplenar(TipusEvent.TALLER)

        inner class EventPerEmplenar(val tipus: TipusEvent) {
            infix fun nom(nom: String) = EventAmbNom(this, nom)
        }

        inner class EventAmbNom(val eventPrevi: EventPerEmplenar, val nom: String) {
            infix fun autor(autor: String) = EventAmbNomAutor(this, autor)
        }

        inner class EventAmbNomAutor(val eventPrevi: EventAmbNom, val autor: String) {
            infix fun aula(aula: UInt) = EventAmbNomAutorAula(this, aula)
        }

        inner class EventAmbNomAutorAula(val eventPrevi: EventAmbNomAutor, val aula: UInt) {
            infix fun data(data: String) {
                _events.add(
                    Event(nom = eventPrevi.eventPrevi.nom,
                        autor = eventPrevi.autor,
                        aula = aula,
                        data = LocalDateTime.parse(data))
                )
            }
        }
        operator fun Event.unaryPlus() = _events.add(this)
    }
}

// ***** DSL END *****

// TODO("freeDatingService has clients looking for a partner")

fun main() {
    val tecno = conferencia {
        nom = "Tecnologia i programacio"
        localitat = "Barcelona"
        configuracioDeEvents {
            // Crida a mètode
            nouEventXerrada("Intro Kotlin", "Pere", 5U, LocalDateTime.parse("2024-03-11T09:00"))
            nouEventTaller("Android", "Ana", 3U, LocalDateTime.parse("2024-03-10T09:00"))
            // Propietat
            nouEventXerrada nom "Kotlin I" autor "Pep" aula 1U data "2024-03-11T09:00"
            nouEventTaller nom "Arduino" autor "Pere" aula 2U data "2024-03-11T09:00"
            // Sobrecàrrega operador
            +Event(nom = "Kotlin II", autor = "Joan", aula = 4U, data = LocalDateTime.parse("2024-03-12T09:00"))
        }
    }
    println(tecno)
}