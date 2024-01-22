package com.zachjones.languageclassifier.controller

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.zachjones.languageclassifier.model.DgsConstants
import com.zachjones.languageclassifier.model.types.RandomPhraseResult
import com.zachjones.languageclassifier.service.PhraseService

@DgsComponent
class PhraseDataFetcher(
    private val phraseService: PhraseService
) {
    @DgsQuery(field = DgsConstants.QUERY.RandomPhrase)
    fun randomPhrase(): RandomPhraseResult {
        return phraseService.randomPhrase()
    }
}