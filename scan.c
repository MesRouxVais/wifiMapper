
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/event_groups.h"
#include "esp_wifi.h"
#include "esp_log.h"
#include "esp_event.h"
#include "nvs_flash.h"
#include "wifi_buffer.h"

#include <stdio.h>
#include "freertos/task.h"
#include "esp_nimble_hci.h"
#include "nimble/nimble_port.h"
#include "nimble/nimble_port_freertos.h"
#include "host/ble_hs.h"
#include "services/gap/ble_svc_gap.h"
#include "services/gatt/ble_svc_gatt.h"
#include "sdkconfig.h"



#define DEFAULT_SCAN_LIST_SIZE 24
static const char *TAG = "wifi_scan";

static uint8_t ble_addr_type;
static char received_data[15] = "scan data ready";
static uint16_t notification_handle = 0;
static uint16_t conn_handle = BLE_HS_CONN_HANDLE_NONE;
static bool notifications_enabled = true;
void ble_app_advertise(void);
void wifi_scan(void);

// Fonction pour envoyer une notification
static int send_notification(const char *data) {
    struct os_mbuf *om;
    int rc;

    if (conn_handle == BLE_HS_CONN_HANDLE_NONE || !notifications_enabled) {
        ESP_LOGI(TAG, "Cannot send notification: no connection or notifications disabled");
        return BLE_HS_ENOTCONN;
    }

    om = ble_hs_mbuf_from_flat(data, strlen(data));
    if (om == NULL) {
        ESP_LOGE(TAG, "Failed to allocate memory for notification");
        return BLE_HS_ENOMEM;
    }

    rc = ble_gattc_notify_custom(conn_handle, notification_handle, om);
    if (rc != 0) {
        ESP_LOGE(TAG, "Failed to send notification, rc=%d", rc);
        os_mbuf_free_chain(om);
        return rc;
    }

    ESP_LOGI(TAG, "Notification sent successfully: %s", data);
    return 0;
}

static int device_write(uint16_t conn_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt *ctxt, void *arg)
{
    
    ESP_LOGI(TAG, "Data received from client");
    wifi_scan();
    // Envoyer une notification avec les données reçues
    if (notifications_enabled) {
        send_notification(received_data);
    }
    
    return 0;
}

static int device_read(uint16_t con_handle, uint16_t attr_handle, struct ble_gatt_access_ctxt *ctxt, void *arg)
{
    char* formatted_data = wifi_buffer_get_formatted_data();
    os_mbuf_append(ctxt->om, formatted_data, strlen(formatted_data));
    return 0;
}

static const struct ble_gatt_svc_def gatt_svcs[] = {
    {
        .type = BLE_GATT_SVC_TYPE_PRIMARY,
        .uuid = BLE_UUID16_DECLARE(0x180),
        .characteristics = (struct ble_gatt_chr_def[]){
            {
                .uuid = BLE_UUID16_DECLARE(0xFEF4),
                .flags = BLE_GATT_CHR_F_READ,
                .access_cb = device_read,
                .val_handle = &notification_handle
            },
            {
                .uuid = BLE_UUID16_DECLARE(0xDEAD),
                .flags = BLE_GATT_CHR_F_WRITE,
                .access_cb = device_write
            },
            {
                0
            }
        }
    },
    {
        0
    }
};

static int ble_gap_event(struct ble_gap_event *event, void *arg)
{
    switch (event->type)
    {
        case BLE_GAP_EVENT_CONNECT:
            ESP_LOGI(TAG, "BLE GAP EVENT CONNECT %s", event->connect.status == 0 ? "OK!" : "FAILED!");
            if (event->connect.status == 0) {
                conn_handle = event->connect.conn_handle;
            } else {
                conn_handle = BLE_HS_CONN_HANDLE_NONE;
                ble_app_advertise();
            }
            break;

        case BLE_GAP_EVENT_DISCONNECT:
            ESP_LOGI(TAG, "BLE GAP EVENT DISCONNECT");
            conn_handle = BLE_HS_CONN_HANDLE_NONE;
            //notifications_enabled = false; simplify 
            ble_app_advertise();
            break;

        case BLE_GAP_EVENT_SUBSCRIBE:
            notifications_enabled = event->subscribe.cur_notify;
            ESP_LOGI(TAG, "Notifications %s", notifications_enabled ? "enabled" : "disabled");
            break;

        case BLE_GAP_EVENT_MTU:
            ESP_LOGI(TAG, "MTU Update Event, MTU: %d", event->mtu.value);
            break;

        default:
            break;
    }
    return 0;
}

