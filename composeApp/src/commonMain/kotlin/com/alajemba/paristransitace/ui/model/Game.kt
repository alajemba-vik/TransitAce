package com.alajemba.paristransitace.ui.model

import kotlin.Boolean

data class GameSetup(
    val language: GameLanguage,
    val name: String,
    val simulationType: SimulationType,
    val scenariosGenerationStatus: ScenarioGenerationStatus = ScenarioGenerationStatus.NOT_STARTED,
) {
    val isOnLanguageStep: Boolean get() = language == GameLanguage.UNDEFINED

    val isOnNameStep: Boolean get() = !isOnLanguageStep && name.isBlank()
    val isOnSelectSimulationStep: Boolean get() = !isOnLanguageStep && !isOnNameStep &&
            simulationType == SimulationType.UNDEFINED

    val isOnScenariosGenRequirementsStep: Boolean get() = !isOnLanguageStep && !isOnNameStep &&
            !isOnSelectSimulationStep && !isOnSelectSimulationStep &&  scenariosGenerationStatus == ScenarioGenerationStatus.NOT_STARTED

    val isOnScenariosGenerationStep: Boolean get() = !isOnLanguageStep && !isOnNameStep &&
            !isOnSelectSimulationStep && scenariosGenerationStatus == ScenarioGenerationStatus.PROCESSING

    val isOnScenariosGenerationSuccessStep: Boolean get() = !isOnLanguageStep && !isOnNameStep &&
            !isOnSelectSimulationStep && scenariosGenerationStatus == ScenarioGenerationStatus.SUCCESS
    val isOnScenariosGenerationFailureStep: Boolean get() = !isOnLanguageStep && !isOnNameStep &&
            !isOnSelectSimulationStep && scenariosGenerationStatus == ScenarioGenerationStatus.FAILURE

    val isSetupComplete: Boolean get() = !isOnLanguageStep && !isOnNameStep &&
            !isOnSelectSimulationStep &&
            (if (isCustomSimulation) scenariosGenerationStatus == ScenarioGenerationStatus.SCENARIOS_GENERATED_ACTION else
                    scenariosGenerationStatus == ScenarioGenerationStatus.SUCCESS)

    val isPendingCustomScenarioSaveAction : Boolean get() = isCustomSimulation && scenariosGenerationStatus == ScenarioGenerationStatus.SUCCESS

    val isEnglish: Boolean get() = language == GameLanguage.ENGLISH
    val isCustomSimulation: Boolean get() = simulationType == SimulationType.CUSTOM

    companion object {
        val EMPTY = GameSetup(GameLanguage.UNDEFINED,  "", SimulationType.UNDEFINED)
    }

    enum class GameLanguage {
        ENGLISH, FRENCH, UNDEFINED;
    }

    enum class SimulationType {
        DEFAULT, CUSTOM, UNDEFINED
    }

    enum class ScenarioGenerationStatus {
        SUCCESS, FAILURE, NOT_STARTED, PROCESSING, SCENARIOS_GENERATED_ACTION
    }
}

