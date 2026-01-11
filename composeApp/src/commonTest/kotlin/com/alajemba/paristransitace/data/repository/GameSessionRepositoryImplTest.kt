package com.alajemba.paristransitace.data.repository

import com.alajemba.paristransitace.domain.model.Scenario
import com.alajemba.paristransitace.domain.model.ScenarioOption
import com.alajemba.paristransitace.domain.model.ScenarioTheme
import com.alajemba.paristransitace.domain.model.StoryLine
import com.alajemba.paristransitace.domain.model.UserStats
import com.alajemba.paristransitace.domain.repository.GameSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameSessionRepositoryImplTest {

    private lateinit var repository: FakeGameSessionRepository

    @BeforeTest
    fun setup() {
        repository = FakeGameSessionRepository()
    }

    @Test
    fun `setNewSession should update currentStoryLine and scenarios`() {
        val storyLine = createTestStoryLine()
        val scenarios = createTestScenarios()

        repository.setNewSession(storyLine, scenarios)

        assertEquals(storyLine, repository.currentStoryLine)
        assertEquals(scenarios, repository.scenarios)
    }

    @Test
    fun `setNewSession should clear current scenario`() = runTest {
        val storyLine = createTestStoryLine()
        val scenarios = createTestScenarios()

        repository.setNewSession(storyLine, scenarios)
        repository.nextScenario()

        // Set new session should clear current scenario
        repository.setNewSession(storyLine, scenarios)

        assertNull(repository.currentScenario.value)
    }



    @Test
    fun `nextScenario should return first scenario when no current scenario`() = runTest {
        val scenarios = createTestScenarios()
        repository.setNewSession(createTestStoryLine(), scenarios)

        val result = repository.nextScenario()

        assertTrue(result)
        assertEquals(scenarios[0].id, repository.currentScenario.value?.id)
        assertEquals(0, repository.currentScenario.value?.currentIndexInGame)
    }

    @Test
    fun `nextScenario should advance to next scenario`() = runTest {
        val scenarios = createTestScenarios()
        repository.setNewSession(createTestStoryLine(), scenarios)

        repository.nextScenario() // First
        repository.nextScenario() // Second

        assertEquals(scenarios[1].id, repository.currentScenario.value?.id)
        assertEquals(1, repository.currentScenario.value?.currentIndexInGame)
    }

    @Test
    fun `nextScenario should return false when no more scenarios`() = runTest {
        val scenarios = createTestScenarios()
        repository.setNewSession(createTestStoryLine(), scenarios)

        repository.nextScenario() // 0
        repository.nextScenario() // 1
        repository.nextScenario() // 2
        val result = repository.nextScenario() // Beyond

        assertFalse(result)
    }

    @Test
    fun `nextScenario should return false when scenarios list is empty`() {
        repository.setNewSession(createTestStoryLine(), emptyList())

        val result = repository.nextScenario()

        assertFalse(result)
    }



    @Test
    fun `scenarioProgress should be 0 when no current scenario`() = runTest {
        repository.setNewSession(createTestStoryLine(), createTestScenarios())

        val progress = repository.scenarioProgress.first()

        assertEquals(0f, progress)
    }

    @Test
    fun `scenarioProgress should reflect current position`() = runTest {
        val scenarios = createTestScenarios() // 3 scenarios
        repository.setNewSession(createTestStoryLine(), scenarios)

        repository.nextScenario() // index 0
        val progress = repository.scenarioProgress.first()

        assertEquals(1f / 3f, progress)
    }

    @Test
    fun `scenarioProgressText should show correct format`() = runTest {
        val scenarios = createTestScenarios()
        repository.setNewSession(createTestStoryLine(), scenarios)

        repository.nextScenario()
        repository.nextScenario() // index 1

        val progressText = repository.scenarioProgressText.first()

        assertEquals("1/3", progressText)
    }



    @Test
    fun `saveGameState should save current state`() = runTest {
        val storyLine = createTestStoryLine()
        val scenarios = createTestScenarios()
        val userStats = UserStats(budget = 75.5, morale = 80, legalInfractionsCount = 2)

        repository.setNewSession(storyLine, scenarios)
        repository.nextScenario()
        repository.nextScenario() // index 1

        repository.saveGameState(userStats)

        val savedState = repository.lastSavedUserStats
        assertNotNull(savedState)
        assertEquals(75.5, savedState.budget)
        assertEquals(80, savedState.morale)
        assertEquals(2, savedState.legalInfractionsCount)
        assertEquals(1, repository.lastSavedScenarioIndex)
    }

    @Test
    fun `saveGameState should save with index -1 when no current scenario`() = runTest {
        val storyLine = createTestStoryLine()
        val userStats = UserStats()

        repository.setNewSession(storyLine, createTestScenarios())
        repository.saveGameState(userStats)

        assertEquals(-1, repository.lastSavedScenarioIndex)
    }



    @Test
    fun `loadSavedGame should return null when no saved state exists`() {
        repository.savedUserStats = null

        val result = repository.loadSavedGame()

        assertNull(result)
    }

    @Test
    fun `loadSavedGame should return null when story cannot be loaded`() {
        repository.savedUserStats = UserStats()
        repository.savedStoryId = 999L
        repository.canLoadStory = false

        val result = repository.loadSavedGame()

        assertNull(result)
    }

    @Test
    fun `loadSavedGame should restore user stats correctly`() {
        val savedStats = UserStats(budget = 50.0, morale = 60, legalInfractionsCount = 3)
        repository.savedUserStats = savedStats
        repository.savedStoryId = 1L
        repository.savedScenarioIndex = 1
        repository.canLoadStory = true
        repository.scenariosForLoading = createTestScenarios()

        val result = repository.loadSavedGame()

        assertNotNull(result)
        assertEquals(50.0, result.budget)
        assertEquals(60, result.morale)
        assertEquals(3, result.legalInfractionsCount)
    }

    @Test
    fun `loadSavedGame should restore scenario position`() = runTest {
        repository.savedUserStats = UserStats()
        repository.savedStoryId = 1L
        repository.savedScenarioIndex = 2
        repository.canLoadStory = true
        repository.scenariosForLoading = createTestScenarios()

        repository.loadSavedGame()

        val currentScenario = repository.currentScenario.value
        assertNotNull(currentScenario)
        assertEquals(2, currentScenario.currentIndexInGame)
    }



    @Test
    fun `loadStoryForSession should return false when scenarios are empty`() {
        repository.scenariosForLoading = emptyList()
        repository.storyLineForLoading = createTestStoryLine()

        val result = repository.loadStoryForSession(1L)

        assertFalse(result)
    }

    @Test
    fun `loadStoryForSession should return false when story not found`() {
        repository.scenariosForLoading = createTestScenarios()
        repository.storyLineForLoading = null

        val result = repository.loadStoryForSession(1L)

        assertFalse(result)
    }

    @Test
    fun `loadStoryForSession should return true and set session on success`() {
        val storyLine = createTestStoryLine()
        val scenarios = createTestScenarios()
        repository.scenariosForLoading = scenarios
        repository.storyLineForLoading = storyLine

        val result = repository.loadStoryForSession(1L)

        assertTrue(result)
        assertEquals(storyLine, repository.currentStoryLine)
        assertEquals(scenarios, repository.scenarios)
    }



    @Test
    fun `clearSession should reset all session state`() = runTest {
        repository.setNewSession(createTestStoryLine(), createTestScenarios())
        repository.nextScenario()

        repository.clearSession()

        assertEquals(StoryLine.EMPTY, repository.currentStoryLine)
        assertTrue(repository.scenarios.isEmpty())
        assertNull(repository.currentScenario.value)
    }



    @Test
    fun `deleteSavedGame should clear saved state`() {
        repository.savedUserStats = UserStats()

        repository.deleteSavedGame()

        assertNull(repository.savedUserStats)
    }



    @Test
    fun `clearAll should delete saved game and clear session`() = runTest {
        repository.savedUserStats = UserStats()
        repository.setNewSession(createTestStoryLine(), createTestScenarios())
        repository.nextScenario()

        repository.clearAll()

        assertNull(repository.savedUserStats)
        assertEquals(StoryLine.EMPTY, repository.currentStoryLine)
        assertTrue(repository.scenarios.isEmpty())
        assertNull(repository.currentScenario.value)
    }



    private fun createTestStoryLine() = StoryLine(
        id = 1L,
        title = "Test Story",
        description = "A test story",
        timeCreated = 0L,
        initialBudget = 100.0,
        initialMorale = 50
    )

    private fun createTestScenarios(): List<Scenario> = listOf(
        createScenario("scenario_1", "Scenario 1"),
        createScenario("scenario_2", "Scenario 2"),
        createScenario("scenario_3", "Scenario 3")
    )

    private fun createScenario(id: String, title: String) = Scenario(
        id = id,
        title = title,
        description = "Description for $title",
        options = listOf(
            ScenarioOption(id = "opt_1", text = "Option 1"),
            ScenarioOption(id = "opt_2", text = "Option 2")
        ),
        correctOptionId = "opt_1",
        nextScenarioId = null,
        currentIndexInGame = -1,
        scenarioTheme = ScenarioTheme.MORNING
    )


}

