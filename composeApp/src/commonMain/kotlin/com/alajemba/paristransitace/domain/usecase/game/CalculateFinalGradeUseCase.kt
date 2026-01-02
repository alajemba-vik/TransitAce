package com.alajemba.paristransitace.domain.usecase.game

import com.alajemba.paristransitace.domain.model.GameReport

class CalculateFinalGradeUseCase {
    operator fun invoke(
        budgetRemaining: Double,
        moraleRemaining: Int,
        legalInfractionsCount: Int
    ): GameReport {
        val infractionPenalty = legalInfractionsCount * 20
        val totalScore = budgetRemaining + moraleRemaining - infractionPenalty

        val grade = when {
            legalInfractionsCount >= 3 -> 'F'
            budgetRemaining <= -20.0 -> 'F'
            totalScore >= 120 -> 'A'
            totalScore >= 90 -> 'B'
            totalScore >= 60 -> 'C'
            totalScore >= 30 -> 'D'
            budgetRemaining >= 0 -> 'E'
            else -> 'F'
        }

        return GameReport(
            grade = grade.toString(),
            summary = getGradeDescription(grade, legalInfractionsCount)
        )
    }

    private fun getGradeDescription(grade: Char, legalInfractionsCount: Int): String {
        return when (grade) {
            'A' -> "Incredible. You mastered the system better than a local."
            'B' -> "Solid performance. You survived Paris with style."
            'C' -> "You made it through, but it wasn't pretty."
            'D' -> "Barely alive. You need a vacation from your vacation."
            'E' -> "Technically you survived, but at what cost?"
            'F' -> if (legalInfractionsCount >= 3) "Deported. Three strikes and you are out."
                   else "Paris chewed you up and spit you out."
            else -> "Unknown fate."
        }
    }
}