package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.certainTreatmentSinceMinDate
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import java.time.LocalDate

class HasHadSpecificTreatmentSinceDate(private val treatment: Treatment, private val minDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingTreatments = record.oncologicalHistory.mapNotNull { entry ->
            TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry) { it.name == treatment.name }
        }
        val predicateDescription = "matching '${treatment.display()}'"
        val formattedMinDate = Format.date(minDate)

        return when {
            matchingTreatments.any { certainTreatmentSinceMinDate(it, minDate) } -> {
                EvaluationFactory.pass("Treatment $predicateDescription administered since $formattedMinDate based on provided treatments")
            }

            matchingTreatments.any { potentialTreatmentSinceMinDate(it, minDate) } -> {
                EvaluationFactory.undetermined(
                    "Treatment $predicateDescription administered with unknown date " +
                            "based on provided treatments"
                )
            }

            matchingTreatments.isNotEmpty() -> {
                EvaluationFactory.fail("All treatments $predicateDescription administered before $formattedMinDate based on provided treatments")
            }

            else -> EvaluationFactory.fail("No treatments $predicateDescription in provided treatments")
        }
    }
}