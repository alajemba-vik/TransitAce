package com.alajemba.paristransitace.ui.model

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

    val isOnScenariosGenerationStep: Boolean get() = !isOnLanguageStep && !isOnNameStep &&
            !isOnSelectSimulationStep && scenariosGenerationStatus == ScenarioGenerationStatus.NOT_STARTED

    val isOnScenariosGenerationFailureStep: Boolean get() = !isOnLanguageStep && !isOnNameStep &&
            !isOnSelectSimulationStep && scenariosGenerationStatus == ScenarioGenerationStatus.FAILURE

    val isSetupComplete: Boolean get() = !isOnLanguageStep && !isOnNameStep &&
            !isOnSelectSimulationStep && scenariosGenerationStatus == ScenarioGenerationStatus.SUCCESS

    val isEnglish: Boolean get() = language == GameLanguage.ENGLISH
    val isCustomSimulation: Boolean get() = simulationType == SimulationType.CUSTOM

    companion object {
        val EMPTY = GameSetup(GameLanguage.UNDEFINED, "", SimulationType.UNDEFINED)
    }

    enum class GameLanguage {
        ENGLISH, FRENCH, UNDEFINED;
    }

    enum class SimulationType {
        DEFAULT, CUSTOM, UNDEFINED
    }

    enum class ScenarioGenerationStatus {
        SUCCESS, FAILURE, NOT_STARTED
    }
}

