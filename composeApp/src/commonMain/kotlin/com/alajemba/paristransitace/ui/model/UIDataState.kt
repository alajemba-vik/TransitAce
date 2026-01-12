package com.alajemba.paristransitace.ui.model

sealed class UIDataState {
    object Idle : UIDataState()
    object Loading : UIDataState()
    sealed class Success : UIDataState() {
        object ScenariosGenerated: Success()

        object StorylineLoaded: Success()

        companion object: Success()
    }

    sealed class Error: UIDataState() {
        data object NetworkError : Error()
        data class AIError(
            val message: String
        ) : Error()
    }
}

