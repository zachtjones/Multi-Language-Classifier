package com.zachjones.languageclassifier.service

import com.netflix.graphql.dgs.DgsMutation
import com.zachjones.languageclassifier.entities.DATA_PATH
import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.entities.TRAINING_DATA_PREFIX
import com.zachjones.languageclassifier.entities.TRAINING_DATA_SUFFIX
import com.zachjones.languageclassifier.model.DgsConstants
import com.zachjones.languageclassifier.model.types.DownloadedTrainingData
import com.zachjones.languageclassifier.model.types.Language
import examples.GetWikipediaContent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.name

@Component
class TrainingDataService {

    private val loadedDataMetadata = mutableListOf<DownloadedTrainingData>()
    private val loadedData = hashMapOf<String, List<InputRow>>()
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        val files = Files.list(Path.of(DATA_PATH)).filter {
            it.name.startsWith(TRAINING_DATA_PREFIX)
        }.toList()
        val trainingData = files.map {
            val id = it.name.removePrefix(TRAINING_DATA_PREFIX).removeSuffix(TRAINING_DATA_SUFFIX)

            return@map id to InputRow.loadExamples(it.toFile().absolutePath)
        }.associate { it.first to it.second }
        loadedData += trainingData

        loadedDataMetadata += loadedData.map {
            DownloadedTrainingData(
                id = it.key,
                // assuming all words are the same size
                numberOfPhrasesInEachLanguage = it.value.size / Language.values().size,
                phraseWordLength = it.value[0].words.size
            )
        }
        logger.info("Loaded ${loadedData.size} training data sets")
    }

    private fun getFileName(id: String) = "$DATA_PATH$TRAINING_DATA_PREFIX${id}$TRAINING_DATA_SUFFIX"

    @DgsMutation(field = DgsConstants.MUTATION.DownloadTrainingData)
    fun downloadTrainingData(phrasesPerLanguage: Int): DownloadedTrainingData {
        // TODO - base on input
        val phraseWordLength = 20

        val id = UUID.randomUUID().toString()
        val filename = getFileName(id)

        GetWikipediaContent.main(filename, phrasesPerLanguage)
        // TODO - return the data instead of having to load it
        val createdData = InputRow.loadExamples(filename)
        loadedData[id] = createdData
        loadedDataMetadata.add(
            DownloadedTrainingData(
                id = id,
                numberOfPhrasesInEachLanguage = phrasesPerLanguage,
                phraseWordLength = phraseWordLength
            )
        )
        return DownloadedTrainingData(
            id = id,
            numberOfPhrasesInEachLanguage = phrasesPerLanguage,
            phraseWordLength = phraseWordLength
        )
    }

    fun trainingDataSets(): List<DownloadedTrainingData> {
        return loadedDataMetadata
    }

    fun getTrainingDataSet(id: String): List<InputRow> {
        return loadedData[id]
            ?: throw IllegalArgumentException("Error: $id does not exist in training data")
    }
}