package cat.copernic.mendez.dsl

import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.random.Random

const val MAX_SCORE_SEX_MATCHING = 1_000_000_000L
const val MAX_SCORE_SMOKE = 1000_000_000L
const val REDUCTION_PER_DAY = 2_740L
const val THRESHOLD_YEARS = 5

enum class Sex { MAN, WOMAN }

enum class SexualInclination { HETERO, GUY_LESBIAN, BISEXUAL }

data class Client(
    val id: Int = abs(Random.nextInt()),
    var name: String = "N/A",
    var sex: Sex = Sex.MAN,
    var sexualInclination: SexualInclination = SexualInclination.HETERO,
    var dob: LocalDateTime = LocalDateTime.now(),
    var age: Int = 0,
    var smoker: Boolean = false,
    var location: String = "N/A",
) {
    override fun hashCode(): Int {
        return id
    }
}

data class Match(val candidate: Client, val score: Long)

class DatingService(
    private val name: String = "Free Dating Service",
    private val year: Int = LocalDateTime.now().year,
) {

    private val _clients = mutableListOf<Client>()
    val clients
        get() = _clients.toList()

    fun add(client: Client) = _clients.add(client)

    fun findBestMatches(client: Client): List<Match> {
        val matches = mutableListOf<Match>()
        _clients.forEach { candidate ->
            if (candidate != client &&
                isSexualyCompatible(client, candidate) &&
                isSmokeCompatible(client, candidate) &&
                isAgeCompatible(client, candidate, THRESHOLD_YEARS)
            )
                matches.add(aval(client, candidate))
        }
        return matches.sortedByDescending { it.score }
    }

    private fun aval(client: Client, candidate: Client): Match {
        var score = avalSex(client, candidate)
        score += evalDob(client, candidate)
        score += evalLocation(client, candidate)
        score += evalSmoker(client, candidate)
        val match = Match(candidate, score)
        return match
    }

    private fun isSexualyCompatible(client: Client, candidate: Client): Boolean {
        // full match
        if (client.sex == Sex.MAN && client.sexualInclination == SexualInclination.HETERO)
            if (candidate.sex == Sex.WOMAN && candidate.sexualInclination == SexualInclination.HETERO)
                return true
        // full match
        if (client.sex == Sex.WOMAN && client.sexualInclination == SexualInclination.HETERO)
            if (candidate.sex == Sex.MAN && candidate.sexualInclination == SexualInclination.HETERO)
                return true
        // full match
        if (client.sex == Sex.MAN && client.sexualInclination == SexualInclination.GUY_LESBIAN)
            if (candidate.sex == Sex.MAN && candidate.sexualInclination == SexualInclination.GUY_LESBIAN)
                return true
        // full match
        if (client.sex == Sex.WOMAN && client.sexualInclination == SexualInclination.GUY_LESBIAN)
            if (candidate.sex == Sex.WOMAN && candidate.sexualInclination == SexualInclination.GUY_LESBIAN)
                return true
        // full match
        if (client.sex == Sex.MAN && client.sexualInclination == SexualInclination.BISEXUAL)
            if (candidate.sex == Sex.MAN && candidate.sexualInclination == SexualInclination.BISEXUAL)
                return true
        // full match
        if (client.sex == Sex.MAN && client.sexualInclination == SexualInclination.BISEXUAL)
            if (candidate.sex == Sex.WOMAN && candidate.sexualInclination == SexualInclination.BISEXUAL)
                return true
        // full match
        if (client.sex == Sex.WOMAN && client.sexualInclination == SexualInclination.BISEXUAL)
            if (candidate.sex == Sex.MAN && candidate.sexualInclination == SexualInclination.BISEXUAL)
                return true
        // full match
        if (client.sex == Sex.WOMAN && client.sexualInclination == SexualInclination.BISEXUAL)
            if (candidate.sex == Sex.WOMAN && candidate.sexualInclination == SexualInclination.BISEXUAL)
                return true
        // Other rules to be added
        return false
    }

    // Client and candidate have the same habits, o candidate is a nonsmoker
    private fun isSmokeCompatible(client: Client, candidate: Client): Boolean =
        client.smoker == candidate.smoker || !candidate.smoker

    // Max number of years
    private fun isAgeCompatible(client: Client, candidate: Client, threshold: Int): Boolean =
        abs(candidate.age - client.age) <= threshold

    private fun avalSex(client: Client, candidate: Client): Long {
        var score = -MAX_SCORE_SEX_MATCHING

        if (isSexualyCompatible(client, candidate)) score = MAX_SCORE_SEX_MATCHING

        return score
    }

    private fun evalDob(client: Client, candidate: Client): Long {
        return Duration.between(client.dob, candidate.dob).toDays() * REDUCTION_PER_DAY
    }

    private fun evalSmoker(client: Client, candidate: Client): Long {
        var score = -MAX_SCORE_SMOKE
        if (client.smoker == candidate.smoker) {
            score = MAX_SCORE_SMOKE
        }
        return score
    }

    private fun evalLocation(client: Client, candidate: Client): Long {
        var score = checkProximity(client.location, candidate.location)

        return score
    }

    private fun checkProximity(location1: String?, location2: String?) = 0L

    override fun toString(): String {
        val buffer = StringBuilder()
        with(buffer) {
            append("Name: '$name' year: '$year' ")
            append("\n")
            _clients.forEach {
                append(it.toString())
                append("\n")
            }
        }
        return buffer.toString()
    }
}

