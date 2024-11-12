# wifiMapper
#### Languages
[![used lang](https://skillicons.dev/icons?i=java,kotlin,c)](https://skillicons.dev)
#### Platforms
[![used tech](https://skillicons.dev/icons?i=docker,androidstudio,cmake)](https://skillicons.dev)

| Component            | Function                                                                                           | Links                            |
|----------------------|----------------------------------------------------------------------------------------------------|----------------------------------|
| ESP32                | Collects Wi-Fi access point information and sends it via BLE to the Android application            | AP: WiFi, Android-APP: BLE       |
| Android application  | Stores data from the ESP32 in an SQLite database and sends it to a remote server at the end of a scan session | ESP: BLE, remote server: "HTTP" |
| Remote server        | Processes the data and displays it on a map                                                        | Android-APP: "HTTP"              |
