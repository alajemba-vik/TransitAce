package com.alajemba.paristransitace.data.local

import com.alajemba.paristransitace.domain.model.GameInventory
import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.ScenarioOption
import com.alajemba.paristransitace.domain.model.ScenarioTheme
import com.alajemba.paristransitace.domain.model.StoryLine
import kotlin.time.Clock

class DefaultScenariosProvider {

    companion object {
        const val DEFAULT_ID = 0L
    }

    fun getDefaultStoryLine(): StoryLine{
        return StoryLine(
            initialBudget = 100.0,
            initialMorale = 30,
            id = DEFAULT_ID,
            title = "Paris Transit Adventure",
            description = "",
            timeCreated =  Clock.System.now().toEpochMilliseconds(),
        )
    }

    fun getDefaultScenarios(isFr: Boolean): List<Scenario> {
        return listOf(
            // SCENARIO 0: Morning in Orly
            createScenario(
                id = "0",
                title = if (isFr) "Le Matin à Orly" else "The Morning in Orly",
                description = if (isFr)
                    "Vous vous réveillez dans votre petit appartement à Orly. Le Uber depuis l'aéroport hier soir vous a coûté cher. Il vous reste 100€. Vous devez vous équiper. Les tickets en carton sont morts. Que faites-vous ?"
                else
                    "You wake up in your tiny apartment in Orly. That Uber from the airport last night drained your bank account. You have €100 left. Cardboard tickets are history. How do you prepare for the day?",
                options = listOf(
                    createOption(
                        id = "0_APP",
                        text = if (isFr) "Installer l'appli 'IDF Mobilités' sur Android/iPhone" else "Install 'IDF Mobilités' App on Android/iPhone",
                        budgetImpact = 0.0,
                        moraleImpact = 10,
                        commentary = if (isFr)
                            "Choix intelligent. Vous paierez au trajet (2€/2,50€). Attention à la batterie !"
                        else
                            "Smart choice. You'll pay per ride (€2/€2.50). Just watch your battery!",
                        inventory = listOf(GameInventory("hasApp", "IDF Mobilités app installed", ""))
                    ),
                    createOption(
                        id = "0_NAVIGO",
                        text = if (isFr) "Acheter un passe Navigo Découverte Hebdo (35€)" else "Buy a Navigo Découverte Weekly (35€)",
                        budgetImpact = -35.0,
                        moraleImpact = 20,
                        commentary = if (isFr)
                            "C'est un investissement (35€), mais vous avez la paix. Tout est inclus (Orly, Versailles, CDG)."
                        else
                            "It's a big investment (€35), but you have peace. Everything is included (Orly, Versailles, CDG).",
                        inventory = listOf(GameInventory("hasWeeklyPass", "Navigo Découverte Weekly pass", ""))
                    )
                ),
                correctOptionID = "0_APP",
                nextScenarioId = "1",
                theme = ScenarioTheme.MORNING
            ),

            // SCENARIO 1: Commute to Class
            createScenario(
                id = "1",
                title = if (isFr) "Trajet vers les Cours" else "Commute to Class",
                description = if (isFr)
                    "Vous devez aller en cours à Villejuif. C'est tout proche, mais vous devez prendre le Métro 14 ou un Bus. Vous hésitez sur le prix. En 2026, tout a changé."
                else
                    "You need to get to class in Villejuif. It's nearby, but requires transit. You could take the new Metro 14 extension or a Bus. You hesitate on the price. In 2026, the rules are strict.",
                options = listOf(
                    createOption(
                        id = "1_BUS",
                        text = if (isFr) "Prendre le Bus (2,00€)" else "Take the Bus (€2.00)",
                        budgetImpact = -2.0,
                        moraleImpact = 5,
                        commentary = if (isFr) "(Inclus dans votre Navigo si vous l'avez) Le bus est lent mais moins cher." else "(Included in your Navigo if you have it) The bus is slow but cheap."
                    ),
                    createOption(
                        id = "1_METRO",
                        text = if (isFr) "Prendre le Métro 14 (2,50€)" else "Take Metro 14 (€2.50)",
                        budgetImpact = -2.50,
                        moraleImpact = 10,
                        commentary = if (isFr) "Rapide et automatique." else "Fast and automated."
                    )
                ),
                correctOptionID = "",
                nextScenarioId = "1_B",
                theme = ScenarioTheme.UNIVERSITY
            ),

            // SCENARIO 1_B: The Open Gate (INFRACTION HERE)
            createScenario(
                id = "1_B",
                title = if (isFr) "Le Portique Ouvert" else "The Open Gate",
                description = if (isFr)
                    "Vous arrivez à la station. Le portique est grand ouvert car quelqu'un vient de passer avec une valise. Vous êtes pressé. Un groupe de contrôleurs discute plus loin."
                else
                    "You arrive at the station. The gate is wide open because someone just went through with a suitcase. You are in a rush. A group of inspectors is chatting nearby.",
                options = listOf(
                    createOption(
                        id = "1B_JUMP",
                        text = if (isFr) "Profiter de l'ouverture (Passer sans valider)" else "Take the opening (Walk through without scanning)",
                        budgetImpact = -60.0,
                        moraleImpact = -30,
                        commentary = if (isFr) "ERREUR FATALE ! Les contrôleurs vous arrêtent. 'Défaut de validation'. Même avec un forfait, il FAUT valider. Amende: 60€." else "FATAL ERROR! Inspectors stop you. 'Failure to validate'. Even with a pass, you MUST scan. Fine: €60.",
                        increaseLegalInfractionsBy = 1
                    ),
                    createOption(
                        id = "1B_SCAN",
                        text = if (isFr) "Valider votre passe/téléphone" else "Scan your pass/phone",
                        budgetImpact = 0.0,
                        moraleImpact = 5,
                        commentary = if (isFr) "Bip. Vous passez légalement. Les contrôleurs sourient à la personne derrière vous qui a essayé de frauder." else "Beep. You pass legally. The inspectors smile at the person behind you who tried to jump it."
                    )
                ),
                correctOptionID = "1B_SCAN",
                nextScenarioId = "2",
                theme = ScenarioTheme.GATE_JUMP
            ),

            // SCENARIO 2: To The Louvre
            createScenario(
                id = "2",
                title = if (isFr) "Direction Le Louvre" else "To The Louvre",
                description = if (isFr)
                    "Après les cours, vous voulez voir la Joconde. Vous arrivez à la station. Vous n'avez pas de carte plastique, juste votre téléphone et votre montre connectée."
                else
                    "Class is over. You want to see the Mona Lisa. You arrive at the station. You don't have a plastic card, just your phone and smartwatch.",
                options = listOf(
                    createOption(
                        id = "2_WATCH",
                        text = if (isFr) "Valider avec la Montre Connectée" else "Validate with Smartwatch",
                        budgetImpact = -2.50,
                        moraleImpact = 20,
                        commentary = if (isFr) "Bip Vert ! Depuis mi-2024, les montres connectées fonctionnent enfin. Vous avez l'air d'un local." else "Green Beep! Since mid-2024, smartwatches finally work. You look like a total local.",
                        inventory = emptyList()
                    ),
                    createOption(
                        id = "2_COUNTER",
                        text = if (isFr) "Chercher un guichet pour acheter un ticket carton" else "Find a counter to buy a cardboard ticket",
                        budgetImpact = 0.0,
                        moraleImpact = -10,
                        commentary = if (isFr) "Sophia rit. 'Les cartons c'est fini mon chou !' Vous perdez du temps." else "Sophia laughs. 'Cardboard is dead, darling!' You waste time."
                    )
                ),
                correctOptionID = "2_WATCH",
                nextScenarioId = "3",
                theme = ScenarioTheme.EIFFEL_TOWER
            ),

            // SCENARIO 3: Evening Trap (INFRACTION HERE)
            createScenario(
                id = "3",
                title = if (isFr) "Le Piège de la Soirée" else "The Evening Trap",
                description = if (isFr)
                    "Vous êtes à Châtelet. Vos amis sont dans un bar un peu loin du métro. Vous prenez le Métro, puis vous devez monter dans un Bus pour finir le trajet. Vous avez déjà validé votre trajet Métro."
                else
                    "You are at Châtelet. Your friends are at a bar far from the metro station. You take the Metro, but then need to hop on a Bus to finish the trip. You already validated your Metro trip.",
                options = listOf(
                    createOption(
                        id = "3_TRANSFER",
                        text = if (isFr) "Utiliser le même ticket (Correspondance)" else "Use the same ticket (Transfer)",
                        budgetImpact = -35.0,
                        moraleImpact = -20,
                        commentary = if (isFr) "AMENDE ! Le grand piège de 2026. Pas de correspondance Rail <-> Bus avec des tickets unitaires." else "FINE! The big trap of 2026. No transfers between Rail and Bus with single tickets.",
                        increaseLegalInfractionsBy = 1
                    ),
                    createOption(
                        id = "3_NEW_BUS",
                        text = if (isFr) "Valider un NOUVEAU ticket Bus (2€)" else "Validate a NEW Bus ticket (€2)",
                        budgetImpact = -2.0,
                        moraleImpact = 5,
                        commentary = if (isFr) "(Inclus dans votre Navigo si vous l'avez) Second paiement accepté. Rail et Bus sont séparés maintenant." else "(Included in your Navigo if you have it) Second payment accepted. Rail and Bus are separate worlds now."
                    )
                ),
                correctOptionID = "3_NEW_BUS",
                nextScenarioId = "4_B",
                theme = ScenarioTheme.BUS_INTERIOR
            ),

            // SCENARIO 4_B: La Défense choice
            createScenario(
                id = "4_B",
                title = if (isFr) "La Défense: Métro ou RER ?" else "La Défense: Metro or RER?",
                description = if (isFr)
                    "Vous allez voir une expo à La Défense (Banlieue proche). Vous pouvez prendre le RER A (rapide) ou le Métro 1 (lent). Vous n'avez PAS de passe Navigo, vous payez au ticket. Que choisissez-vous ?"
                else
                    "You are going to an exhibition at La Défense (Near Suburb). You can take the RER A (Fast) or Metro 1 (Slow). You do NOT have a Navigo pass, you are paying per ticket. What do you choose?",
                options = listOf(
                    createOption(
                        id = "4B_RER",
                        text = if (isFr) "Prendre le RER A (Rapide)" else "Take RER A (Fast)",
                        budgetImpact = -2.50,
                        moraleImpact = 10,
                        commentary = if (isFr) "Surprise ! Même en banlieue, c'est 2,50€ tarif unique maintenant. Vous gagnez du temps." else "Surprise! Even for suburbs, it's €2.50 flat fare now. You saved time."
                    ),
                    createOption(
                        id = "4B_METRO1",
                        text = if (isFr) "Prendre le Métro 1 (Lent mais sûr)" else "Take Metro 1 (Slow but safe)",
                        budgetImpact = -2.50,
                        moraleImpact = -5,
                        commentary = if (isFr) "C'est le même prix (2,50€) ! Mais le métro s'arrête partout. Vous arrivez en retard." else "It's the same price (€2.50)! But the metro stops everywhere. You arrive late."
                    )
                ),
                correctOptionID ="4B_RER",
                nextScenarioId = "4",
                theme = ScenarioTheme.SUBURBAN_STATION
            ),

            // SCENARIO 4: Versailles
            createScenario(
                id = "4",
                title = if (isFr) "L'Excursion Royale" else "The Royal Excursion",
                description = if (isFr)
                    "Le lendemain, vous allez à Versailles. C'est loin (Zone 4). Avant, c'était un ticket spécial cher. Vous ouvrez l'appli IDF Mobilités. Que achetez-vous ?"
                else
                    "The next day, you go to Versailles. It's far (Zone 4). Before, this was an expensive special ticket. You open the IDF Mobilités app. What do you buy?",
                options = listOf(
                    createOption(
                        id = "4_SPECIAL",
                        text = if (isFr) "Ticket 'Origine-Destination' Spécial" else "Special 'Origin-Destination' Ticket",
                        budgetImpact = 0.0,
                        moraleImpact = -5,
                        commentary = if (isFr) "L'appli ne propose pas ça. Vous cherchez pour rien. Les zones n'existent plus pour les tickets !" else "The app doesn't show this. You look for nothing. Zones don't exist for tickets anymore!"
                    ),
                    createOption(
                        id = "4_STANDARD",
                        text = if (isFr) "Ticket Rail Standard (2,50€)" else "Standard Rail Ticket (€2.50)",
                        budgetImpact = -2.50,
                        moraleImpact = 15,
                        commentary = if (isFr) "Victoire ! Le ticket à 2,50€ (ou votre Navigo) vous emmène PARTOUT en Île-de-France. Versailles, Disney..." else "Victory! The €2.50 ticket (or your Navigo) takes you ANYWHERE in Ile-de-France. Versailles, Disney..."
                    )
                ),
                correctOptionID = "4_STANDARD",
                nextScenarioId = "5",
                theme = ScenarioTheme.SUBURBAN_STATION
            ),

            // SCENARIO 5: Imagine R subscription
            createScenario(
                id = "5",
                title = if (isFr) "Imagine R: Le Choix" else "Imagine R: The Choice",
                description = if (isFr)
                    "Vous restez un an. Imagine R coûte 392,30€ par an. C'est une grosse somme... MAIS ! Vous pouvez payer par prélèvement mensuel (en 9 fois). C'est débité petit à petit."
                else
                    "You are staying for a year. Imagine R costs €392.30 per year. That's a lot... BUT! You can pay via direct debit in 9 installments (bits). You are debited little by little.",
                options = listOf(
                    createOption(
                        id = "5_SUB",
                        text = if (isFr) "Souscrire à Imagine R (Prélèvement mensuel)" else "Subscribe to Imagine R (Direct Debit)",
                        budgetImpact = -38.0,
                        moraleImpact = 30,
                        commentary = if (isFr) "Dossier envoyé ! Vous paierez environ 38€/mois pendant 9 mois. Liberté totale (Zones 1-5) !" else "Paperwork sent! You'll pay about €38/month for 9 months. Total freedom (Zones 1-5)!",
                        inventory = listOf(GameInventory("hasWeeklyPass", "Imagine R subscription (treated like weekly pass)", ""))
                    ),
                    createOption(
                        id = "5_PAYG",
                        text = if (isFr) "Payer au trajet (Trop de paperasse)" else "Pay-As-You-Go (Too much paperwork)",
                        budgetImpact = 0.0,
                        moraleImpact = -5,
                        commentary = if (isFr) "Dommage. Imagine R est imbattable pour les étudiants. Vous paierez plus cher à la longue." else "Too bad. Imagine R is unbeatable for students. You'll pay more in the long run."
                    )
                ),
                correctOptionID = "5_SUB",
                nextScenarioId = "6",
                theme = ScenarioTheme.UNIVERSITY
            ),

            // SCENARIO 6: Airport return
            createScenario(
                id = "6",
                title = if (isFr) "Retour à la Maison" else "Heading Home",
                description = if (isFr)
                    "C'est l'heure de rentrer. Vous devez aller à l'aéroport Charles de Gaulle (CDG). Vous prenez le RER B. Votre ticket à 2,50€ marchera-t-il ?"
                else
                    "Time to go home. You need to get to Charles de Gaulle Airport (CDG). You hop on the RER B. Will your €2.50 ticket work?",
                options = listOf(
                    createOption(
                        id = "6_YES",
                        text = if (isFr) "Oui, c'est un ticket Rail !" else "Yes, it is a Rail ticket!",
                        budgetImpact = 0.0,
                        moraleImpact = 30,
                        commentary = if (isFr) "OUI ! Le Navigo (ou Imagine R) INCLUT les aéroports. Vous passez comme un roi. Bon vol !" else "YES! The Navigo (or Imagine R) INCLUDES airports. You pass like a king. Have a safe flight!"
                    ),
                    createOption(
                        id = "6_NO",
                        text = if (isFr) "Non, il faut un ticket Aéroport (13€)" else "No, need Airport Ticket (€13)",
                        budgetImpact = -13.0,
                        moraleImpact = 20,
                        commentary = if (isFr) "Correct. Sans forfait, c'est 13€ tarif unique. Vous évitez l'amende." else "Correct. Without a pass, it's €13 flat. You avoid the fine."
                    )
                ),
                correctOptionID = "6_NO",
                nextScenarioId = null,
                theme = ScenarioTheme.AIRPORT
            )
        )
    }


    private fun createOption(
        id: String,
        text: String,
        budgetImpact: Double = 0.0,
        moraleImpact: Int = 0,
        commentary: String = "",
        inventory: List<GameInventory> = emptyList(),
        increaseLegalInfractionsBy: Int = 0
    ) = ScenarioOption(
        id = id,
        text = text,
        budgetImpact = budgetImpact,
        moraleImpact = moraleImpact,
        commentary = commentary,
        inventory = inventory,
        increaseLegalInfractionsBy = increaseLegalInfractionsBy
    )

    private fun createScenario(
        id: String,
        title: String,
        description: String,
        options: List<ScenarioOption>,
        correctOptionID: String,
        nextScenarioId: String? = null,
        theme: ScenarioTheme
    ) = Scenario(
        id = id,
        title = title,
        description = description,
        options = options,
        correctOptionId = correctOptionID,
        nextScenarioId = nextScenarioId,
        scenarioTheme = theme
    )
}