void ble_app_advertise(void)
{
    struct ble_hs_adv_fields fields;
    const char *device_name;
    memset(&fields, 0, sizeof(fields));
    device_name = ble_svc_gap_device_name();
    fields.name = (uint8_t *)device_name;
    fields.name_len = strlen(device_name);
    fields.name_is_complete = 1;
    ble_gap_adv_set_fields(&fields);

    struct ble_gap_adv_params adv_params;
    memset(&adv_params, 0, sizeof(adv_params));
    adv_params.conn_mode = BLE_GAP_CONN_MODE_UND;
    adv_params.disc_mode = BLE_GAP_DISC_MODE_GEN;
    ble_gap_adv_start(ble_addr_type, NULL, BLE_HS_FOREVER, &adv_params, ble_gap_event, NULL);
}

void ble_app_on_sync(void)
{
    ble_hs_id_infer_auto(0, &ble_addr_type);
    ble_app_advertise();
}

void host_task(void *param)
{
    nimble_port_run();
}

//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________


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
    wifi_buffer_reset();
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

static bool wifi_initialized = false;

static esp_err_t initialize_wifi(void) {
    if (wifi_initialized) {
        return ESP_OK;
    }

    // Create default station interface if it doesn't exist
    esp_netif_t *sta_netif = esp_netif_create_default_wifi_sta();
    if (sta_netif == NULL) {
        ESP_LOGE(TAG, "Failed to create station interface");
        return ESP_FAIL;
    }

    // Initialize WiFi with error handling
    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
    esp_err_t err = esp_wifi_init(&cfg);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to init wifi: %s", esp_err_to_name(err));
        return err;
    }

    err = esp_wifi_set_mode(WIFI_MODE_STA);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to set wifi mode: %s", esp_err_to_name(err));
        return err;
    }

    err = esp_wifi_start();
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to start wifi: %s", esp_err_to_name(err));
        return err;
    }

    wifi_initialized = true;
    return ESP_OK;
}


void wifi_scan(void) {
    // Initialize WiFi if not already initialized
    esp_err_t err = initialize_wifi();
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to initialize WiFi");
        return;
    }

    // Clear previous scan results
    err = esp_wifi_scan_stop();
    if (err != ESP_OK && err != ESP_ERR_WIFI_NOT_STARTED) {
        ESP_LOGW(TAG, "Failed to stop previous scan: %s", esp_err_to_name(err));
    }

    // Configure scan parameters (optional)
    wifi_scan_config_t scan_config = {
        .ssid = NULL,
        .bssid = NULL,
        .channel = 0,     // 0 = scan all channels
        .show_hidden = true,
        .scan_type = WIFI_SCAN_TYPE_ACTIVE,
        .scan_time.active.min = 200,
        .scan_time.active.max = 300
    };

    // Allocate memory for scan results
    wifi_ap_record_t *ap_info = calloc(DEFAULT_SCAN_LIST_SIZE, sizeof(wifi_ap_record_t));
    if (ap_info == NULL) {
        ESP_LOGE(TAG, "Failed to allocate memory for AP records");
        return;
    }

    // Start scan with error handling
    err = esp_wifi_scan_start(&scan_config, true);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to start scan: %s", esp_err_to_name(err));
        free(ap_info);
        return;
    }

    // Get scan results
    uint16_t ap_count = 0;
    err = esp_wifi_scan_get_ap_num(&ap_count);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get AP count: %s", esp_err_to_name(err));
        free(ap_info);
        return;
    }
    
    uint16_t number = DEFAULT_SCAN_LIST_SIZE;
    err = esp_wifi_scan_get_ap_records(&number, ap_info);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to get AP records: %s", esp_err_to_name(err));
        free(ap_info);
        return;
    }
    
    ESP_LOGI(TAG, "Found %u access points", ap_count);

    // Process and save results
    save_scan_results(ap_info, ap_count);

    // Cleanup
    free(ap_info);

    // Optional: add delay between scans if you're doing them in quick succession
    // vTaskDelay(pdMS_TO_TICKS(1000));  // 1 second delay
}

void wifi_cleanup(void) {
    if (!wifi_initialized) {
        return;
    }

    esp_wifi_scan_stop();
    esp_wifi_stop();
    esp_wifi_deinit();
    wifi_initialized = false;
}

//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________
//_______________________________________________________________________________________


void app_main(void) {
    // Initialize NVS first
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);

    // Initialize TCP/IP and event loop first
    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());

    // Initialize WiFi buffer
    if (!wifi_buffer_init(DEFAULT_SCAN_LIST_SIZE)) {
        ESP_LOGE(TAG, "Failed to initialize WiFi buffer");
        return;
    }

    // Initialize BLE
    nimble_port_init();
    ble_svc_gap_device_name_set("BLE-Server");
    ble_svc_gap_init();
    ble_svc_gatt_init();
    ble_gatts_count_cfg(gatt_svcs);
    ble_gatts_add_svcs(gatt_svcs);
    ble_hs_cfg.sync_cb = ble_app_on_sync;
    nimble_port_freertos_init(host_task);

    // Print information
    print_auth_modes();
    print_cipher_types();
}