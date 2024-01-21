package com.zachjones.languageclassifier.service

import com.zachjones.languageclassifier.entities.DATA_PATH
import com.zachjones.languageclassifier.model.types.Language
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainingDataServiceTest {
    private val objectMapper = FileOperationService().objectMapper()
    private val trainingDataService = TrainingDataService(objectMapper)

    @OptIn(ExperimentalPathApi::class)
    @BeforeAll
    @AfterAll
    fun cleanup() {
        // delete test data before and after the run
        Path.of(DATA_PATH).deleteRecursively()
        Path.of(DATA_PATH).createDirectories()
    }

    @Test
    fun `a new data file created happy path`() {
        // small data size to run faster & easier to debug
        val result = trainingDataService.createTrainingData(10)
        result.numberOfPhrasesInEachLanguage shouldBe 10

        // load it
        val trainingData = trainingDataService.getTrainingDataSet(result.id)
        trainingData.forEach { it.language.shouldNotBeNull() }
        trainingData.size shouldBe Language.values().size * 10
    }
}