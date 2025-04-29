package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.doid.DoidModel

object TumorMetastasisEvaluator {

    fun evaluate(
        hasLesions: Boolean?,
        hasSuspectedLesions: Boolean?,
        requestedCategoryType: String,
        tumorDoids: Set<String>?,
        doidModel: DoidModel
    ): Evaluation {
        val doidsToMatch = when (requestedCategoryType) {
            TumorDetails.BONE -> DoidConstants.BONE_CANCER_DOID
            TumorDetails.LIVER -> DoidConstants.LIVER_CANCER_DOID
            TumorDetails.LYMPH_NODE -> DoidConstants.LYMPH_NODE_CANCER_DOID
            TumorDetails.LUNG -> DoidConstants.LUNG_CANCER_DOID
            else -> null
        }
        val isPrimaryTumor = if (doidsToMatch != null) DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, doidsToMatch) else false
        val messageType = requestedCategoryType.lowercase()

        return when {
            hasLesions == true && !isPrimaryTumor  -> {
                EvaluationFactory.pass("Has $messageType metastases")
            }

            hasLesions == true -> {
                EvaluationFactory.undetermined("Has $messageType lesions but unsure if considered metastases because of primary $messageType cancer")
            }

            hasSuspectedLesions == true && !isPrimaryTumor -> {
                EvaluationFactory.warn("Has suspected $messageType metastases")
            }

            hasSuspectedLesions == true -> {
                EvaluationFactory.undetermined("Has suspected $messageType lesions but unsure if considered metastases because of primary $messageType cancer")
            }

            hasLesions == null && isPrimaryTumor -> {
                EvaluationFactory.undetermined("Has primary $messageType cancer but undetermined if patient may have $messageType metastases")
            }

            hasLesions == null -> EvaluationFactory.undetermined("Missing $messageType metastasis data")

            else -> {
                EvaluationFactory.fail("No $messageType metastases")
            }
        }
    }
}