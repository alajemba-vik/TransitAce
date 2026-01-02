package com.alajemba.paristransitace.data.mapper

import com.alajemba.paristransitace.data.remote.model.GameInventoryResponse
import com.alajemba.paristransitace.data.remote.model.ScenarioResponse
import com.alajemba.paristransitace.data.remote.model.ScenarioOptionResponse
import com.alajemba.paristransitace.domain.model.GameInventory
import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.ScenarioOption
import com.alajemba.paristransitace.domain.model.ScenarioTheme

fun ScenarioResponse.toDomain() = Scenario(
    id = id,
    title = title,
    description = description,
    options = options.map { it.toDomain() },
    correctOptionId = correctOptionId,
    nextScenarioId = nextScenarioId,
    currentIndexInGame = currentIndexInGame,
    scenarioTheme = ScenarioTheme.fromKey(scenarioTheme)
)

fun ScenarioOptionResponse.toDomain() = ScenarioOption(
    id = id,
    text = text,
    budgetImpact = budgetImpact,
    moraleImpact = moraleImpact,
    commentary = commentary,
    inventory = inventory.map { it.toDomain() },
    increaseLegalInfractionsBy = increaseLegalInfractionsBy
)

fun GameInventoryResponse.toDomain() = GameInventory(
    name = name,
    description = description,
    imageUrl = imageUrl
)

fun Scenario.toResponse() = ScenarioResponse(
    id = id,
    title = title,
    description = description,
    options = options.map { it.toResponse() },
    correctOptionId = correctOptionId,
    nextScenarioId = nextScenarioId,
    currentIndexInGame = currentIndexInGame,
    scenarioTheme = scenarioTheme.name
)

fun ScenarioOption.toResponse() = ScenarioOptionResponse(
    id = id,
    text = text,
    budgetImpact = budgetImpact,
    moraleImpact = moraleImpact,
    commentary = commentary,
    inventory = inventory.map { it.toResponse() },
    increaseLegalInfractionsBy = increaseLegalInfractionsBy
)

fun GameInventory.toResponse() = GameInventoryResponse(
    name = name,
    description = description,
    imageUrl = imageUrl
)