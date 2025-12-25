import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    // KMM - Koin Call
    init() {
        KoiniOSModuleKt.doInitKoin()
        // Sleep for 1.5 seconds to allow users see our pretty splash screen
        Thread.sleep(forTimeInterval: 1.0)
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
