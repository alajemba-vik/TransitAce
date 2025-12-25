package com.alajemba.paristransitace.model.ai_script


object AIScript {

    // Maps the Stage to a Pair of Strings: (English, French)
    private val script = mapOf(
        DiscussionStage.LANGUAGE to (
                "English or Français? Keep it simple, kid." to
                        "English ou Français ? Restons simples, gamin."
                ),
        DiscussionStage.NAME to (
                "Great. English it is. And what is your name, you poor soul?" to
                        "Super. Français it is. Et quel est ton nom, pauvre âme ?"
                ),
        DiscussionStage.SIMULATION_TYPE to (
                "Alright %s, last step. Select your simulation type: Default or Custom?" to
                        "D'accord %s, dernière étape. Sélectionnez votre type de simulation : Par défaut ou Personnalisé ?"
                ),
        DiscussionStage.GENERATING to (
                "Simulation selected. Generating scenarios..." to
                        "Simulation sélectionnée. Génération des scénarios..."
                ),
        DiscussionStage.FAILURE to (
                "Hmmm. That's unusual. I couldn't get things ready for you. Oh well... delays are normal in Paris.\nShould A) I try again, or B) would you prefer to just do the default scenario, little one?" to
                        "Hmmm. C'est inhabituel. Je n'ai pas réussi à préparer le terrain. Enfin bon... les retards, c'est la norme à Paris. A) Je réessaie, ou B) tu préfères le scénario par défaut, mon petit ?"
                ),
        DiscussionStage.COMPLETE to (
                "Okay, I am done. Welcome %s, to the city of Paris. I wish you well on your journey. You'll need all the luck you can get." to
                        "D'accord, j'ai terminé. Bienvenue %s, dans la ville de Paris. Je vous souhaite bonne chance pour votre voyage. Vous aurez besoin de toute la chance possible."
                ),
        DiscussionStage.UNKNOWN to (
                "I refuse to respond to that, kid." to
                        "Je refuse de répondre à cela, gamin."
                )
    )

    /**
     * Retrieves the correct line based on stage and language.
     * @param args Optional arguments to replace %s placeholders (e.g., the user's name).
     */
    fun getLine(stage: DiscussionStage, isEnglish: Boolean, vararg args: Any): String {
        val (en, fr) = script[stage] ?: script[DiscussionStage.UNKNOWN]!!
        val rawTemplate = if (isEnglish) en else fr

        // This safely formats the string if arguments (like name) are provided
        return try {
            rawTemplate.format(*args)
        } catch (e: Exception) {
            rawTemplate // Fallback if formatting fails
        }
    }
}

private fun String.format(vararg args: Any): String {
    var result = this
    args.forEach { arg ->
        // replaceFirst ensures we fill the placeholders in order (Name first, then Date, etc.)
        result = result.replaceFirst("%s", arg.toString())
    }
    return result
}

/*
* Move to chatviewmodel once working...
* fun setupGame(updatedGameSetup: GameSetup) {
        val stage = updatedGameSetup.currentStage

        // 1. Get the text from the Script Object
        val chatMessage = AIScript.getLine(
            stage = stage,
            isEnglish = updatedGameSetup.isEnglish,
            updatedGameSetup.name // Pass name as an argument for %s
        )

        // 2. Handle Side Effects (UI Loading States)
        when (stage) {
            DiscussionStage.GENERATING -> {
                if (updatedGameSetup.isCustomSimulation) {
                    _uiDataState.value = UIDataState.Loading
                }
            }
            DiscussionStage.FAILURE -> {
                _uiDataState.value = UIDataState.Idle
            }
            else -> { /* No side effects */ }
        }

        // 3. Send Message
        attachNewMessage(chatMessage, ChatMessageSender.AI)
    }
* */