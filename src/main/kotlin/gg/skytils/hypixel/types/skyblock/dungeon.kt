package gg.skytils.hypixel.types.skyblock

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class DungeonsData(
    @Serializable(with = DungeonTypeSerializer::class)
    val dungeon_types: Map<String, DungeonData> = emptyMap(),
    // This can not exist when the player has entered a dungeon but not completed it
    // For some reason some players will have an empty class map and others will just be missing the entire thing
    // Why did they design it this way?
    // Why is there no api guarantee?
    val player_classes: Map<String, DungeonClass> = emptyMap(),
    val dungeon_journal: DungeonJournalData = DungeonJournalData(),
    val selected_dungeon_class: String? = null,
    val secrets: Double = 0.0,
)

object DungeonTypeSerializer : KSerializer<Map<String, DungeonData>> {
    override val descriptor = MapSerializer(String.serializer(), DungeonDataSerializer).descriptor

    override fun deserialize(decoder: Decoder): Map<String, DungeonData> =
        (decoder as? JsonDecoder)?.decodeJsonElement()?.jsonObject?.let { json ->
            json.entries
                .groupBy { it.key.removePrefix("master_") }
                .map { (k,v) ->
                    val obj = buildJsonObject {
                        v.forEach { put(if("master" in it.key) "master" else "normal", it.value) }
                    }
                    k to decoder.json.decodeFromJsonElement<DungeonData>(obj)
                }.toMap()
        } ?: error("Only json supported")

    override fun serialize(encoder: Encoder, value: Map<String, DungeonData>) {
        TODO("Not yet implemented")
    }

}

@Serializable(with = DungeonDataSerializer::class)
data class DungeonData(
    val experience: Double,
    val normal: DungeonModeData,
    val master: DungeonModeData?
)

object DungeonDataSerializer : KSerializer<DungeonData> {
    override val descriptor = buildClassSerialDescriptor("DungeonData") {
        element<Double>("experience")
        element<DungeonModeData>("normal")
        element<DungeonModeData?>("master")
    }

    override fun deserialize(decoder: Decoder): DungeonData =
        (decoder as? JsonDecoder)?.decodeJsonElement()?.jsonObject?.let { json ->
            DungeonData(
                json["normal"]?.jsonObject?.get("experience")?.jsonPrimitive?.double ?: 0.0,
                decoder.json.decodeFromJsonElement(json["normal"]!!),
                json["master"]?.takeIf {
                    it.jsonObject["tier_completions"]?.jsonObject?.isEmpty() == false
                }?.let {
                    decoder.json.decodeFromJsonElement(it)
                }
            )
        } ?: error("Only json supported")

    override fun serialize(encoder: Encoder, value: DungeonData) {
        TODO("Not yet implemented")
    }
}

@Serializable
data class DungeonModeData(
    val times_played: Map<String, Double> = emptyMap(),
    val tier_completions: Map<String, Double> = emptyMap(),
    val milestone_completions: Map<String, Double> = emptyMap(),
    val highest_tier_completed: Int = 0,
    val fastest_time: Map<String, Double> = emptyMap(),
    val fastest_time_s: Map<String, Double> = emptyMap(),
    val fastest_time_s_plus: Map<String, Double> = emptyMap(),
    val best_runs: Map<String, List<DungeonRun>> = emptyMap(),
    val best_score: Map<String, Double> = emptyMap(),
    val mobs_killed: Map<String, Double> = emptyMap(),
    val most_mobs_killed: Map<String, Double> = emptyMap(),
    val most_healing: Map<String, Double> = emptyMap(),
    val watcher_kills: Map<String, Double> = emptyMap(),
)

@Serializable
data class DungeonRun(
    val timestamp: Long,
    val score_exploration: Double,
    val score_speed: Double,
    val score_skill: Double,
    val score_bonus: Double,
    val dungeon_class: String,
    val teammates: List<String>,
    val elapsed_time: Long,
    val damage_dealt: Double,
    val deaths: Double,
    val mobs_killed: Double,
    val secrets_found: Double,
    val damage_mitigated: Double = 0.0,
    val ally_healing: Double = 0.0
)

@Serializable
data class DungeonClass(
    val experience: Double = 0.0
)

@Serializable
data class DungeonJournalData(
    val unlocked_journals: List<String> = emptyList()
)