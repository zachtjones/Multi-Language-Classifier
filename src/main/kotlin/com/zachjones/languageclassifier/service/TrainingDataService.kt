package com.zachjones.languageclassifier.service

import com.zachjones.languageclassifier.entities.DATA_PATH
import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.entities.TRAINING_DATA_PREFIX
import com.zachjones.languageclassifier.entities.TRAINING_DATA_SUFFIX
import com.zachjones.languageclassifier.entities.poemSourceFile
import com.zachjones.languageclassifier.model.types.Language
import com.zachjones.languageclassifier.model.types.TrainingData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.name

@Component
class TrainingDataService(
    private val fileOperationService: FileOperationService
) {

    private val loadedDataMetadata = mutableListOf<TrainingData>()
    private val loadedData = hashMapOf<String, List<InputRow>>()
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        val files = Files.list(Path.of(DATA_PATH)).filter {
            it.name.startsWith(TRAINING_DATA_PREFIX)
        }.toList()
        val trainingData = files.map {
            val id = it.name.removePrefix(TRAINING_DATA_PREFIX).removeSuffix(TRAINING_DATA_SUFFIX)
            val trainingData = fileOperationService.readJsonFile<List<InputRow>>(getFileName(id))
            return@map id to trainingData
        }.associate { it.first to it.second }
        loadedData += trainingData

        loadedDataMetadata += loadedData.map {
            TrainingData(
                id = it.key,
                // assuming all words are the same size
                numberOfPhrasesInEachLanguage = it.value.size / Language.values().size
            )
        }
        logger.info("Loaded ${loadedData.size} training data sets")
    }

    private fun getFileName(id: String) = Path("$DATA_PATH$TRAINING_DATA_PREFIX${id}$TRAINING_DATA_SUFFIX")

    fun createTrainingData(phrasesPerLanguage: Int): TrainingData {
        val id = UUID.randomUUID().toString()
        logger.info("Creating training data id=$id with $phrasesPerLanguage phrases per language")

        val input: List<InputRow> = Language.values().filter { it != Language.OTHER }.flatMap { language ->
            createInputRows(language, phrasesPerLanguage).also {
                logger.info("Created training data for: $language")
            }
        }

        fileOperationService.writeJsonFile(getFileName(id), input)

        loadedData[id] = input
        val newMetaData = TrainingData(
            id = id,
            numberOfPhrasesInEachLanguage = phrasesPerLanguage,
        )
        loadedDataMetadata.add(newMetaData)
        logger.info("Created training data id=$id")
        return newMetaData
    }

    private fun createInputRows(language: Language, numberExamples: Int): List<InputRow> {
        val filename = poemSourceFile(language)
        // TODO see if the paragraphs training is better, or just doing one line at a time
        val fileContent = this::class.java.classLoader.getResourceAsStream(filename)!!
            .bufferedReader(Charsets.UTF_8)
            .readLines()
            .filter { it.length > 20 } // skip empty and short lines

        return fileContent
            .shuffled()
            .take(numberExamples)
            .map { InputRow(language, it) }
    }

    fun trainingDataSets(): List<TrainingData> {
        return loadedDataMetadata
    }

    fun getTrainingDataSet(id: String): List<InputRow> {
        return loadedData[id]
            ?: throw IllegalArgumentException("Error: $id does not exist in training data")
    }
}