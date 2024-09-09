package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import java.time.LocalDate

class HasHadTreatmentWithCategoryButNotOfTypesRecently(
    private val category: TreatmentCategory, private val ignoreTypes: Set<TreatmentType>,
    private val minDate: LocalDate, private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentAssessment = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val startedPastMinDate = isAfterDate(minDate, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)
            val categoryAndTypeMatch = treatmentHistoryEntry.categories().contains(category)
                    && treatmentHistoryEntry.matchesTypeFromSet(ignoreTypes) != true
            TreatmentFunctions.TreatmentAssessment(
                hasHadValidTreatment = categoryAndTypeMatch && startedPastMinDate == true,
                hasInconclusiveDate = categoryAndTypeMatch && startedPastMinDate == null,
                hasHadTrialAfterMinDate = TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, category)
                        && startedPastMinDate == true
            )
        }.fold(TreatmentFunctions.TreatmentAssessment()) { acc, element -> acc.combineWith(element) }

        val hasActiveOrRecentlyStoppedMedication = record.medications
            ?.filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }

        val hadCancerMedicationWithCategoryButNotOfTypes = hasActiveOrRecentlyStoppedMedication
            ?.any { medication ->
                MedicationFunctions.hasCategory(medication, category) && MedicationFunctions.doesNotHaveIgnoreType(
                    medication,
                    ignoreTypes
                )
            } ?: false

        val ignoringTypesList = concatItems(ignoreTypes)

        return when {
            treatmentAssessment.hasHadValidTreatment || hadCancerMedicationWithCategoryButNotOfTypes -> {
                EvaluationFactory.pass("Has received ${category.display()} treatment ignoring $ignoringTypesList")
            }

            treatmentAssessment.hasInconclusiveDate -> {
                EvaluationFactory.undetermined("Has received ${category.display()} treatment ignoring $ignoringTypesList but inconclusive date")
            }

            treatmentAssessment.hasHadTrialAfterMinDate || hasActiveOrRecentlyStoppedMedication?.any { it.isTrialMedication } == true -> {
                EvaluationFactory.undetermined(
                    "Patient has participated in a trial recently, inconclusive ${category.display()} treatment",
                    "Inconclusive ${category.display()} treatment due to trial participation"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Has not had recent ${category.display()} treatment ignoring $ignoringTypesList"
                )
            }
        }
    }
}