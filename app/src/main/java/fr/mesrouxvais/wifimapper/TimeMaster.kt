package fr.mesrouxvais.wifimapper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

object TimeMaster {
    private var job: Job? = null
    private val isRunning = AtomicBoolean(false)
    private val lastConfirmationTime = AtomicLong(0)
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private const val BASE_INTERVAL = 10_000L // 10 secondes entre scan = max speed 564 scans ?? here
    private const val EXTENDED_INTERVAL = 60_000L // 60 secondes supplémentaires

    fun startCycle(onCycle: () -> Unit) {
        if (isRunning.getAndSet(true)) {
            return // Évite de démarrer plusieurs cycles
        }

        job = coroutineScope.launch {
            while (isActive) {
                onCycle()

                val currentTime = System.currentTimeMillis()
                val timeSinceLastConfirmation = currentTime - lastConfirmationTime.get()

                // Si on n'a pas reçu de confirmation récemment
                if (lastConfirmationTime.get() == 0L || timeSinceLastConfirmation > EXTENDED_INTERVAL) {
                    delay(BASE_INTERVAL)
                } else {
                    delay(EXTENDED_INTERVAL)
                }
            }
        }
    }

    fun stopCycle() {
        job?.cancel()
        job = null
        isRunning.set(false)
        lastConfirmationTime.set(0)
    }

    fun confirmCycle() {
        lastConfirmationTime.set(System.currentTimeMillis())
    }

    // Pour vérifier si le cycle est en cours
    fun isRunning(): Boolean = isRunning.get()

}