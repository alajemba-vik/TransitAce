# Paris Transit Ace

<p align="center">
  <img src="docs/images/app_icon.png" alt="Paris Transit Ace" width="120"/>
</p>

> **A Kotlin Multiplatform survival simulation game for navigating the Paris Metro, powered by AI.**


**Paris Transit Ace** is a cross-platform simulation game where players navigate the chaotic social and logistical challenges of the Paris public transport system. Make strategic decisions to manage your budget, morale, and legal standing while experiencing AI-generated scenarios unique to each playthrough.

---

## Motivation and Background

I volunteered as a student ambassador in my college to help international students navigate the transport system. I quickly noticed a pattern: despite sharing curated videos and articles about how to validate tickets or handle inspections, many students simply didn't watch them. The result? They lost passive information just didn't stick.

I realized that **Gamified Learning** offers a solution that pamphlets can't: it transforms passive reading into active, experiential memory. You might forget a rule you read in a PDF, but you remember the anxiety of getting "caught" by a ticket inspector in a simulation.

**Paris Transit Ace** was born from this insight. It started as a simple web app for my schoolmates, but the overwhelming feedback was, "This would be better on a phone."

I rebuilt the project as a native mobile experience using **Kotlin Multiplatform (KMM)** and **Generative AI** to solve the anxiety of the unknown. By simulating the unwritten rules of the Paris Metro—from "Contrôleurs" to sudden strikes—in a safe environment, players learn practical survival skills before facing the real thing.

---

