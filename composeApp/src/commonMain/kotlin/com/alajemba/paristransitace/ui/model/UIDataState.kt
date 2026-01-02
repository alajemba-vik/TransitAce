package com.alajemba.paristransitace.ui.model

sealed class UIDataState {
    object Idle : UIDataState()
    object Loading : UIDataState()
    sealed class Success : UIDataState() {
        object ScenariosGenerated: Success()

        companion object: Success()
    }

    sealed class Error : UIDataState() {
        data object NetworkError : Error()
        data object AIError : Error()
    }
}

