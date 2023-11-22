package com.zachjones.languageclassifier.controller

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.zachjones.languageclassifier.model.DgsConstants
import com.zachjones.languageclassifier.model.types.LanguageClassificationInput
import com.zachjones.languageclassifier.model.types.LanguageClassificationResult
import com.zachjones.languageclassifier.service.LanguageClassificationService
import org.slf4j.LoggerFactory

@DgsComponent
class LanguageClassificationDataFetcher(
    private val languageClassificationService: LanguageClassificationService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @DgsQuery(field = DgsConstants.QUERY.Language)
    fun language(@InputArgument input: LanguageClassificationInput): LanguageClassificationResult {
        return languageClassificationService.classify(input).also {
            logger.info("Response: $it")
        }
    }
}