#include "wifi_buffer.h"
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

wifi_buffer_t* g_wifi_buffer = NULL;

bool wifi_buffer_init(size_t initial_capacity) {
    if (g_wifi_buffer != NULL) {
        return false;
    }
    
    g_wifi_buffer = malloc(sizeof(wifi_buffer_t));
    if (!g_wifi_buffer) return false;
    
    g_wifi_buffer->entries = malloc(initial_capacity * sizeof(wifi_entry_t));
    if (!g_wifi_buffer->entries) {
        free(g_wifi_buffer);
        g_wifi_buffer = NULL;
        return false;
    }
    
    g_wifi_buffer->count = 0;
    g_wifi_buffer->capacity = initial_capacity;
    return true;
}

void wifi_buffer_reset(void){
    g_wifi_buffer->count = 0;
}


bool wifi_buffer_add_ap_info(const wifi_ap_record_t* ap_info) {
    if (!g_wifi_buffer || !ap_info) return false;

    // Vérifier si nous devons augmenter la capacité
    if (g_wifi_buffer->count >= g_wifi_buffer->capacity) {
        size_t new_capacity = g_wifi_buffer->capacity * 2;
        wifi_entry_t* new_entries = realloc(g_wifi_buffer->entries, 
                                          new_capacity * sizeof(wifi_entry_t));
        if (!new_entries) return false;
        
        g_wifi_buffer->entries = new_entries;
        g_wifi_buffer->capacity = new_capacity;
    }
    
    // Copier les données
    wifi_entry_t* entry = &g_wifi_buffer->entries[g_wifi_buffer->count];
    strncpy(entry->ssid, (char*)ap_info->ssid, MAX_SSID_LENGTH - 1);
    entry->ssid[MAX_SSID_LENGTH - 1] = '\0';
    entry->rssi = ap_info->rssi;
    entry->authmode = ap_info->authmode;
    entry->pairwise_cipher = ap_info->pairwise_cipher;
    entry->group_cipher = ap_info->group_cipher;
    entry->primary = ap_info->primary;
    
    g_wifi_buffer->count++;
    return true;
}

char* wifi_buffer_get_formatted_data(void) {
    if (!g_wifi_buffer) return NULL;

    size_t buffer_size = g_wifi_buffer->count * LINE_BUFFER_SIZE;
    char* output = malloc(buffer_size);
    if (!output) return NULL;
    
    // Initialisation de la chaîne de sortie avec le nombre de points Wi-Fi en première ligne
    snprintf(output, buffer_size, "%zu\n", g_wifi_buffer->count);
    size_t total_length = strlen(output);  // On commence avec la longueur de la première ligne
    
    // Ajouter les détails des points Wi-Fi
    for (size_t i = 0; i < g_wifi_buffer->count; i++) {
        wifi_entry_t* entry = &g_wifi_buffer->entries[i];
        char line[LINE_BUFFER_SIZE];
        
        snprintf(line, LINE_BUFFER_SIZE, 
                "%s,%d,%d,%d,%d,%d\n",
                entry->ssid, 
                entry->rssi,
                entry->authmode, 
                entry->pairwise_cipher,
                entry->group_cipher,
                entry->primary);
                
        if (total_length + strlen(line) >= buffer_size) {
            buffer_size *= 2;
            char* new_output = realloc(output, buffer_size);
            if (!new_output) {
                free(output);
                return NULL;
            }
            output = new_output;
        }
        
        strcat(output, line);
        total_length += strlen(line);
    }
    
    return output;
}


void wifi_buffer_free(void) {
    if (g_wifi_buffer) {
        free(g_wifi_buffer->entries);
        free(g_wifi_buffer);
        g_wifi_buffer = NULL;
    }
}
