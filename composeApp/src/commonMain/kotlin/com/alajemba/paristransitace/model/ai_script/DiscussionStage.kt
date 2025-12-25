package com.alajemba.paristransitace.model.ai_script

import com.alajemba.paristransitace.ui.model.GameSetup

enum class DiscussionStage {
    LANGUAGE,
    NAME,
    SIMULATION_TYPE,
    GENERATING,
    FAILURE,
    COMPLETE,
    UNKNOWN
}

val GameSetup.currentStage: DiscussionStage
    get() = when {
        isOnLanguageStep -> DiscussionStage.LANGUAGE
        isOnNameStep -> DiscussionStage.NAME
        isOnSelectSimulationStep -> DiscussionStage.SIMULATION_TYPE
        isOnScenariosGenerationStep -> DiscussionStage.GENERATING
        isOnScenariosGenerationFailureStep -> DiscussionStage.FAILURE
        isSetupComplete -> DiscussionStage.COMPLETE
        else -> DiscussionStage.UNKNOWN
    }