package com.alajemba.paristransitace.domain.model

data class GameSetup(
    val language: GameLanguage,
    val deviceLanguage: GameLanguage,
    val name: String,
    val simulationType: SimulationType,
    val scenariosGenerationStatus: ScenarioGenerationStatus = ScenarioGenerationStatus.NOT_STARTED,
) {
    private val currentStep: GameSetupStep get() = when {
        language == GameLanguage.UNDEFINED -> GameSetupStep.LANGUAGE
        name.isBlank() -> GameSetupStep.NAME
        simulationType == SimulationType.UNDEFINED -> GameSetupStep.SIMULATION_SELECT
        scenariosGenerationStatus == ScenarioGenerationStatus.NOT_STARTED -> GameSetupStep.SCENARIOS_REQUIREMENTS
        scenariosGenerationStatus == ScenarioGenerationStatus.PROCESSING -> GameSetupStep.SCENARIOS_GENERATING
        scenariosGenerationStatus == ScenarioGenerationStatus.SUCCESS -> GameSetupStep.SCENARIOS_SUCCESS
        scenariosGenerationStatus == ScenarioGenerationStatus.FAILURE -> GameSetupStep.SCENARIOS_FAILURE
        scenariosGenerationStatus == ScenarioGenerationStatus.SCENARIOS_GENERATED_ACTION -> GameSetupStep.COMPLETE
        else -> GameSetupStep.LANGUAGE
    }

    val isOnLanguageStep: Boolean get() = currentStep == GameSetupStep.LANGUAGE
    val isOnNameStep: Boolean get() = currentStep == GameSetupStep.NAME
    val isOnSelectSimulationStep: Boolean get() = currentStep == GameSetupStep.SIMULATION_SELECT
    val isOnScenariosGenRequirementsStep: Boolean get() = currentStep == GameSetupStep.SCENARIOS_REQUIREMENTS
    val isOnScenariosGenerationStep: Boolean get() = currentStep == GameSetupStep.SCENARIOS_GENERATING
    val isOnScenariosGenerationSuccessStep: Boolean get() = currentStep == GameSetupStep.SCENARIOS_SUCCESS
    val isOnScenariosGenerationFailureStep: Boolean get() = currentStep == GameSetupStep.SCENARIOS_FAILURE
    val isSetupComplete: Boolean get() = currentStep == GameSetupStep.COMPLETE ||
            (!isCustomSimulation && currentStep == GameSetupStep.SCENARIOS_SUCCESS)

    val isEnglish: Boolean get() = language == GameLanguage.ENGLISH ||
            (isOnLanguageStep && deviceLanguage == GameLanguage.ENGLISH)
    val isCustomSimulation: Boolean get() = simulationType == SimulationType.CUSTOM

    companion object {
        val EMPTY = GameSetup(GameLanguage.UNDEFINED, GameLanguage.UNDEFINED, "", SimulationType.UNDEFINED)
    }
}

private enum class GameSetupStep {
    LANGUAGE,
    NAME,
    SIMULATION_SELECT,
    SCENARIOS_REQUIREMENTS,
    SCENARIOS_GENERATING,
    SCENARIOS_SUCCESS,
    SCENARIOS_FAILURE,
    COMPLETE
}

enum class GameLanguage(val languageTag: String ="") {
    ENGLISH("en"),
    FRENCH("fr"),
    UNDEFINED("")
}
enum class SimulationType { DEFAULT, CUSTOM, UNDEFINED }
enum class ScenarioGenerationStatus { SUCCESS, FAILURE, NOT_STARTED, PROCESSING, SCENARIOS_GENERATED_ACTION }