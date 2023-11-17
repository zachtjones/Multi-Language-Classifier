package com.zachjones.languageclassifier.controller

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.zachjones.languageclassifier.model.DgsConstants
import com.zachjones.languageclassifier.model.types.DownloadTrainingDataInput
import com.zachjones.languageclassifier.model.types.TrainingData
import com.zachjones.languageclassifier.model.types.TrainingDataResult
import com.zachjones.languageclassifier.service.TrainingDataService

@DgsComponent
class DownloadTrainingDataFetcher(
    private val trainingDataService: TrainingDataService
) {
    @DgsMutation(field = DgsConstants.MUTATION.DownloadTrainingData)
    fun downloadTrainingData(@InputArgument input: DownloadTrainingDataInput): TrainingData {
        // TODO - mutex on this to prevent too many requests
        val phrasesPerLanguage = input.numberOfPhrasesInEachLanguage
        require(phrasesPerLanguage in 10..1000) {
            "numberOfPhrasesInEachLanguage should be between 10 and 1000 (inclusive)"
        }
        return trainingDataService.downloadTrainingData(phrasesPerLanguage)
    }

    @DgsQuery(field = DgsConstants.QUERY.TrainingData)
    fun trainingDataSets(): TrainingDataResult {
        return TrainingDataResult(
            trainingData = trainingDataService.trainingDataSets()
        )
    }

}