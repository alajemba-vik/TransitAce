package com.alajemba.paristransitace.ui.model

sealed class UIDataState {
    object Idle : UIDataState()
    object Loading : UIDataState()
    sealed class Success : UIDataState() {
        object ScenariosGenerated: Success()

        companion object: Success()
    }

    sealed class Error : UIDataState() {
        object NetworkError : UIDataState.Error()
        object AIError : Error()


    }
}

