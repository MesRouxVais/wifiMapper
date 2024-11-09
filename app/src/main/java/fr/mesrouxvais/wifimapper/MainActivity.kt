package fr.mesrouxvais.wifimapper

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import fr.mesrouxvais.wifimapper.userInterface.Terminal
import fr.mesrouxvais.wifimapper.userInterface.UserInputCenter

@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : AppCompatActivity() {
    private lateinit var commandInput: EditText
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        commandInput = findViewById(R.id.commandInput)


        Terminal.initialize(findViewById(R.id.terminalOutput), findViewById(R.id.scrollView))
        Terminal.getInstance().displayOnTerminal("[-]:Welcome to the terminal", Color.WHITE)

        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        Terminal.getInstance().displayOnTerminal("[-]:android version :$sdkVersion ($release)", Color.WHITE)


        UserInputCenter.initialize(this)
        PermissionCenter.initialize(this,this)
        BluetoothCenter.initialize(this)



        val executeButton: Button = findViewById(R.id.executeButton)
        executeButton.setOnClickListener {
            val command = commandInput.text.toString()
            if (command.isNotBlank()) {
                Terminal.getInstance().displayOnTerminal("[+]:$command", Color.CYAN)
                UserInputCenter.getInstance().processCommand(command)
                commandInput.text.clear() // Efface le champ de saisie
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            BluetoothCenter.onBluetoothRequestPermissionsResult(grantResults)
        }
    }

}