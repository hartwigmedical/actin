package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.MedicationFunctions.createTreatmentHistoryEntriesFromMedications
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadTreatmentWithCategoryButNotOfTypes(
    private val category: TreatmentCategory,
    private val ignoreTypes: Set<TreatmentType>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(record.medications)

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            effectiveTreatmentHistory,
            category,
            { historyEntry -> ignoreTypes.none { historyEntry.isOfType(it) == true } }
        )

        val ignoreTypesList = Format.concatItemsWithAnd(ignoreTypes)
        return when {
            treatmentSummary.hasSpecificMatch() -> EvaluationFactory.pass(
                "Has received ${category.display()} ignoring $ignoreTypesList"
            )

            treatmentSummary.hasPossibleTrialMatch() -> EvaluationFactory.undetermined(
                "Undetermined if received ${category.display()} ignoring $ignoreTypesList due to trial participation"
            )

            else -> EvaluationFactory.fail("Has not received ${category.display()} ignoring $ignoreTypesList")
        }
    }
}