package fr.mesrouxvais.wifimapper.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import fr.mesrouxvais.wifimapper.userInterface.Terminal
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.ref.WeakReference


class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val contextRef: WeakReference<Context> = WeakReference(context)

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_FIRST_NAME + " TEXT,"
                + COLUMN_LAST_NAME + " TEXT,"
                + COLUMN_AGE + " INTEGER" + ")")
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    fun exportDatabaseToCSV(csvFileName: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)

        // Ouvrir un fichier CSV
        val file = File(contextRef.get()?.getExternalFilesDir(null),csvFileName)
        val outputStream = FileOutputStream(file)
        val writer = BufferedWriter(OutputStreamWriter(outputStream))

        // Écrire l'en-tête du fichier CSV
        val columnNames = cursor.columnNames
        writer.write(columnNames.joinToString(","))
        writer.newLine()

        // Écrire les données du curseur dans le fichier CSV
        while (cursor.moveToNext()) {
            val rowData = (0 until cursor.columnCount)
                .map { cursor.getString(it) }
                .joinToString(",")
            writer.write(rowData)
            writer.newLine()
        }

        writer.flush()
        writer.close()
        cursor.close()
        Terminal.getInstance().displayOnTerminal("[-]: csv file ${file.name} created at ${file.path}", Color.WHITE)

    }


    // Méthodes pour insérer, lire, mettre à jour et supprimer des données
    fun addPerson(firstName: String?, lastName: String?, age: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_FIRST_NAME, firstName)
        values.put(COLUMN_LAST_NAME, lastName)
        values.put(COLUMN_AGE, age)
        return db.insert(TABLE_NAME, null, values)
    }

    val allPersons: List<Person>
        get() {
            val personList: MutableList<Person> = ArrayList<Person>()
            val selectQuery = "SELECT * FROM " + TABLE_NAME
            val db = this.readableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val person: Person = Person()
                    person.id = cursor.getInt(0);
                    person.firstName = cursor.getString(1);
                    person.lastName = cursor.getString(2);
                    person.age = cursor.getInt(3);
                    personList.add(person)
                } while (cursor.moveToNext())
            }
            return personList
        }

    companion object {
        private const val DATABASE_NAME = "person_data.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "persons"
        private const val COLUMN_ID = "id"
        private const val COLUMN_FIRST_NAME = "first_name"
        private const val COLUMN_LAST_NAME = "last_name"
        private const val COLUMN_AGE = "age"
    }
}