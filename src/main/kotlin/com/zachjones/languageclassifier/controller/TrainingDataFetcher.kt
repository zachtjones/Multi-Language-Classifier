package com.zachjones.languageclassifier.controller

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.zachjones.languageclassifier.config.MutationsConfig
import com.zachjones.languageclassifier.model.DgsConstants
import com.zachjones.languageclassifier.model.types.CreateTrainingDataInput
import com.zachjones.languageclassifier.model.types.TrainingData
import com.zachjones.languageclassifier.model.types.TrainingDataResult
import com.zachjones.languageclassifier.service.TrainingDataService

@DgsComponent
class TrainingDataFetcher(
    private val trainingDataService: TrainingDataService,
    private val mutationsConfig: MutationsConfig
) {
    @DgsMutation(field = DgsConstants.MUTATION.CreateTrainingData)
    fun createTrainingData(@InputArgument input: CreateTrainingDataInput): TrainingData {
        require(!mutationsConfig.disableMutations) {
            "Mutations are disabled"
        }
        val phrasesPerLanguage = input.numberOfPhrasesInEachLanguage
        // files are each only 10k lines, so we won't want to do more than 2k to avoid all
        // data files looking the same
        require(phrasesPerLanguage in 10..2_000) {
            "numberOfPhrasesInEachLanguage should be between 10 and 2_000 (inclusive)"
        }
        return trainingDataService.createTrainingData(phrasesPerLanguage)
    }

    @DgsQuery(field = DgsConstants.QUERY.TrainingData)
    fun trainingDataSets(): TrainingDataResult {
        return TrainingDataResult(
            trainingData = trainingDataService.trainingDataSets()
        )
    }

}