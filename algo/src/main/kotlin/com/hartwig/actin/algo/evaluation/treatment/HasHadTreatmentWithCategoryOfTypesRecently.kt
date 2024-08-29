package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import java.time.LocalDate

class HasHadTreatmentWithCategoryOfTypesRecently(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>,
    private val minDate: LocalDate, private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentAssessment = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val startedPastMinDate = isAfterDate(minDate, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)
            val categoryAndTypeMatch = treatmentHistoryEntry.categories().contains(category)
                    && treatmentHistoryEntry.matchesTypeFromSet(types) == true
            TreatmentFunctions.TreatmentAssessment(
                hasHadValidTreatment = categoryAndTypeMatch && startedPastMinDate == true,
                hasInconclusiveDate = categoryAndTypeMatch && startedPastMinDate == null,
                hasHadTrialAfterMinDate = TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, category)
                        && startedPastMinDate == true
            )
        }.fold(TreatmentFunctions.TreatmentAssessment()) { acc, element -> acc.combineWith(element) }

        val typesList = concatItems(types)

        val priorCancerMedication = record.medications
            ?.filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
            ?.filter { medication -> ( medication.treatment?.category?.equals(category) == true && medication.treatment?.drugTypes?.any { types.contains(it) } == true) || medication.isTrialMedication} ?: emptyList()

        return when {
            treatmentAssessment.hasHadValidTreatment || (priorCancerMedication.isNotEmpty() && priorCancerMedication.any { !it.isTrialMedication }) -> {
                EvaluationFactory.pass("Has received $typesList ${category.display()} treatment")
            }

            treatmentAssessment.hasInconclusiveDate -> {
                EvaluationFactory.undetermined("Has received $typesList ${category.display()} treatment but inconclusive date")
            }

            treatmentAssessment.hasHadTrialAfterMinDate || priorCancerMedication.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Patient has participated in a trial recently, inconclusive ${category.display()} treatment",
                    "Inconclusive ${category.display()} treatment due to trial participation"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Has not had recent $typesList ${category.display()} treatment"
                )
            }
        }
    }
}