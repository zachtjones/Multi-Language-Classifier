package com.zachjones.languageclassifier.service

import com.zachjones.languageclassifier.entities.DATA_PATH
import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.entities.MODEL_PREFIX
import com.zachjones.languageclassifier.entities.MODEL_SUFFIX
import com.zachjones.languageclassifier.model.types.ModelType
import com.zachjones.languageclassifier.model.types.TrainedModel
import learners.Decider
import learners.MultiClassifier
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.name

@Component
class ModelsService {

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
                trainingAccuracyPercentage = -1.0 // TODO - also persist this data
            )
        }
        loadedModelsMetadata += modelMetadata

        logger.info("Loaded ${loadedModelsMetadata.size} models")
    }

    fun trainDecisionTreeModel(
        trainingData: List<InputRow>,
        attributeGenerations: Int,
        attributePoolSize: Int,
        treeDepth: Int
    ): TrainedModel {
        val modelId = UUID.randomUUID().toString()

        logger.info("Training ${ModelType.DECISION_TREE} model, id=$modelId")
        val model: MultiClassifier = MultiClassifier.learnDecisionTree(
            trainingData,
            treeDepth,
            attributeGenerations,
            attributePoolSize,
            true
        )

        return saveAndEvaluateModel(modelId, model, trainingData)
    }

    fun trainAdaptiveBoostingModel(
        trainingData: List<InputRow>,
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
            true
        )
        return saveAndEvaluateModel(modelId, model, trainingData)
    }

    private fun saveAndEvaluateModel(
        modelId: String,
        model: MultiClassifier,
        trainingData: List<InputRow>
    ): TrainedModel {
        val learnerFile = "$DATA_PATH$MODEL_PREFIX$modelId$MODEL_SUFFIX"
        model.saveTo(learnerFile)

        // evaluate learner
        val accuracyPercent: Double = 100 * (1 - model.errorRateUnWeighted(trainingData))
        logger.info("Training accuracy: $accuracyPercent for model $modelId")

        val trainedModel = TrainedModel(
            modelId = modelId,
            description = model.description,
            trainingAccuracyPercentage = accuracyPercent
        )
        loadedModelsMetadata[modelId] = trainedModel
        return trainedModel
    }

    fun models(): List<TrainedModel> {
        return loadedModelsMetadata.values.toList()
    }
}