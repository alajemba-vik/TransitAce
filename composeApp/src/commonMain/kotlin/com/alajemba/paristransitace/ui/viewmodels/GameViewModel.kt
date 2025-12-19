package com.alajemba.paristransitace.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alajemba.paristransitace.ChatSDK
import com.alajemba.paristransitace.ui.model.GameAlert
import com.alajemba.paristransitace.ui.model.Scenario
import com.alajemba.paristransitace.ui.model.ScenarioOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class GameViewModel(private val chatSDK: ChatSDK) : ViewModel() {
    private val _scenariosState = MutableStateFlow(emptyList<Scenario>())

    private val _currentScenario = MutableStateFlow<Scenario?>(null)
    val currentScenario = _currentScenario.asStateFlow()

    private val _scenarioProgress = MutableStateFlow(0f)
    val scenarioProgress = _scenarioProgress.asStateFlow()
    val scenarioProgressText = _currentScenario.asStateFlow().map { current ->
        val index = current?.currentIndexInGame ?: 0
        val total = _scenariosState.value.size
        "$index/$total"
    }

    private val _alerts = MutableStateFlow(emptyList<GameAlert>())
    val unreadAlertsCount = _alerts.asStateFlow().map { alerts ->
        alerts.count { it.unread }
    }

    // TODO("move to data layer")
    fun startGame(isEnglish: Boolean) {
        viewModelScope.launch {
            _scenariosState.value = buildScenarios(!isEnglish)

            nextScenario()
        }
    }


    fun nextScenario(){
        val currentIndex = _currentScenario.value?.currentIndexInGame ?: 0
        val nextIndex = currentIndex + 1

        _currentScenario.value = _scenariosState.value.getOrNull(nextIndex)?.copy(
            currentIndexInGame = nextIndex
        )

        _scenarioProgress.value = nextIndex.toFloat() / _scenariosState.value.size
    }

    fun setAlert(message: String, title: String){
        _alerts.value += GameAlert(title = title, message = message, unread = true)
    }

    fun onOptionSelected(option: ScenarioOption) {

    }

}


private fun createOption(id: String, text: String, cost: Double = 0.0, morale: Int = 0) =
    ScenarioOption(id = id, text = text, cost = cost, moraleImpact = morale)


private fun createScenario(
    id: String,
    title: String,
    description: String,
    options: List<ScenarioOption>,
    correctOptionIndex: Int = 0,
) = Scenario(
    id = id,
    title = title,
    description = description,
    options = options,
    correctOptionId = correctOptionIndex,
)


