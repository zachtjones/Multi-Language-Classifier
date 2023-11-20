package com.zachjones.languageclassifier.controller

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.zachjones.languageclassifier.model.DgsConstants
import com.zachjones.languageclassifier.model.types.LanguageClassificationInput
import com.zachjones.languageclassifier.model.types.LanguageClassificationResult
import com.zachjones.languageclassifier.service.LanguageClassificationService

@DgsComponent
class LanguageClassificationDataFetcher(
    private val languageClassificationService: LanguageClassificationService
) {

    @DgsQuery(field = DgsConstants.QUERY.Language)
    fun language(@InputArgument input: LanguageClassificationInput): LanguageClassificationResult {
        return languageClassificationService.classify(input)
    }
}