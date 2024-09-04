
# TPL Maps SDK Sample with Jetpack Compose

This is a sample Android application demonstrating the integration of the TPL Maps SDK using Jetpack Compose. The project showcases basic functionalities such as map view, search, and reverse geocoding using the TPL Maps SDK.

## Features

- **Map View Integration**: Display an interactive map using the TPL Maps SDK.
- **Search Places**: Search for places using the TPL Maps SDK and display the results in a list.
- **Reverse Geocoding**: Automatically fetch and display the address based on the map's camera position.
- **Custom UI with Jetpack Compose**: Implement UI components like search bars, zoom controls, and floating action buttons using Jetpack Compose.

## Screenshots

![image](https://github.com/user-attachments/assets/78eea83e-240b-4c96-83d5-d9dcb90f241f)
![image](https://github.com/user-attachments/assets/bd1e8c75-ffc1-44d4-9f73-2adc40f6fa2d)


## Requirements

- **Android SDK**: 24 or higher
- **Build Tools**: 34.0.0
- **Kotlin**: 1.9.0
- **Gradle**: 8.7

## Dependencies

The project uses the following libraries and dependencies:

```gradle
dependencies {
    implementation "androidx.core:core-ktx:1.13.1"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.8.4"
    implementation "androidx.activity:activity-compose:1.9.1"
    implementation platform("androidx.compose:compose-bom:2024.04.01")
    implementation "androidx.compose.ui:ui"
    implementation "androidx.compose.ui:ui-graphics"
    implementation "androidx.compose.ui:ui-tooling-preview"
    implementation "androidx.compose.material3:material3"
    implementation "com.tpl.maps.sdk:maps:2.0.1"
    implementation "com.tpl.maps.sdk:places:2.0.1"
    testImplementation "junit:junit:4.13.2"
    androidTestImplementation "androidx.test.ext:junit:1.2.1"
    androidTestImplementation "androidx.test.espresso:espresso-core:3.6.1"
    androidTestImplementation platform("androidx.compose:compose-bom:2024.04.01")
    androidTestImplementation "androidx.compose.ui:ui-test-junit4"
    debugImplementation "androidx.compose.ui:ui-tooling"
    debugImplementation "androidx.compose.ui:ui-test-manifest"
}
```

## Getting Started

### Prerequisites

1. **API Key**: Obtain your API key from the TPL Maps developer portal. Add the key to your `AndroidManifest.xml` file:
   ```xml
   <meta-data
       android:name="your_metadata_name"
       android:value="your_api_key" />
   ```

2. **Permissions**: Ensure the required permissions are declared in your `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
   ```

### Running the Project

1. Clone this repository:
   ```bash
   git clone [https://github.com/Hasnain17/TPL-Sample-Compose.git]
   ```
2. Open the project in Android Studio.
3. Build the project to install dependencies.
4. Run the app on an Android device or emulator.

## Code Overview

### MainActivity.kt

The `MainActivity` is the entry point of the application. It initializes the Jetpack Compose UI and sets up the `MapViewContainer` composable, which contains the core logic for map integration, place search, and reverse geocoding.

### MapViewContainer.kt

This composable function embeds the `MapView` using `AndroidView` and handles the TPL Maps SDK interactions, including:

- **MapView**: Initializes and manages the lifecycle of the `MapView`.
- **SearchManager**: Manages search queries and displays results using `LazyColumn`.
- **FloatingActionButton**: Provides zoom controls and a "Locate Me" feature.

### UI Components

- **Search Bar**: Allows users to search for places and displays the results.
- **Address Display**: Shows the address corresponding to the map's camera position.
- **Floating Action Buttons**: Controls for zooming in, zooming out, and centering the map on the user's location.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.



---

Feel free to adjust the sections according to your specific needs and add any additional details specific to your project.
