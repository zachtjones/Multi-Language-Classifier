package com.zachjones.languageclassifier.service

import com.zachjones.languageclassifier.model.types.Language
import com.zachjones.languageclassifier.model.types.RandomPhraseResult
import org.springframework.stereotype.Component

@Component
class PhraseService(
    private val trainingDataService: TrainingDataService
) {
    fun randomPhrase(): RandomPhraseResult {
        val dataSet = trainingDataService.trainingDataSets().random()
        val examples = trainingDataService.getTrainingDataSet(dataSet.id)
        val result = examples.random()
        return RandomPhraseResult(
            phrase = result.words.joinToString(separator = " "),
            language = result.language ?: Language.OTHER
        )
    }
}