## Table of Contents
- [Motivation and Background](#motivation-and-background)
- [Features](#features)
- [Supported Platforms](#supported-platforms)
- [Screenshots](#screenshots)
- [Technical Implementation](#technical-implementation)
  - [Architecture](#architecture)
  - [Generative AI](#generative-ai)
  - [Database](#database)
  - [UI & UX](#ui--ux)
- [How to Run](#how-to-run)
  - [Prerequisites](#prerequisites)
  - [API Key Configuration](#api-key-configuration)
  - [Running on Android](#running-on-android)
  - [Running on iOS](#running-on-ios)
- [Chat Commands](#chat-commands)
- [Game Mechanics](#game-mechanics)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Future Steps](#future-steps)
- [License](#license)

---

## Features

### AI-Driven Gameplay
* **Dynamic Scenario Generation:** AI generates unique storylines with branching choices based on your selected simulation type
* **Multiple AI Provider Support:** Supports Google Gemini and Mistral AI APIs
* **Function Calling Integration:** AI can execute game commands directly through natural language
* **Bilingual Support:** Full support for English and French gameplay

### Game Mechanics
* **Player Stats Tracking:**
  - **Budget** - Manage your money wisely
  - **Morale** - Keep your spirits up
  - **Legal Infractions** - Breaking rules have consequences
* **Game Over Conditions:**
  - Budget drops below 0
  - Morale reaches 0
  - 3 or more legal infractions
* **Grading System:** Receive a final grade (A-F) based on your performance

### Persistence & Save System
* **Storyline Management:** Save, load, and delete your storylines

### Chat Commands
Natural language commands to control the game:
* View all saved storylines
* Load specific scenarios by name or ID
* Delete individual or all storylines
* Reset current level
* Clear chat history

---

## Supported Platforms

| Platform | Status |
|----------|--------|
| Android | ✅ Supported |
| iOS | ✅ Supported |

---

## Screenshots

<p align="center">

| Landing Screen | Gameplay | Game Over |
|:---:|:---:|:---:|
| <img src="docs/screenshots/landing.png" width="200"/> | <img src="docs/screenshots/gameplay.png" width="200"/> | <img src="docs/screenshots/gameover.png" width="200"/> |

</p>

---

## Technical Implementation

### Architecture
The app follows **Clean Architecture** principles with Kotlin Multiplatform:

```
composeApp/
└── src/
    ├── commonMain/          # Shared code
    │   ├── domain/          # Business logic, models, use cases
    │   ├── data/            # Repositories, data sources
    │   └── ui/              # Compose UI, ViewModels
    ├── androidMain/         # Android-specific code
    └── iosMain/             # iOS-specific code
```

* **Domain Layer:** Pure Kotlin models (`StoryLine`, `Scenario`, `UserStats`), use cases, and repository interfaces
* **Data Layer:** Repository implementations, local (SQLDelight) and remote (LLM APIs) data sources
* **UI Layer:** Jetpack Compose Multiplatform, organized by feature (`game/`, `chat/`, `home/`, `landing/`)
* **Dependency Injection:** Koin for cross-platform DI

### Generative AI
Supports multiple AI providers:

| Provider | Model | Scenario Generation | Chat & Function Calling |
|----------|-------|:-------------------:|:-----------------------:|
| Google Gemini | gemini-2.5-flash | ✅ | ✅ |
| Mistral AI | mistral-small-latest | ✅ | ✅ |

**Features:**
* **Scenario Generation:** Both providers can generate complete storylines with branching scenarios
* **Response Cleanup:** Automatically strips markdown code fences from AI responses

**Function Calling (Two Modes):**

The game supports executing commands through the chat interface using two different modes:

1. **Local Mode** - Commands are matched locally using regex patterns without calling the AI. This is the preferred mode for common commands because it provides instant responses, saves API costs, and works even when offline or when API keys are unavailable. Supported commands include:
   - `help`, `aide`, `?` - Show available commands
   - `stories`, `list` - Show all saved storylines
   - `load [name]` - Load a specific storyline
   - `delete [name]` - Delete a storyline
   - `delete all` - Delete all storylines
   - `reset` - Reset current level
   - `clear` - Clear chat history

2. **AI Mode** - If the message doesn't match any local command pattern, it falls back to the AI (Gemini or Mistral only) which can interpret natural language and execute function calls. This allows for flexible phrasing like "Can you show me what scenarios I have saved?" or "I want to play the metro strike story".

### Database
* **SQLDelight:** Cross-platform SQLite database
* **Tables:**
  - `StoryEntity` - Saved storylines
  - `ScenarioEntity` - Individual scenarios with options
  - `ChatMessages` - Conversation history
  - `SavedGameState` - Current game progress
  - `Settings` - User preferences

### UI & UX
* **Compose Multiplatform:** 100% shared UI code
* **State Management:** ViewModel + StateFlow pattern
* **Navigation:** Compose Navigation

---

## How to Run

### Prerequisites
* **JDK 17** or newer
* **Android Studio Ladybug** (2024.2.1) or newer
* **Xcode 15+** (for iOS builds on macOS)
* At least one AI API key (Gemini or Mistral)

### API Key Configuration

You'll need at least one API key from the following providers:

**Google Gemini:**
1. Go to [Google AI Studio](https://aistudio.google.com/)
2. Sign in with your Google account
3. Click "Get API Key" → "Create API key"
4. Copy the generated key

**Mistral AI:**
1. Go to [Mistral AI Console](https://console.mistral.ai/)
2. Create an account or sign in
3. Navigate to "API Keys" section
4. Click "Create new key" and copy it

Once you have your key(s), create `local.properties` in the project root:

```properties
# At least one key is required
GEMINI_API_KEY=your_gemini_api_key_here
MISTRALAI_API_KEY=your_mistral_api_key_here
```

Run the application and the build system will inject these keys via `BuildConfig`.

> ⚠️ **Important:** Never commit `local.properties` to version control.

### Running on Android

**Using Android Studio:**
1. Open the project in Android Studio
2. Select the `composeApp` run configuration
3. Choose an Android emulator (API 29+) or physical device
4. Click Run

**Using Terminal:**
```shell
# Build
./gradlew :composeApp:assembleDebug

# Install on connected device
./gradlew :composeApp:installDebug
```

### Running on iOS

**Using Xcode:**
1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select a simulator or physical device
3. Click Run

**Using Android Studio (with KMM plugin):**
1. Select the iOS run configuration
2. Choose a simulator
3. Click Run

---

## Chat Commands

The game supports natural language commands in English and French:

| Command | Examples |
|---------|----------|
| **Help** | `help`, `aide`, `h`, `?` |
| **Show Storylines** | `show me all storylines`, `stories`, `list` |
| **Load Storyline** | `load [name] scenario`, `load 1` |
| **Delete Storyline** | `delete [name]`, `remove storyline 1` |
| **Delete All** | `delete all`, `delete all storylines` |
| **Reset Level** | `reset`, `restart`, `reset this level` |
| **Clear Chat** | `clear`, `cls`, `clear chat` |

---

## Game Mechanics

### Stats
| Stat | Description | Game Over Condition |
|------|-------------|---------------------|
| Budget | Your available money | Below 0 |
| Morale | Your mental state | Reaches 0 |
| Legal Infractions | Rule violations | 3 or more |

### Grading
Your final grade is calculated based on:
* Percentage of budget remaining (50% weight)
* Percentage of morale remaining (50% weight)
* Legal infractions cap your maximum grade:
  - 0 infractions: Can achieve A
  - 1 infraction: Maximum grade is C
  - 2 infractions: Maximum grade is D
  - 3+ infractions: Automatic F

| Grade | Score Range |
|-------|-------------|
| A | 90-100 (0 infractions only) |
| B | 75-89 (0 infractions only) |
| C | 60-74 (or max with 1 infraction) |
| D | 40-59 (or max with 2 infractions) |
| E | 20-39 |
| F | 0-19 or 3+ infractions |

---

## Project Structure

```
UI Layer        →  HomeScreen, GameScreen, LandingScreen
                        ↓ 
                            (shared)
ViewModels      →  UserViewModel, GameViewModel, ChatViewModel
                        ↓
                        
Domain Layer    →  Use Cases, Repository Interfaces

                        ↓
Data Layer      →  LocalDataSource (SQLDelight), LLMRemoteDataSource (AI APIs)
```

---

## Testing

Run all tests:
```shell
./gradlew check
```

Run specific tests:
```shell
# Common unit tests
./gradlew :composeApp:testDebugUnitTest
```

---


## Future Steps

* **Voice Mode:** Audio-to-text for verbal interactions
* **Leaderboard:** Global rankings
* **More Themes:** Additional scenario themes and storylines
* **Sound Effects:** Ambient metro sounds and feedback

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

