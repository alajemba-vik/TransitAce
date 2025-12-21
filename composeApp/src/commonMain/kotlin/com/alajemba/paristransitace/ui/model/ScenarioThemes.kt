package com.alajemba.paristransitace.ui.model

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer: we serialize the enum by its NAME (String) so the DrawableResource
 * is not required to be serializable. On deserialization we map the incoming string
 * to the nearest enum via fromKey (falls back to DEFAULT).
 */
object ScenarioThemeSerializer : KSerializer<ScenarioTheme> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ScenarioTheme", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ScenarioTheme) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): ScenarioTheme {
        val key = decoder.decodeString()
        return ScenarioTheme.fromKey(key)
    }
}

@Serializable(with = ScenarioThemeSerializer::class)
enum class ScenarioTheme(val image: DrawableResource) {

    MORNING(Res.drawable.scene_room),
    // --- CATEGORY A: TRANSIT CORE ---
    METRO_PLATFORM(Res.drawable.scene_metro_platform),
    BUS_INTERIOR(Res.drawable.scene_bus_interior),
    RER_PACKED(Res.drawable.scene_rer_packed),
    TICKET_MACHINE(Res.drawable.scene_ticket_machine),
    TURNSTILE_SUCCESS(Res.drawable.scene_turnstile_success),

    // --- CATEGORY B: VILLAINS & INFRACTIONS ---
    INSPECTORS(Res.drawable.scene_inspectors),
    GATE_JUMP(Res.drawable.scene_gate_jump),
    GETTING_FINED(Res.drawable.scene_getting_fined),
    BROKEN_GATE(Res.drawable.scene_broken_gate),
    STRIKE_CROWD(Res.drawable.scene_strike_crowd),

    // --- CATEGORY C: LOCATIONS ---
    EIFFEL_TOWER(Res.drawable.scene_eiffel_tower),
    UNIVERSITY(Res.drawable.scene_university),
    SUBURBAN_STATION(Res.drawable.scene_suburban_station),
    AIRPORT(Res.drawable.scene_airport),
    NIGHT_STREET(Res.drawable.scene_night_street),
    // --- CATEGORY D: STORY & EMOTION ---
    ROMANCE(Res.drawable.scene_romance),
    CONFUSED_MAP(Res.drawable.scene_confused_map),
    LUGGAGE_STRUGGLE(Res.drawable.scene_luggage_struggle),
    CAFE_REWARD(Res.drawable.scene_cafe_reward),
    EMPTY_LATE(Res.drawable.scene_night_street),

    // FALLBACK (If AI makes up a new key)
    DEFAULT(Res.drawable.scene_night_street);

    companion object {
        /**
         * Safely finds the theme from the AI's string output.
         * Returns DEFAULT if the AI hallucinates a key.
         */
        fun fromKey(key: String): ScenarioTheme {
            return entries.find { it.name.equals(key, ignoreCase = true) } ?: DEFAULT
        }
    }
}