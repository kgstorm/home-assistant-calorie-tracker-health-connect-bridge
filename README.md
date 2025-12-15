# Home Assistant Calorie Tracker Health Connect Bridge

A bridge application that syncs calorie data from Home Assistant's calorie tracker integration to Android Health Connect.

## Features

- 🔄 Sync calorie intake data from Home Assistant to Health Connect
- 🔐 Secure permission handling for Health Connect
- ⚙️ Easy configuration with Home Assistant URL and access token
- 🔁 Automatic periodic background sync
- 📱 Clean Material Design 3 UI

## Requirements

- Android 8.0 (API 26) or higher
- Android Health Connect installed and configured
- Home Assistant instance with calorie tracker sensor
- Home Assistant long-lived access token

## Setup

### 1. Home Assistant Configuration

Ensure you have a calorie tracker sensor in Home Assistant with the entity ID `sensor.calorie_tracker`.

Generate a long-lived access token:
1. Go to your Home Assistant profile
2. Scroll to "Long-Lived Access Tokens"
3. Click "Create Token"
4. Save the token securely

### 2. Build the App

```bash
# Clone the repository
git clone https://github.com/kgstorm/home-assistant-calorie-tracker-health-connect-bridge.git
cd home-assistant-calorie-tracker-health-connect-bridge

# Build with Gradle
./gradlew assembleDebug

# The APK will be in app/build/outputs/apk/debug/
```

### 3. Install and Configure

1. Install the APK on your Android device
2. Install Health Connect from Google Play Store if not already installed
3. Open the app
4. Grant Health Connect permissions
5. Enter your Home Assistant URL (e.g., `https://your-home-assistant.com`)
6. Enter your long-lived access token
7. Save settings
8. Tap "Sync Now" to perform the first sync

## How It Works

1. The app fetches calorie data from your Home Assistant `sensor.calorie_tracker` entity
2. It writes the calorie data to Health Connect as nutrition records
3. Background sync runs every 15 minutes to keep data up-to-date
4. All data is stored securely using Android DataStore

## Permissions

The app requires the following permissions:

- **Internet**: To communicate with Home Assistant
- **Health Connect Read/Write Nutrition**: To sync calorie data

## Project Structure

```
app/src/main/java/com/kgstorm/healthconnectbridge/
├── MainActivity.kt                 # Main UI and user interactions
├── HealthConnectManager.kt        # Health Connect API wrapper
├── PreferencesManager.kt          # Settings storage
├── api/
│   ├── HomeAssistantApi.kt       # Retrofit API interface
│   └── HomeAssistantClient.kt    # API client factory
├── models/
│   └── CalorieData.kt            # Data models
└── sync/
    ├── CalorieSyncService.kt     # Core sync logic
    └── CalorieSyncWorker.kt      # Background sync worker
```

## Technologies Used

- **Kotlin**: Primary programming language
- **AndroidX Health Connect SDK**: For Health Connect integration
- **Retrofit + OkHttp**: For Home Assistant API communication
- **WorkManager**: For background sync scheduling
- **DataStore**: For secure settings storage
- **Material Design 3**: For modern UI components

## Development

### Building

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Code Style

This project follows the official Kotlin coding conventions.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Troubleshooting

### Health Connect Not Available
- Ensure your device runs Android 8.0 or higher
- Install Health Connect from the Google Play Store

### Sync Fails
- Verify your Home Assistant URL is correct and accessible
- Check that your access token is valid
- Ensure the `sensor.calorie_tracker` entity exists in your Home Assistant

### Permissions Denied
- Go to Android Settings > Health Connect
- Find this app and grant nutrition permissions

## Support

For issues and questions, please use the [GitHub Issues](https://github.com/kgstorm/home-assistant-calorie-tracker-health-connect-bridge/issues) page.
