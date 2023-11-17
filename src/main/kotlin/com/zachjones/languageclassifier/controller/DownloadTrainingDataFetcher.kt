package com.zachjones.languageclassifier.controller

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.zachjones.languageclassifier.model.DgsConstants
import com.zachjones.languageclassifier.model.types.DownloadTrainingDataInput
import com.zachjones.languageclassifier.model.types.DownloadedTrainingData
import com.zachjones.languageclassifier.model.types.DownloadedTrainingDataResult
import com.zachjones.languageclassifier.service.TrainingDataService

@DgsComponent
class DownloadTrainingDataFetcher(
    private val trainingDataService: TrainingDataService
) {
    @DgsMutation(field = DgsConstants.MUTATION.DownloadTrainingData)
    fun downloadTrainingData(@InputArgument input: DownloadTrainingDataInput): DownloadedTrainingData {
        // TODO - mutex on this to prevent too many requests
        val phrasesPerLanguage = input.numberOfPhrasesInEachLanguage
        require(phrasesPerLanguage in 10..1000) {
            "numberOfPhrasesInEachLanguage should be between 10 and 1000 (inclusive)"
        }
        return trainingDataService.downloadTrainingData(phrasesPerLanguage)
    }

    @DgsQuery(field = DgsConstants.QUERY.DownloadedTrainingDataSets)
    fun trainingDataSets(): DownloadedTrainingDataResult {
        return DownloadedTrainingDataResult(
            trainingData = trainingDataService.trainingDataSets()
        )
    }

}