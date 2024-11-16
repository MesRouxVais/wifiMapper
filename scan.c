
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/event_groups.h"
#include "esp_wifi.h"
#include "esp_log.h"
#include "esp_event.h"
#include "nvs_flash.h"
#include "wifi_buffer.h"

#define DEFAULT_SCAN_LIST_SIZE 24
static const char *TAG = "wifi_scan";

static void print_auth_modes(void) {
    ESP_LOGI(TAG, "Authentication Modes:");
    ESP_LOGI(TAG, "WIFI_AUTH_OPEN \t%d", WIFI_AUTH_OPEN);
    ESP_LOGI(TAG, "WIFI_AUTH_OWE \t%d", WIFI_AUTH_OWE);
    ESP_LOGI(TAG, "WIFI_AUTH_WEP \t%d", WIFI_AUTH_WEP);
    ESP_LOGI(TAG, "WIFI_AUTH_WPA_PSK \t%d", WIFI_AUTH_WPA_PSK);
    ESP_LOGI(TAG, "WIFI_AUTH_WPA2_PSK \t%d", WIFI_AUTH_WPA2_PSK);
    ESP_LOGI(TAG, "WIFI_AUTH_WPA_WPA2_PSK \t%d", WIFI_AUTH_WPA_WPA2_PSK);
    ESP_LOGI(TAG, "WIFI_AUTH_WPA3_PSK \t%d", WIFI_AUTH_WPA3_PSK);
    ESP_LOGI(TAG, "WIFI_AUTH_WPA2_WPA3_PSK \t%d", WIFI_AUTH_WPA2_WPA3_PSK);
}

static void print_cipher_types(void) {
    ESP_LOGI(TAG, "Cipher Types:");
    ESP_LOGI(TAG, "WIFI_CIPHER_TYPE_NONE \t%d", WIFI_CIPHER_TYPE_NONE);
    ESP_LOGI(TAG, "WIFI_CIPHER_TYPE_WEP40 \t%d", WIFI_CIPHER_TYPE_WEP40);
    ESP_LOGI(TAG, "WIFI_CIPHER_TYPE_WEP104 \t%d", WIFI_CIPHER_TYPE_WEP104);
    ESP_LOGI(TAG, "WIFI_CIPHER_TYPE_TKIP \t%d", WIFI_CIPHER_TYPE_TKIP);
    ESP_LOGI(TAG, "WIFI_CIPHER_TYPE_CCMP \t%d", WIFI_CIPHER_TYPE_CCMP);
    ESP_LOGI(TAG, "WIFI_CIPHER_TYPE_TKIP_CCMP \t%d", WIFI_CIPHER_TYPE_TKIP_CCMP);
    ESP_LOGI(TAG, "WIFI_CIPHER_TYPE_AES_CMAC128 \t%d", WIFI_CIPHER_TYPE_AES_CMAC128);
}

static void save_scan_results(wifi_ap_record_t *ap_info, uint16_t ap_count) {
    for (int i = 0; i < ap_count; i++) {
        if (!wifi_buffer_add_ap_info(&ap_info[i])) {
            ESP_LOGE(TAG, "Failed to add AP info to buffer");
        }
    }

    char* formatted_data = wifi_buffer_get_formatted_data();
    if (formatted_data) {
        printf("%s", formatted_data);
        free(formatted_data);
    }

    ESP_LOGI(TAG, "Scan results processed");
}

static void wifi_scan(void) {
    // Initialize network interface
    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    esp_netif_t *sta_netif = esp_netif_create_default_wifi_sta();
    assert(sta_netif);

    // Initialize WiFi
    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
    ESP_ERROR_CHECK(esp_wifi_init(&cfg));
    ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_STA));
    ESP_ERROR_CHECK(esp_wifi_start());

    // Allocate memory for scan results
    wifi_ap_record_t *ap_info = calloc(DEFAULT_SCAN_LIST_SIZE, sizeof(wifi_ap_record_t));
    if (ap_info == NULL) {
        ESP_LOGE(TAG, "Failed to allocate memory for AP records");
        return;
    }

    // Start scan
    ESP_ERROR_CHECK(esp_wifi_scan_start(NULL, true));

    // Get scan results
    uint16_t ap_count = 0;
    ESP_ERROR_CHECK(esp_wifi_scan_get_ap_num(&ap_count));
    
    uint16_t number = DEFAULT_SCAN_LIST_SIZE;
    ESP_ERROR_CHECK(esp_wifi_scan_get_ap_records(&number, ap_info));
    
    ESP_LOGI(TAG, "Found %u access points", ap_count);

    // Process and save results
    save_scan_results(ap_info, ap_count);

    // Cleanup
    free(ap_info);
}

void app_main(void) {
    // Initialize NVS
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);

    // Initialize WiFi buffer
    if (!wifi_buffer_init(DEFAULT_SCAN_LIST_SIZE)) {
        ESP_LOGE(TAG, "Failed to initialize WiFi buffer");
        return;
    }

    // Print information and start scan
    print_auth_modes();
    print_cipher_types();
    wifi_scan();

    // Cleanup
    wifi_buffer_free();
}