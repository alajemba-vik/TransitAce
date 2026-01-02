package com.alajemba.paristransitace.data.mapper

import com.alajemba.paristransitace.db.StoryEntity
import com.alajemba.paristransitace.domain.model.StoryLine

fun StoryEntity.toDomain() = StoryLine(
    id = id,
    title = title,
    description = description ?: "",
    timeCreated = timeCreated,
    initialBudget = initialBudget ?: 0.0,
    initialMorale = initialMorale?.toInt() ?: 0
)