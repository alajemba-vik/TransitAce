package com.alajemba.paristransitace.data.repository

import com.alajemba.paristransitace.data.local.LocalDataSource
import com.alajemba.paristransitace.domain.repository.GameSessionRepository
import com.alajemba.paristransitace.domain.repository.UserRepository

class UserRepositoryImpl(
    private val localDataSource: LocalDataSource
) : UserRepository {
    override fun hasSavedGame(): Boolean = localDataSource.hasSavedGame()
}