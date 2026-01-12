package com.alajemba.paristransitace.domain.usecase.game

import com.alajemba.paristransitace.domain.model.GameReport
import com.alajemba.paristransitace.utils.debugLog

class CalculateFinalGradeUseCase {
    operator fun invoke(
        budgetRemaining: Double,
        moraleRemaining: Int,
        legalInfractionsCount: Int,
        initialMorale: Int,
        initialBudget: Double
    ): GameReport {

        val budgetPercentage = if (initialBudget > 0) {
            (budgetRemaining / initialBudget * 100).coerceIn(-100.0, 100.0)
        } else 0.0

        val moralePercentage = if (initialMorale > 0) {
            (moraleRemaining.toDouble() / initialMorale * 100).coerceIn(0.0, 100.0)
        } else 0.0

        val infractionPenalty = legalInfractionsCount * 10
        val totalScore = (budgetPercentage * 0.5 + moralePercentage * 0.5 - infractionPenalty).coerceIn(0.0, 100.0)

        debugLog("Grade calculation: budgetRemaining=$budgetRemaining, initialBudget=$initialBudget, budgetPercentage=$budgetPercentage, moralePercentage=$moralePercentage, totalScore=$totalScore")

        val grade = when {
            legalInfractionsCount >= 3 -> 'F'
            budgetPercentage <= 0 || moralePercentage <= 0 -> 'F'
            legalInfractionsCount == 2 -> minOf('D', getGradeFromScore(totalScore))
            legalInfractionsCount == 1 -> minOf('C', getGradeFromScore(totalScore))
            else -> getGradeFromScore(totalScore)
        }

        return GameReport(
            grade = grade.toString(),
            summary = getGradeDescription(grade, legalInfractionsCount)
        )
    }

    private fun getGradeFromScore(totalScore: Double): Char {
        return when {
            totalScore >= 90 -> 'A'
            totalScore >= 75 -> 'B'
            totalScore >= 60 -> 'C'
            totalScore >= 40 -> 'D'
            totalScore >= 20 -> 'E'
            else -> 'F'
        }
    }

    private fun getGradeDescription(grade: Char, legalInfractionsCount: Int): String {
        return when (grade) {
            'A' -> "Incredible. You mastered the system better than a local."
            'B' -> "Solid performance. You survived France with style."
            'C' -> "You made it through, but it wasn't pretty."
            'D' -> "Barely alive. You might need a vacation."
            'E' -> "Technically you survived, but at what cost?"
            'F' -> if (legalInfractionsCount >= 3) "Deported. Three legal infractions and you are out."
                   else "Paris chewed you up and spit you out."
            else -> "Unknown fate."
        }
    }
}