package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasKnownCnsMetastases(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val isPrimaryBrainOrGliomaTumor =
                DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.BRAIN_CANCER_DOID)

            return when {
                (hasCnsLesions == true || hasBrainLesions == true) && !isPrimaryBrainOrGliomaTumor -> {
                    EvaluationFactory.pass("Has CNS metastases")
                }

                hasCnsLesions == true || hasBrainLesions == true -> {
                    EvaluationFactory.undetermined("Has CNS lesions but unsure if considered CNS metastases because of primary brain cancer")
                }

                (hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true) && !isPrimaryBrainOrGliomaTumor -> {
                    val message = "Has suspected CNS metastases"
                    EvaluationFactory.warn(message)
                }

                hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true -> {
                    val message = "Has suspected CNS lesions but unsure if considered metastases because of primary brain cancer"
                    EvaluationFactory.undetermined(message)
                }

                (hasCnsLesions == null || hasBrainLesions == null) && isPrimaryBrainOrGliomaTumor -> {
                    EvaluationFactory.undetermined("Has primary brain cancer hence undetermined if patient considers to have CNS metastases")
                }

                hasCnsLesions == null || hasBrainLesions == null -> {
                    val message = "Undetermined if CNS metastases present (data missing)"
                    EvaluationFactory.undetermined(message)
                }

                else -> EvaluationFactory.fail("No known CNS metastases present")
            }
        }
    }
}