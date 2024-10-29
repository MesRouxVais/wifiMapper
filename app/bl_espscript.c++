#include <BluetoothSerial.h>

BluetoothSerial SerialBT;

void setup() {
    Serial.begin(115200); // Initialiser la communication série
    SerialBT.begin("ESP32_Bl"); // Nom du dispositif Bluetooth
    Serial.println("Bluetooth prêt !");
}

void loop() {
    // Envoie un message toutes les secondes
    SerialBT.println("exécution boucle");
    Serial.println("Message envoyé : exécution boucle");

    // Vérifie si des données sont disponibles via Bluetooth
    if (SerialBT.available()) {
        String input = SerialBT.readStringUntil('\n'); // Lire jusqu'à la nouvelle ligne
        Serial.print("Données reçues : ");
        Serial.println(input); // Afficher les données reçues sur le port série
    }

    delay(2000); // Attendre 1 seconde
}