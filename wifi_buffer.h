#define WIFI_BUFFER_H

#include <stdbool.h>
#include "esp_wifi.h"

#define MAX_SSID_LENGTH 33
#define LINE_BUFFER_SIZE 128

// Structure pour stocker une entrée WiFi
typedef struct {
    char ssid[MAX_SSID_LENGTH];
    int rssi;
    int authmode;
    int pairwise_cipher;
    int group_cipher;
    int primary;
} wifi_entry_t;

// Structure pour le buffer dynamique
typedef struct {
    wifi_entry_t* entries;
    size_t count;
    size_t capacity;
} wifi_buffer_t;

extern wifi_buffer_t* g_wifi_buffer;

// Fonctions publiques
bool wifi_buffer_init(size_t initial_capacity);
bool wifi_buffer_add_ap_info(const wifi_ap_record_t* ap_info);
char* wifi_buffer_get_formatted_data(void);
void wifi_buffer_free(void);
