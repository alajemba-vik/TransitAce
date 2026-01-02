package com.alajemba.paristransitace.ui.mapper

import com.alajemba.paristransitace.domain.model.ScenarioTheme
import org.jetbrains.compose.resources.DrawableResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.scene_airport
import paristransitace.composeapp.generated.resources.scene_broken_gate
import paristransitace.composeapp.generated.resources.scene_bus_interior
import paristransitace.composeapp.generated.resources.scene_cafe_reward
import paristransitace.composeapp.generated.resources.scene_confused_map
import paristransitace.composeapp.generated.resources.scene_eiffel_tower
import paristransitace.composeapp.generated.resources.scene_gate_jump
import paristransitace.composeapp.generated.resources.scene_getting_fined
import paristransitace.composeapp.generated.resources.scene_inspectors
import paristransitace.composeapp.generated.resources.scene_luggage_struggle
import paristransitace.composeapp.generated.resources.scene_metro_platform
import paristransitace.composeapp.generated.resources.scene_night_street
import paristransitace.composeapp.generated.resources.scene_rer_packed
import paristransitace.composeapp.generated.resources.scene_romance
import paristransitace.composeapp.generated.resources.scene_room
import paristransitace.composeapp.generated.resources.scene_strike_crowd
import paristransitace.composeapp.generated.resources.scene_suburban_station
import paristransitace.composeapp.generated.resources.scene_ticket_machine
import paristransitace.composeapp.generated.resources.scene_turnstile_success
import paristransitace.composeapp.generated.resources.scene_university

fun ScenarioTheme.toDrawable(): DrawableResource = when (this) {
    ScenarioTheme.MORNING -> Res.drawable.scene_room
    ScenarioTheme.METRO_PLATFORM -> Res.drawable.scene_metro_platform
    ScenarioTheme.BUS_INTERIOR -> Res.drawable.scene_bus_interior
    ScenarioTheme.RER_PACKED -> Res.drawable.scene_rer_packed
    ScenarioTheme.TICKET_MACHINE -> Res.drawable.scene_ticket_machine
    ScenarioTheme.TURNSTILE_SUCCESS -> Res.drawable.scene_turnstile_success
    ScenarioTheme.INSPECTORS -> Res.drawable.scene_inspectors
    ScenarioTheme.GATE_JUMP -> Res.drawable.scene_gate_jump
    ScenarioTheme.GETTING_FINED -> Res.drawable.scene_getting_fined
    ScenarioTheme.BROKEN_GATE -> Res.drawable.scene_broken_gate
    ScenarioTheme.STRIKE_CROWD -> Res.drawable.scene_strike_crowd
    ScenarioTheme.EIFFEL_TOWER -> Res.drawable.scene_eiffel_tower
    ScenarioTheme.UNIVERSITY -> Res.drawable.scene_university
    ScenarioTheme.SUBURBAN_STATION -> Res.drawable.scene_suburban_station
    ScenarioTheme.AIRPORT -> Res.drawable.scene_airport
    ScenarioTheme.NIGHT_STREET -> Res.drawable.scene_night_street
    ScenarioTheme.ROMANCE -> Res.drawable.scene_romance
    ScenarioTheme.CONFUSED_MAP -> Res.drawable.scene_confused_map
    ScenarioTheme.LUGGAGE_STRUGGLE -> Res.drawable.scene_luggage_struggle
    ScenarioTheme.CAFE_REWARD -> Res.drawable.scene_cafe_reward
    ScenarioTheme.EMPTY_LATE -> Res.drawable.scene_night_street
    ScenarioTheme.DEFAULT -> Res.drawable.scene_night_street
}