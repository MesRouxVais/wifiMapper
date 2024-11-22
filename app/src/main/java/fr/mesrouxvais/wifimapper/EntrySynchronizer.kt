package fr.mesrouxvais.wifimapper

import android.graphics.Color
import fr.mesrouxvais.wifimapper.userInterface.Terminal
import java.lang.ref.WeakReference

data class AP(
    val name: String,
    val value1: Int,
    val value2: Int,
    val value3: Int,
    val value4: Int,
    val value5: Int,
    val latitude: Double,
    val longitude: Double
)

data class DensityPoint(
    val appareilCount: Int,
    val latitude: Double,
    val longitude: Double
)




object EntrySynchronizer {
    //maybe usefull


    private var espStatement: String ? = null
    private var lastLatitude: Double ? = null
    private var lastLongitude: Double ? = null



    fun updateLocation(latitude: Double, longitude: Double){
        this.lastLongitude = longitude
        this.lastLatitude = latitude
        Terminal.getInstance().displayOnTerminal("[i]:updateLocation", Color.YELLOW)
        checkInputCompletion()
    }
    fun updateStatement(statement: String){
        this.espStatement = statement
        Terminal.getInstance().displayOnTerminal("[i]:updateStatement", Color.YELLOW)
        checkInputCompletion()
    }
    private fun checkInputCompletion(){
        Terminal.getInstance().displayOnTerminal("[i]:cheking completation", Color.YELLOW)
        if(espStatement != null && lastLatitude != null && lastLongitude != null){
            main(espStatement!!, lastLatitude!!, lastLongitude!!)
        }
    }
    fun main(input: String, latitude: Double, longitude: Double) {

        // Parse APs
        val aps = parseAPs(input, latitude, longitude)
        Terminal.getInstance().displayOnTerminal("[i]:Parsed APs: $aps", Color.WHITE)

        // Create Density Points
        val densityPoints = createDensityPoints(input, latitude, longitude)
        Terminal.getInstance().displayOnTerminal("[i]:Density Points: $densityPoints", Color.WHITE)
        espStatement = null
        lastLatitude = null
        lastLatitude = null
        TimeMaster.confirmCycle()
    }

    fun parseAPs(input: String, latitude: Double, longitude: Double): List<AP> {
        // Split the input into lines and skip the first line (number of devices)
        val lines = input.split("\n").drop(1)

        return lines
            // Filter out incomplete lines (less than 6 parameters)
            .filter { it.split(",").size >= 6 }
            .map { line ->
                val parts = line.split(",")
                AP(
                    name = parts[0],
                    value1 = parts[1].toIntOrNull() ?: 0,
                    value2 = parts[2].toIntOrNull() ?: 0,
                    value3 = parts[3].toIntOrNull() ?: 0,
                    value4 = parts[4].toIntOrNull() ?: 0,
                    value5 = parts[5].toIntOrNull() ?: 0,
                    latitude = latitude,
                    longitude = longitude
                )
            }
    }

    fun createDensityPoints(input: String, latitude: Double, longitude: Double): List<DensityPoint> {
        // First line contains the number of devices
        val appareilCount = input.split("\n").first().toIntOrNull() ?: 0

        return listOf(
            DensityPoint(
                appareilCount = appareilCount,
                latitude = latitude,
                longitude = longitude
            )
        )
    }
}