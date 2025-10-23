package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.MedicationFunctions.createTreatmentHistoryEntriesFromMedications
import com.hartwig.actin.calendar.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithOr
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import java.time.LocalDate

class HasHadTreatmentWithCategoryOfTypesRecently(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?,
    private val minDate: LocalDate,
    private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory =
            record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(
                record.medications?.filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE })

        val treatmentAssessment = effectiveTreatmentHistory.map { treatmentHistoryEntry ->
            val categoryMatch = category in treatmentHistoryEntry.categories()
            val typeMatch = if (types == null) true else treatmentHistoryEntry.matchesTypeFromSet(types)
            val startedPastMinDate = isAfterDate(minDate, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)

            TreatmentAssessment(
                hasHadValidTreatment = categoryMatch && typeMatch == true && startedPastMinDate == true,
                hasPotentiallyValidTreatment = categoryMatch && typeMatch == null && startedPastMinDate == true,
                hasInconclusiveDate = categoryMatch && typeMatch == true && startedPastMinDate == null,
                hasHadTrialAfterMinDate = TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, setOf(category))
                        && startedPastMinDate == true
            )
        }.fold(TreatmentAssessment()) { acc, element -> acc.combineWith(element) }

        val typesAndCategoryString = listOfNotNull(types?.let(::concatItemsWithOr), category.display()).joinToString(" ")

        return when {
            treatmentAssessment.hasHadValidTreatment -> {
                EvaluationFactory.pass("Has received $typesAndCategoryString treatment within requested time frame")
            }

            treatmentAssessment.hasPotentiallyValidTreatment -> {
                EvaluationFactory.undetermined("Has potentially received $typesAndCategoryString treatment within requested time frame - exact drug type of patient's treatment unknown")
            }

            treatmentAssessment.hasInconclusiveDate -> {
                EvaluationFactory.undetermined("Has received $typesAndCategoryString treatment but inconclusive if within requested time frame")
            }

            treatmentAssessment.hasHadTrialAfterMinDate -> {
                EvaluationFactory.undetermined("Undetermined if treatment received in previous trial may have included ${category.display()}")
            }

            else -> EvaluationFactory.fail("Has not had $typesAndCategoryString treatment within requested time frame")
        }
    }
}