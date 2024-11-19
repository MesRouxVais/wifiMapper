package fr.mesrouxvais.wifimapper

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import fr.mesrouxvais.wifimapper.database.DatabaseHelper
import fr.mesrouxvais.wifimapper.userInterface.Terminal
import fr.mesrouxvais.wifimapper.userInterface.UserInputCenter

class LocationCenter (private val context: Context){

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var isLocationUpdatesStarted = false

    companion object {
        private const val LOCATION_UPDATE_INTERVAL = 10000L // 10 secondes
        private const val LOCATION_FASTEST_UPDATE_INTERVAL = 5000L // 5 secondes

        private var instance: LocationCenter? = null

        // Méthode pour créer l'instance du singleton
        fun initialize(context: Context) {
            if (instance == null) {
                instance = LocationCenter(context)
                Terminal.getInstance().displayOnTerminal("[-]:LocationCenter is initialized.", Color.WHITE)
            } else {
                throw IllegalStateException("LocationCenter has already been initialized.")
            }
        }

        fun getInstance(): LocationCenter {
            return instance ?: throw IllegalStateException("UserInputCenter not initialized.")
        }
    }

    fun startLocationPing() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Configuration de la localisation
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,  // Demande la meilleure précision possible
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_FASTEST_UPDATE_INTERVAL)

            // Options supplémentaires
            setWaitForAccurateLocation(true)  // Attendre une localisation précise
            setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL * 2)  // Délai max entre mises à jour
        }.build()

        // Callback pour les mises à jour de localisation
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Votre log de localisation
                    Terminal.getInstance().displayOnTerminal(
                        "[-]:Location : ${location.latitude}, ${location.longitude}",
                        Color.MAGENTA
                    )

                    // Informations supplémentaires possibles
                    Log.d("LocationInfo", """
                Latitude: ${location.latitude}
                Longitude: ${location.longitude}
                Précision: ${location.accuracy} mètres
                Vitesse: ${location.speed} km/h
                Altitude: ${location.altitude} m
            """.trimIndent())
                }
            }

            // Gestion des erreurs potentielles
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                if (!locationAvailability.isLocationAvailable) {
                    Terminal.getInstance().displayOnTerminal(
                        "Localisation temporairement indisponible",
                        Color.RED
                    )
                }
            }
        }
    }


    private fun toggleLocationUpdates() {
        if (isLocationUpdatesStarted) {
            stopLocationUpdates()
        } else {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {

        // Démarrer les mises à jour de localisation
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        isLocationUpdatesStarted = true

    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isLocationUpdatesStarted = false
    }

}