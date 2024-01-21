package com.zachjones.languageclassifier.core

import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.entities.LanguageDecision
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

interface Decider : Serializable {
    /** Returns the decision for the input value  */
    fun decide(row: InputRow): LanguageDecision

    /** Human readable output for debugging.  */
    fun representation(numSpaces: Int): String?

    /** Tests this decider on the input data, and returns the number incorrect / total.  */
    fun errorRateUnWeighted(testingData: List<InputRow>): Double {
        val total = testingData.size.toDouble()
        val correct = testingData.stream()
            .filter { i: InputRow -> decide(i).mostConfidentLanguage() == i.language }
            .count().toDouble()

        return (total - correct) / total
    }

    /** Writes this object using java serialization to the filename.  */
    @Throws(IOException::class)
    fun saveTo(fileName: String) {
        val file = FileOutputStream(fileName)
        val out = ObjectOutputStream(file)
        out.writeObject(this)
        out.flush()
        out.close()
        file.close()
    }

    companion object {
        /** Loads a decider from the file using java deserialization  */
        @Throws(IOException::class, ClassNotFoundException::class)
        fun loadFromFile(fileName: String): Decider {
            val file = FileInputStream(fileName)
            val `in` = ObjectInputStream(file)
            val result = `in`.readObject() as Decider
            `in`.close()
            file.close()
            return result
        }
    }
}
