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
        requestedLesionCategory: String,
        tumorDoids: Set<String>?,
        doidModel: DoidModel
    ): Evaluation {
        val doidToMatch = when (requestedLesionCategory) {
            TumorDetails.BONE -> DoidConstants.BONE_CANCER_DOID
            TumorDetails.LIVER -> DoidConstants.LIVER_CANCER_DOID
            TumorDetails.LYMPH_NODE -> DoidConstants.LYMPH_NODE_CANCER_DOID
            TumorDetails.LUNG -> DoidConstants.LUNG_CANCER_DOID
            else -> null
        }
        val primaryTumorIsOfRequestedLesionCategory = if (doidToMatch != null) DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, doidToMatch) else false
        val messageString = requestedLesionCategory.lowercase()

        return when {
            hasLesions == true && !primaryTumorIsOfRequestedLesionCategory  -> {
                EvaluationFactory.pass("Has $messageString metastases")
            }

            hasLesions == true -> {
                EvaluationFactory.undetermined("Has $messageString lesions but unsure if considered metastases because of primary $messageString cancer")
            }

            hasSuspectedLesions == true && !primaryTumorIsOfRequestedLesionCategory -> {
                EvaluationFactory.warn("Has suspected $messageString metastases")
            }

            hasSuspectedLesions == true -> {
                EvaluationFactory.undetermined("Has suspected $messageString lesions but unsure if considered metastases because of primary $messageString cancer")
            }

            hasLesions == null && primaryTumorIsOfRequestedLesionCategory -> {
                EvaluationFactory.undetermined("Has primary $messageString cancer but undetermined if patient may have $messageString metastases")
            }

            hasLesions == null -> EvaluationFactory.undetermined("Missing $messageString metastasis data")

            else -> {
                EvaluationFactory.fail("No $messageString metastases")
            }
        }
    }
}