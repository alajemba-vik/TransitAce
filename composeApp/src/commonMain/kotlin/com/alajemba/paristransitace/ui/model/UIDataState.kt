package com.alajemba.paristransitace.ui.model

import com.alajemba.paristransitace.network.models.ChatResult

sealed class UIDataState {
    object Idle : UIDataState()
    object Loading : UIDataState()
    sealed class Success : UIDataState() {
        object ScenariosGenerated: Success()
        data class ChatResponse(
            val command: ChatResult.ExecuteCommand,
            val sentMessage: String
        ): Success()

        companion object: Success()
    }

    sealed class Error : UIDataState() {
        object NetworkError : UIDataState.Error()
        object AIError : Error()


    }
}

