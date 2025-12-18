package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.ChatSDK
import com.alajemba.paristransitace.ui.model.Scenario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class GameViewModel(private val chatSDK: ChatSDK) : ViewModel() {
    private val _scenariosState = MutableStateFlow(emptyList<Scenario>())
    val scenariosState = _scenariosState.asStateFlow()


    // TODO("move to data layer")
    init {
        viewModelScope.launch {
            val isFr = false // TODO: replace with real locale check

            // keep runtime state consistent while raw scenario data is available for mapping/implementation
            _scenariosState.value = emptyList()
            _scenariosState.value = listOf(
                Scenario(
                    id = "1",
                    title = "The Morning in Orly",
                    description = """You wake up in your tiny apartment in Orly. That Uber from the airport last night drained your bank account. You have €100 left. Cardboard tickets are history. How do you prepare for the day?""",
                    correctOptionId = 1,
                    options = listOf(
                        GameOption("OPT_1_APP", "Install 'IDF Mobilités' App", cost = 0.0, moraleImpact = 5, nextScenarioId = "SCENARIO_2"),
                        GameOption("OPT_1_NAVIGO", "Buy Navigo Découverte (Physical)", cost = 35.0, moraleImpact = 10, nextScenarioId = "SCENARIO_2")
                    )
                ),
                Scenario(
                    id = "2",
                    title = "Commute to Class",
                    description = "You need to get to Villejuif. You can take the Metro 14 or a Bus.",
                    correctOptionId = "OPT_2_BUS", // Example
                    options = listOf(
                        GameOption("OPT_2_BUS", "Take the Bus (€2.00)", cost = 2.0, moraleImpact = -5, nextScenarioId = "SCENARIO_3"),
                        GameOption("OPT_2_METRO", "Take Metro 14 (€2.50)", cost = 2.50, moraleImpact = 5, nextScenarioId = "SCENARIO_3")
                    )
                )
            )
        }
    }

}