package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.doid.DoidModel

class IsEligibleForTreatmentLines(private val doidModel: DoidModel, private val lines: List<Int>) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        if (record.clinical().tumor().doids()?.flatMap { doidModel.doidWithParents(it) }?.toSet()
                ?.contains(DoidConstants.COLORECTAL_CANCER_DOID) != true
        ) {
            EvaluationFactory.undetermined("Treatment line determination is currently only supported for colorectal cancer")
        }

        val treatmentLine = determineTreatmentLine(record)
        val message = "Patient determined to be eligible for line $treatmentLine"

        return if (treatmentLine in lines) EvaluationFactory.pass(message) else EvaluationFactory.fail(message)
    }

    companion object {
        private fun determineTreatmentLine(record: PatientRecord): Int {
            val allTreatmentCategories =
                record.clinical().treatmentHistory().flatMap(TreatmentHistoryEntry::categories)

            return when {
                allTreatmentCategories.none { it == TreatmentCategory.CHEMOTHERAPY || it == TreatmentCategory.IMMUNOTHERAPY } -> 1

                allTreatmentCategories.contains(TreatmentCategory.TARGETED_THERAPY) -> 3

                else -> 2
            }
        }
    }
}