//******************* DSL *********************
// datingConfig is a function parameter
fun datingService(datingConfig: DatingConfig.() -> Unit): DatingService {

    val datingConfig = DatingConfig().apply(datingConfig)
    return DatingService(datingConfig.name, datingConfig.year).apply {
        datingConfig.clients.forEach(this::add)
    }
}

class DatingConfig {

    private val _clients = mutableSetOf<Client>()
    val clients
        get() = _clients.toList()

    lateinit var name: String
    var year: Int = LocalDateTime.now().year

    val clientConfig = ClientConfig()

    inner class ClientConfig {
        private val _clients = this@DatingConfig._clients

        operator fun invoke(clientConfig: ClientConfig.() -> Unit) {
            apply(clientConfig)
        }

        val newClient: NewClient
            get() = NewClient()

        inner class NewClient {
            private val client = Client()

            init {
                _clients.add(client)
            }

            infix fun name(name: String): NewClient {
                client.name = name
                return this
            }

            infix fun sex(sex: Sex): NewClient {
                client.sex = sex
                return this
            }

            infix fun sexualInclination(sexualInclination: SexualInclination): NewClient {
                client.sexualInclination = sexualInclination
                return this
            }

            // dob format yyyy-mm-ddThh:mm
            infix fun dob(dob: String): NewClient {
                client.dob = LocalDateTime.parse(dob)
                client.age = (Duration.between(
                    LocalDateTime.parse(dob),
                    LocalDateTime.now()).toDays() / 365).toInt()
                return this
            }

            infix fun smoker(smoker: Boolean): NewClient {
                client.smoker = smoker
                return this
            }

            infix fun location(location: String): NewClient {
                client.location = location
                return this
            }
        }
    }

}

//**************** DSL END ********************

fun main() {

    val service = datingService {
        name = "Free dating Service for all"
        year = 2021
        clientConfig {
            newClient name "Pep" sex
                    Sex.MAN sexualInclination SexualInclination.HETERO dob
                    "1974-03-11T09:00" smoker false location "Terrassa"
            newClient name "Stella" sex
                    Sex.WOMAN sexualInclination SexualInclination.HETERO dob
                    "1974-03-11T09:00" smoker false location "Terrassa"
            newClient name "Madga" sex
                    Sex.WOMAN sexualInclination SexualInclination.HETERO dob
                    "1973-03-11T09:00" smoker false location "Terrassa"
            newClient name "Joan" sex
                    Sex.MAN sexualInclination SexualInclination.HETERO dob
                    "1992-03-11T09:00" smoker false location "Terrassa"
            newClient name "Felip" sex
                    Sex.MAN sexualInclination SexualInclination.GUY_LESBIAN dob
                    "1999-03-11T09:00" smoker false location "Terrassa"
            newClient name "Marc" sex
                    Sex.MAN sexualInclination SexualInclination.GUY_LESBIAN dob
                    "2001-03-11T09:00" smoker false location "Terrassa"
            newClient name "Ana" sex
                    Sex.WOMAN sexualInclination SexualInclination.HETERO dob
                    "2000-03-11T09:00" smoker false location "Terrassa"
            newClient name "Marta" sex
                    Sex.WOMAN sexualInclination SexualInclination.HETERO dob
                    "2002-03-11T09:00" smoker true location "Terrassa"
            newClient name "Tere" sex
                    Sex.WOMAN sexualInclination SexualInclination.HETERO dob
                    "1989-03-11T09:00" smoker false location "Terrassa"
            newClient name "Eva" sex
                    Sex.WOMAN sexualInclination SexualInclination.HETERO dob
                    "2000-03-11T09:00" smoker true location "Terrassa"
            newClient name "Sam" sex
                    Sex.MAN sexualInclination SexualInclination.HETERO dob
                    "2002-03-11T09:00" smoker true location "Terrassa"
        }
    }
    //println(service)
    service.clients.forEach { client ->
        println(client)
        service.findBestMatches(client).forEach(::println)
        println()
    }

}