private fun buildScenarios(isFr: Boolean): List<Scenario> {
    return listOf(
        // SCENARIO_0: Morning in Orly
        createScenario(
            id = "0",
            title = if (isFr) "Le Matin à Orly" else "The Morning in Orly",
            description = if (isFr)
                "Vous vous réveillez dans votre petit appartement à Orly. Le Uber depuis l'aéroport hier soir vous a coûté cher. Il vous reste 100€. Vous devez vous équiper. Les tickets en carton sont morts. Que faites-vous ?"
            else
                "You wake up in your tiny apartment in Orly. That Uber from the airport last night drained your bank account. You have €100 left. Cardboard tickets are history. How do you prepare for the day?",
            options = listOf(
                createOption("0_APP", if (isFr) "Installer l'appli 'IDF Mobilités' sur Android/iPhone" else "Install 'IDF Mobilités' App on Android/iPhone", cost = 0.0, morale = 10),
                createOption("0_NAVIGO", if (isFr) "Acheter un passe Navigo Découverte Hebdo (35€)" else "Buy a Navigo Découverte Weekly (35€)", cost = 35.0, morale = 20)
            ),
            correctOptionIndex = 1
        ),

        // SCENARIO_1: Commute to Class
        createScenario(
            id = "1",
            title = if (isFr) "Trajet vers les Cours" else "Commute to Class",
            description = if (isFr)
                "Vous devez aller en cours à Villejuif. C'est tout proche, mais vous devez prendre le Métro 14 ou un Bus. Vous hésitez sur le prix. En 2025, tout a changé."
            else
                "You need to get to class in Villejuif. It's nearby, but requires transit. You could take the new Metro 14 extension or a Bus. You hesitate on the price. In 2025, the rules are strict.",
            options = listOf(
                createOption("1_BUS", if (isFr) "Prendre le Bus (2,00€)" else "Take the Bus (€2.00)", cost = 2.0, morale = 5),
                createOption("1_METRO", if (isFr) "Prendre le Métro 14 (2,50€)" else "Take Metro 14 (€2.50)", cost = 2.50, morale = 10)
            ),
            correctOptionIndex = 1
        ),

        // SCENARIO_1_B: The Open Gate
        createScenario(
            id = "1_B",
            title = if (isFr) "Le Portique Ouvert" else "The Open Gate",
            description = if (isFr)
                "Vous arrivez à la station. Le portique est grand ouvert car quelqu'un vient de passer avec une valise. Vous êtes pressé. Un groupe de contrôleurs discute plus loin."
            else
                "You arrive at the station. The gate is wide open because someone just went through with a suitcase. You are in a rush. A group of inspectors is chatting nearby.",
            options = listOf(
                createOption("1B_JUMP", if (isFr) "Profiter de l'ouverture (Passer sans valider)" else "Take the opening (Walk through without scanning)", cost = 60.0, morale = -30),
                createOption("1B_SCAN", if (isFr) "Valider votre passe/téléphone" else "Scan your pass/phone", cost = 0.0, morale = 5)
            ),
            correctOptionIndex = 1
        ),

        // SCENARIO_2: To The Louvre
        createScenario(
            id = "2",
            title = if (isFr) "Direction Le Louvre" else "To The Louvre",
            description = if (isFr)
                "Après les cours, vous voulez voir la Joconde. Vous arrivez à la station. Vous n'avez pas de carte plastique, juste votre téléphone et votre montre connectée."
            else
                "Class is over. You want to see the Mona Lisa. You arrive at the station. You don't have a plastic card, just your phone and smartwatch.",
            options = listOf(
                createOption("2_WATCH", if (isFr) "Valider avec la Montre Connectée" else "Validate with Smartwatch", cost = 2.50, morale = 20),
                createOption("2_COUNTER", if (isFr) "Chercher un guichet pour acheter un ticket carton" else "Find a counter to buy a cardboard ticket", cost = 0.0, morale = -10)
            ),
            correctOptionIndex = 0
        ),

        // SCENARIO_3: Evening Trap
        createScenario(
            id = "3",
            title = if (isFr) "Le Piège de la Soirée" else "The Evening Trap",
            description = if (isFr)
                "Vous êtes à Châtelet. Vos amis sont dans un bar un peu loin du métro. Vous prenez le Métro, puis vous devez monter dans un Bus pour finir le trajet. Vous avez déjà validé votre trajet Métro."
            else
                "You are at Châtelet. Your friends are at a bar far from the metro station. You take the Metro, but then need to hop on a Bus to finish the trip. You already validated your Metro trip.",
            options = listOf(
                createOption("3_TRANSFER", if (isFr) "Utiliser le même ticket (Correspondance)" else "Use the same ticket (Transfer)", cost = 35.0, morale = -20),
                createOption("3_NEW_BUS", if (isFr) "Valider un NOUVEAU ticket Bus (2€)" else "Validate a NEW Bus ticket (€2)", cost = 2.0, morale = 5)
            ),
            correctOptionIndex = 1
        ),

        // SCENARIO_4_B: La Défense choice
        createScenario(
            id = "4_B",
            title = if (isFr) "La Défense: Métro ou RER ?" else "La Défense: Metro or RER?",
            description = if (isFr)
                "Vous allez voir une expo à La Défense (Banlieue proche). Vous pouvez prendre le RER A (rapide) ou le Métro 1 (lent). Vous n'avez PAS de passe Navigo, vous payez au ticket. Que choisissez-vous ?"
            else
                "You are going to an exhibition at La Défense (Near Suburb). You can take the RER A (Fast) or Metro 1 (Slow). You do NOT have a Navigo pass, you are paying per ticket. What do you choose?",
            options = listOf(
                createOption("4B_RER", if (isFr) "Prendre le RER A (Rapide)" else "Take RER A (Fast)", cost = 2.50, morale = 10),
                createOption("4B_METRO1", if (isFr) "Prendre le Métro 1 (Lent mais sûr)" else "Take Metro 1 (Slow but safe)", cost = 2.50, morale = -5)
            ),
            correctOptionIndex = 0
        ),

        // SCENARIO_4: Versailles
        createScenario(
            id = "4",
            title = if (isFr) "L'Excursion Royale" else "The Royal Excursion",
            description = if (isFr)
                "Le lendemain, vous allez à Versailles. C'est loin (Zone 4). Avant, c'était un ticket spécial cher. Vous ouvrez l'appli IDF Mobilités. Que achetez-vous ?"
            else
                "The next day, you go to Versailles. It's far (Zone 4). Before, this was an expensive special ticket. You open the IDF Mobilités app. What do you buy?",
            options = listOf(
                createOption("4_SPECIAL", if (isFr) "Ticket 'Origine-Destination' Spécial" else "Special 'Origin-Destination' Ticket", cost = 0.0, morale = -5),
                createOption("4_STANDARD", if (isFr) "Ticket Rail Standard (2,50€)" else "Standard Rail Ticket (€2.50)", cost = 2.50, morale = 15)
            ),
            correctOptionIndex = 1
        ),

        // SCENARIO_5: Imagine R subscription
        createScenario(
            id = "5",
            title = if (isFr) "Imagine R: Le Choix" else "Imagine R: The Choice",
            description = if (isFr)
                "Vous restez un an. Imagine R coûte 392,30€ par an. C'est une grosse somme... MAIS ! Vous pouvez payer par prélèvement mensuel (en 9 fois). C'est débité petit à petit."
            else
                "You are staying for a year. Imagine R costs €392.30 per year. That's a lot... BUT! You can pay via direct debit in 9 installments (bits). You are debited little by little.",
            options = listOf(
                createOption("5_SUB", if (isFr) "Souscrire à Imagine R (Prélèvement mensuel)" else "Subscribe to Imagine R (Direct Debit)", cost = 38.0, morale = 30),
                createOption("5_PAYG", if (isFr) "Payer au trajet (Trop de paperasse)" else "Pay-As-You-Go (Too much paperwork)", cost = 0.0, morale = -5)
            ),
            correctOptionIndex = 0
        ),

        // SCENARIO_6: Airport return
        createScenario(
            id = "6",
            title = if (isFr) "Retour à la Maison" else "Heading Home",
            description = if (isFr)
                "C'est l'heure de rentrer. Vous devez aller à l'aéroport Charles de Gaulle (CDG). Vous prenez le RER B. Votre ticket à 2,50€ marchera-t-il ?"
            else
                "Time to go home. You need to get to Charles de Gaulle Airport (CDG). You hop on the RER B. Will your €2.50 ticket work?",
            options = listOf(
                createOption("6_YES", if (isFr) "Oui, c'est un ticket Rail !" else "Yes, it is a Rail ticket!", cost = 13.0, morale = 30),
                createOption("6_NO", if (isFr) "Non, il faut un ticket Aéroport (13€)" else "No, need Airport Ticket (€13)", cost = 13.0, morale = 20)
            ),
            correctOptionIndex = 1
        )
    )
}

private val INITIAL_INDEX = 1