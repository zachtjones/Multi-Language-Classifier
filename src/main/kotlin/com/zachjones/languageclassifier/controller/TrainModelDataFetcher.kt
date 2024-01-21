package com.zachjones.languageclassifier.controller

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.zachjones.languageclassifier.model.DgsConstants
import com.zachjones.languageclassifier.model.types.ModelType
import com.zachjones.languageclassifier.model.types.TrainModelInput
import com.zachjones.languageclassifier.model.types.TrainedModel
import com.zachjones.languageclassifier.model.types.TrainedModelsResult
import com.zachjones.languageclassifier.service.ModelsService
import com.zachjones.languageclassifier.service.TrainingDataService

@DgsComponent
class TrainModelDataFetcher(
    private val trainingDataService: TrainingDataService,
    private val modelsService: ModelsService
) {

    @DgsMutation(field = DgsConstants.MUTATION.TrainModel)
    fun trainModel(@InputArgument input: TrainModelInput): TrainedModel {

        val trainingData = trainingDataService.getTrainingDataSet(input.trainingDataId)
        val testingData = trainingDataService.getTrainingDataSet(input.testingDataId)

        val attributeGenerations = input.attributeGenerations.also {
            require(it in 1..200) {
                "Attribute generations must be between 1 and 200"
            }
        }
        val attributePoolSize = input.attributePoolSize.also {
            require(it in 10..1000) {
                "Attribute pool size must be between 10 and 1000"
            }
        }

        return when (input.modelType) {
            ModelType.DECISION_TREE -> {
                require(input.ensembleSize == null) {
                    "ensembleSize should be null when modelType=DECISION_TREE"
                }
                val treeDepth = requireNotNull(input.treeDepth){
                    "Tree depth required for DECISION_TREE"
                }
                modelsService.trainDecisionTreeModel(
                    trainingData = trainingData,
                    testingData = testingData,
                    attributeGenerations = attributeGenerations,
                    attributePoolSize = attributePoolSize,
                    treeDepth = treeDepth
                )
            }
            ModelType.ADAPTIVE_BOOSTING_TREE -> {
                require(input.treeDepth == null) {
                    "tree depth should be null when modelType=ADAPTIVE_BOOSTING_TREE, as this always uses depth=1"
                }
                val ensembleSize = requireNotNull(input.ensembleSize) {
                    "Ensemble size required for ADAPTIVE_BOOSTING_TREE"
                }
                modelsService.trainAdaptiveBoostingModel(
                    trainingData = trainingData,
                    testingData = testingData,
                    attributeGenerations = attributeGenerations,
                    attributePoolSize = attributePoolSize,
                    ensembleSize = ensembleSize
                )
            }
        }
    }

    @DgsQuery(field = DgsConstants.QUERY.Models)
    fun models(): TrainedModelsResult {
        return TrainedModelsResult(
            models = modelsService.models()
        )
    }
}