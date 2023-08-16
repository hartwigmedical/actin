package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format.date
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import java.time.LocalDate

class HasHadAnySurgeryAfterSpecificDate(private val minDate: LocalDate, private val evaluationDate: LocalDate) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasFinishedSurgeryBetweenMinAndEval = false
        var hasUnexpectedSurgeryBetweenMinAndEval = false
        var hasCancelledSurgeryBetweenMinAndEval = false
        var hasPlannedSurgeryAfterEval = false
        var hasUnexpectedSurgeryAfterEval = false
        var hasCancelledSurgeryAfterEval = false
        for (surgery in record.clinical().surgeries()) {
            if (minDate.isBefore(surgery.endDate())) {
                if (minDate.isBefore(surgery.endDate())) {
                    if (evaluationDate.isBefore(surgery.endDate())) {
                        if (surgery.status() == SurgeryStatus.CANCELLED) {
                            hasCancelledSurgeryAfterEval = true
                        } else if (surgery.status() == SurgeryStatus.PLANNED) {
                            hasPlannedSurgeryAfterEval = true
                        } else {
                            hasUnexpectedSurgeryAfterEval = true
                        }
                    } else {
                        if (surgery.status() == SurgeryStatus.FINISHED) {
                            hasFinishedSurgeryBetweenMinAndEval = true
                        } else if (surgery.status() == SurgeryStatus.CANCELLED) {
                            hasCancelledSurgeryBetweenMinAndEval = true
                        } else {
                            hasUnexpectedSurgeryBetweenMinAndEval = true
                        }
                    }
                }
            }
        }
        if (hasFinishedSurgeryBetweenMinAndEval) {
            return EvaluationFactory.pass(
                "Patient has had surgery after " + date(minDate),
                "Surgery after " + date(minDate)
            )
        } else if (hasPlannedSurgeryAfterEval) {
            return EvaluationFactory.pass(
                "Patient has surgery planned after " + date(minDate),
                "Patient has surgery planned"
            )
        } else if (hasUnexpectedSurgeryAfterEval || hasUnexpectedSurgeryBetweenMinAndEval) {
            return EvaluationFactory.warn(
                "Patient may have had or may get surgery after " + date(minDate),
                "Potential recent surgery"
            )
        }

        val surgicalTreatmentsOccurredAfterMinDate = record.clinical().treatmentHistory()
            .filter { it.categories().contains(TreatmentCategory.SURGERY) }
            .map { isAfterDate(minDate, it.startYear(), it.startMonth()) }

        return when {
            surgicalTreatmentsOccurredAfterMinDate.any { it == true } -> {
                EvaluationFactory.pass(
                    "Patient has had surgery after " + date(minDate),
                    "Has had surgery after " + date(minDate)
                )
            }

            surgicalTreatmentsOccurredAfterMinDate.any { it == null } -> {
                EvaluationFactory.undetermined(
                    "Patient has had surgery but undetermined how long ago",
                    "Undetermined if previous surgery is recent"
                )
            }

            hasCancelledSurgeryAfterEval || hasCancelledSurgeryBetweenMinAndEval -> {
                EvaluationFactory.fail("Recent surgery got cancelled", "No recent surgery")
            }

            else -> {
                EvaluationFactory.fail("Patient has not received surgery in past nr of months", "No recent surgery")
            }
        }
    }
}