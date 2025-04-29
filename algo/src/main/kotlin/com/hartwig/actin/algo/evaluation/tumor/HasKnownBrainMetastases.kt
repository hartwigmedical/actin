package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasKnownBrainMetastases(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val isPrimaryBrainOrGliomaTumor =
                DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.BRAIN_CANCER_DOID)

            return when {
                hasBrainLesions == true && !isPrimaryBrainOrGliomaTumor -> {
                    EvaluationFactory.pass("Has brain metastases")
                }

                hasBrainLesions == true -> {
                    EvaluationFactory.undetermined("Has brain lesions but unsure if considered metastases because of primary brain cancer")
                }

                hasSuspectedBrainLesions == true && !isPrimaryBrainOrGliomaTumor -> {
                    val message = "Has suspected brain metastases"
                    EvaluationFactory.warn(message)
                }

                hasSuspectedBrainLesions == true -> {
                    EvaluationFactory.undetermined("Has suspected brain lesions but unsure if considered metastases because of primary brain cancer")
                }

                hasBrainLesions == null && isPrimaryBrainOrGliomaTumor -> {
                    EvaluationFactory.undetermined("Has primary brain cancer hence undetermined if patient considers to have brain metastases")
                }

                hasBrainLesions == null -> {
                    val message = "Undetermined if brain metastases present (brain lesions data missing)"
                    EvaluationFactory.undetermined(message)
                }

                else -> EvaluationFactory.fail("No known brain metastases present")
            }
        }
    }
}