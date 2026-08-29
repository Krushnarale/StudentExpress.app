# StudentExpress merge

Base project: `studentexpress(2)` (updated UI)
Backend source: `StudentExpress.app`

The updated UI remains the primary `view` implementation. Backend packages were added under `config`, `controller`, `dao`, and backend services/utilities. Shared model classes were made backend-compatible while preserving the UI rental-duration fields in `RoomItem`.

Authentication UI now calls `AuthController` for login/registration and syncs the authenticated user into the UI `DataRepository`.

Firebase initialization/seed is called from `Main.initApp()` before the UI starts.

Before pushing to GitHub, remove/secure `src/main/resources/firebase/serviceAccount.json` and use a safe secret-management approach. If this credential has been exposed, rotate it in Firebase/Google Cloud.

Run from the project root:

    mvn clean javafx:run
