package fr.mesrouxvais.wifimapper

import android.content.Context
import android.graphics.Color
import fr.mesrouxvais.wifimapper.userInterface.Terminal

class PermissionCenter private constructor(private val context: Context, private val activity: MainActivity){

    companion object {
        private var instance: PermissionCenter? = null

        // Méthode pour créer l'instance du singleton
        fun initialize(context: Context, activity: MainActivity) {
            if (instance == null) {
                instance = PermissionCenter(context, activity)
                Terminal.getInstance().displayOnTerminal("[-]:Permission Center is initialized.", Color.WHITE)
                getInstance().askForPermission()

            } else {
                throw IllegalStateException("Permission Validation has already been initialized.")
            }
        }

        fun getInstance(): PermissionCenter {
            return instance ?: throw IllegalStateException("Permission Validation not initialized.")
        }
    }

    fun askForPermission(): Boolean{
        /*
        if (ContextCompat.checkSelfPermission(context, "com.google.android.things.permission.USE_PERIPHERAL_IO") != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, arrayOf("com.google.android.things.permission.USE_PERIPHERAL_IO"), 100)
            Terminal.getInstance().displayOnTerminal("[-]:permission i/o ask", Color.WHITE)
        } else {
            Terminal.getInstance().displayOnTerminal("[-]:permissions already granted", Color.WHITE)
        }
        */
        return true

    }


}