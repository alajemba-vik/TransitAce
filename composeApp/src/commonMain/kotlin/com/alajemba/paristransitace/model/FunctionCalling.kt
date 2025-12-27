package com.alajemba.paristransitace.model

import com.alajemba.paristransitace.network.models.FunctionDeclaration
import com.alajemba.paristransitace.network.models.FunctionParameters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.StringResource
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.help
import paristransitace.composeapp.generated.resources.load_storyline
import paristransitace.composeapp.generated.resources.restart_game
import paristransitace.composeapp.generated.resources.show_all_storylines

sealed class FunctionCallingActions(
    val actionId: String,
    val description: String,
    val parameters: FunctionParameters,
    val stringResId: StringResource
) {
    object GetAllStorylines : FunctionCallingActions(
        "get_all_storylines",
        "Retrieves and displays a list of all available game storylines or scenarios.",
        FunctionParameters(
            type = "object",
            properties = emptyMap(),
            required = emptyList()
        ),
        Res.string.show_all_storylines
    )
    object LoadStoryline : FunctionCallingActions(
        "load_new_storyline",
        description = "Clears the current session and loads a specific storyline. Requires a keyword or ID.",
        parameters = FunctionParameters(
            type = "object",
            properties = mapOf(
                FunctionParameters.PARAM_SCENARIO_ID to JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("integer"),        // Matches doc: "type (string)"
                        "description" to JsonPrimitive("The exact ID of the scenario to load.") // Matches doc: "description (string)"
                    )
                ),
            ),
            required = listOf(FunctionParameters.PARAM_SCENARIO_ID)
        ),
        Res.string.load_storyline
    )
    object RestartGame : FunctionCallingActions(
        "reset_game",
        description = "Resets the current storyline progress back to the beginning (0% completion).",
        parameters = FunctionParameters(
            type = "object",
            properties = emptyMap(),
            required = emptyList()
        ),
        Res.string.restart_game
    )
    object ShowHelp : FunctionCallingActions(
        "show_help",
        description = "Displays a help menu listing all available commands and actions Sophia can perform.",
        parameters = FunctionParameters(
            type = "object",
            properties = emptyMap(),
            required = emptyList()
        ),
        Res.string.help
    )

    companion object {
        fun toFunctionDeclaration(action: FunctionCallingActions): FunctionDeclaration {
            return FunctionDeclaration(
                name = action.actionId,
                description = action.description,
                parameters = action.parameters
            )
        }

        fun getAllFunctionDeclarations(): List<FunctionDeclaration> {
            return listOf(
                GetAllStorylines,
                LoadStoryline,
                RestartGame,
                ShowHelp
            ).map { toFunctionDeclaration(it) }
        }
    }
}