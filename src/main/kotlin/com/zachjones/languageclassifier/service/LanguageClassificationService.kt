package com.zachjones.languageclassifier.service

import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.model.types.Language
import com.zachjones.languageclassifier.model.types.LanguageClassificationInput
import com.zachjones.languageclassifier.model.types.LanguageClassificationResult
import com.zachjones.languageclassifier.model.types.LanguageProbability
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LanguageClassificationService(
    val modelsService: ModelsService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun classify(input: LanguageClassificationInput): LanguageClassificationResult {
        val decider = modelsService.getModelById(input.modelId)

        val decision = decider.decide(InputRow(input.phrase))
        logger.info("Analyzed the phrase submitted successfully")

        return LanguageClassificationResult(
            // TODO - convert the repo to use enum everywhere instead
            mostLikelyLanguage = Language.valueOf(decision.mostConfidentLanguage()),
            probabilities = Language.values().map {
                LanguageProbability(
                    language = it,
                    percentageLikely = decision.confidenceForLanguage(it.name) * 100
                )
            }.sortedByDescending { it.percentageLikely }
        )
    }
}