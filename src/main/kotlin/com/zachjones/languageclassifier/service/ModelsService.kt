package com.zachjones.languageclassifier.service

import com.zachjones.languageclassifier.LanguageClassifierProperties
import com.zachjones.languageclassifier.core.Decider
import com.zachjones.languageclassifier.core.MultiClassifier
import com.zachjones.languageclassifier.entities.DATA_PATH
import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.entities.MODEL_PREFIX
import com.zachjones.languageclassifier.entities.MODEL_SUFFIX
import com.zachjones.languageclassifier.model.types.ModelType
import com.zachjones.languageclassifier.model.types.TrainedModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.name

@Component
class ModelsService(
    private val languageClassifierProperties: LanguageClassifierProperties
) {

    private val loadedModelsMetadata = hashMapOf<String, TrainedModel>()
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        val files = Files.list(Path.of(DATA_PATH)).filter {
            it.name.startsWith(MODEL_PREFIX)
        }.toList()
        val modelMetadata = files.map {
            val id = it.name.removePrefix(MODEL_PREFIX).removeSuffix(MODEL_SUFFIX)

            return@map id to Decider.loadFromFile(it.toFile().absolutePath)
        }.associate { it.first to it.second }.mapValues { (id, model) ->
            model as MultiClassifier
            TrainedModel(
                modelId = id,
                description = model.description,
                trainingAccuracyPercentage = -1.0, // TODO - also persist this data
                testingAccuracyPercentage = -1.0 // also persist this data
            )
        }
        loadedModelsMetadata += modelMetadata

        logger.info("Loaded ${loadedModelsMetadata.size} models")
    }

    fun trainDecisionTreeModel(
        trainingData: List<InputRow>,
        testingData: List<InputRow>,
        attributeGenerations: Int,
        attributePoolSize: Int,
        treeDepth: Int
    ): TrainedModel {
        logger.info("Environment: ${languageClassifierProperties.environment}")
        val modelId = UUID.randomUUID().toString()

        logger.info("Training ${ModelType.DECISION_TREE} model, id=$modelId")
        val model: MultiClassifier = MultiClassifier.learnDecisionTree(
            trainingData,
            treeDepth,
            attributeGenerations,
            attributePoolSize
        )

        return saveAndEvaluateModel(
            modelId = modelId,
            model = model,
            trainingData = trainingData,
            testingData = testingData
        )
    }

    fun trainAdaptiveBoostingModel(
        trainingData: List<InputRow>,
        testingData: List<InputRow>,
        attributeGenerations: Int,
        attributePoolSize: Int,
        ensembleSize: Int
    ): TrainedModel {
        val modelId = UUID.randomUUID().toString()

        logger.info("Training ${ModelType.ADAPTIVE_BOOSTING_TREE} model, id=$modelId")
        val model: MultiClassifier = MultiClassifier.learnAdaBoost(
            trainingData,
            ensembleSize,
            attributeGenerations,
            attributePoolSize,
        )
        return saveAndEvaluateModel(modelId, model, trainingData, testingData)
    }

    private fun saveAndEvaluateModel(
        modelId: String,
        model: MultiClassifier,
        trainingData: List<InputRow>,
        testingData: List<InputRow>
    ): TrainedModel {
        val learnerFile = "$DATA_PATH$MODEL_PREFIX$modelId$MODEL_SUFFIX"
        model.saveTo(learnerFile)

        // evaluate learner
        val trainingAccuracyPercent: Double = 100 * (1 - model.errorRateUnWeighted(trainingData))
        logger.info("Training accuracy: $trainingAccuracyPercent for model $modelId")

        val testingAccuracyPercent: Double = 100 * (1 - model.errorRateUnWeighted(testingData))
        logger.info("Testing accuracy: $testingAccuracyPercent for model $modelId")

        val trainedModel = TrainedModel(
            modelId = modelId,
            description = model.description,
            trainingAccuracyPercentage = trainingAccuracyPercent,
            testingAccuracyPercentage = testingAccuracyPercent
        )
        loadedModelsMetadata[modelId] = trainedModel
        return trainedModel
    }

    fun models(): List<TrainedModel> {
        return loadedModelsMetadata.values.toList()
    }

    fun getModelById(id: String): Decider {
        val model = loadedModelsMetadata.values
            .firstOrNull { it.modelId == id }
            ?: throw IllegalArgumentException("Model $id not found")

        return Decider.loadFromFile("${DATA_PATH}${MODEL_PREFIX}${model.modelId}${MODEL_SUFFIX}")
    }
}