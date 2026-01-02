package com.alajemba.paristransitace.ui.landing.components


sealed class LandingAnimationStage(
    val stage: Int,
    val currentStageDelayMillis: Long,
    val typeWriterCharDelay : Long = 0L,
) {

    object Initial : LandingAnimationStage(
        stage = 0,
        currentStageDelayMillis = 500
    )

    sealed class TypeWriterStage(
        val text: String,
        stage: Int,
        typeWriterCharDelay: Long,
    ) : LandingAnimationStage(
        stage = stage,
        currentStageDelayMillis = text.length * typeWriterCharDelay,
        typeWriterCharDelay = typeWriterCharDelay,
    )

    class WakeUp(
        text: String
    ) : TypeWriterStage(
        stage = WAKE_UP_STAGE,
        typeWriterCharDelay = TYPEWRITER_CHAR_DELAY,
        text = text
    ) {
        companion object Companion {
            const val TYPEWRITER_CHAR_DELAY = 100L
            private const val WAKE_UP_STAGE = 1

            fun isAfterOrOnWakeUpStage(stage: LandingAnimationStage): Boolean {
                return stage.stage >= WAKE_UP_STAGE
            }
        }
    }

    class Introduction(
        text: String
    ) : TypeWriterStage(
        stage = INTRODUCTION_STAGE,
        typeWriterCharDelay = TYPEWRITER_CHAR_DELAY,
        text = text
    ) {
        companion object Companion {
            const val TYPEWRITER_CHAR_DELAY = 30L
            private const val INTRODUCTION_STAGE = 2

            fun isAfterOrOnIntroductionStage(stage: LandingAnimationStage): Boolean {
                return stage.stage >= INTRODUCTION_STAGE
            }

        }
    }

    object Story : LandingAnimationStage(
        stage = 3,
        currentStageDelayMillis = 1500L,
    )

    object Instructions: LandingAnimationStage(
        stage = 4,
        currentStageDelayMillis = 1000L,
    )

    object ContinueToGame : LandingAnimationStage(
        stage = 5,
        currentStageDelayMillis = 1000L,
    )

    /**
     * Compares two [LandingAnimationStage] based on their stage order.
     * is less than, equal to, or greater than the specified [other] stage.
     *
     * If this stage comes before [other], returns a negative integer.
     * If both stages are the same, returns zero.
     * If this stage comes after [other], returns a positive integer.
     *
     * @return A negative integer, zero, or a positive integer as this stage
     */
    operator fun compareTo(other: LandingAnimationStage): Int {
        return this.stage - other.stage
    }
}
