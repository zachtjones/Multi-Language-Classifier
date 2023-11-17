package com.zachjones.languageclassifier.controller

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.zachjones.languageclassifier.model.DgsConstants
import com.zachjones.languageclassifier.model.types.DownloadTrainingDataInput
import com.zachjones.languageclassifier.model.types.DownloadedTrainingData
import com.zachjones.languageclassifier.model.types.DownloadedTrainingDataResult
import com.zachjones.languageclassifier.model.types.Language
import examples.GetWikipediaContent
import com.zachjones.languageclassifier.entities.InputRow
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

@DgsComponent
class DownloadTrainingDataFetcher {

    private val loadedDataMetadata = mutableListOf<DownloadedTrainingData>()
    private val loadedData = hashMapOf<Int, List<InputRow>>()
    private val nextTrainingNumber = AtomicInteger()
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        var i = 1
        while (true) {
            val fileName = getFileName(i)
            if (Files.exists(Path.of(fileName))) {
                val examples = InputRow.loadExamples(fileName)
                loadedData[i] = examples
                loadedDataMetadata.add(
                    DownloadedTrainingData(
                        id = "$i",
                        numberOfPhrasesInEachLanguage = examples.size / Language.values().size,
                        phraseWordLength = examples[0].words.size
                    )
                )
            } else {
                break
            }
            logger.info("Loaded ${i - 1} training data sets")
            i++
        }
        nextTrainingNumber.set(i)

    }

    private fun getFileName(id: Int) = "data/training-$id.txt"

    @DgsMutation(field = DgsConstants.MUTATION.DownloadTrainingData)
    fun downloadTrainingData(@InputArgument input: DownloadTrainingDataInput): DownloadedTrainingData {
        // TODO - mutex on this to prevent too many requests
        val number = nextTrainingNumber.getAndIncrement()
        // TODO - base on input
        val phraseWordLength = 20

        val phrasesPerLanguage = input.numberOfPhrasesInEachLanguage
        require(phrasesPerLanguage in 10..1000) {
            "numberOfPhrasesInEachLanguage should be between 10 and 1000 (inclusive)"
        }
        val filename = getFileName(number)

        GetWikipediaContent.main(filename, phrasesPerLanguage)
        // TODO - return the data instead of having to load it
        val createdData = InputRow.loadExamples(filename)
        loadedData[number] = createdData
        loadedDataMetadata.add(
            DownloadedTrainingData(
                id = "$number",
                numberOfPhrasesInEachLanguage = phrasesPerLanguage,
                phraseWordLength = phraseWordLength
            )
        )
        return DownloadedTrainingData(
            id = "$number",
            numberOfPhrasesInEachLanguage = phrasesPerLanguage,
            phraseWordLength = phraseWordLength
        )
    }

    @DgsQuery(field = DgsConstants.QUERY.DownloadedTrainingDataSets)
    fun downloadedTrainingDataSets(): DownloadedTrainingDataResult {
        return DownloadedTrainingDataResult(
            trainingData = loadedDataMetadata
        )
    }

}