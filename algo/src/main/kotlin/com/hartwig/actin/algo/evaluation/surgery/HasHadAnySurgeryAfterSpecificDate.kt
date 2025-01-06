package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format.date
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import java.time.LocalDate

class HasHadAnySurgeryAfterSpecificDate(private val minDate: LocalDate, private val evaluationDate: LocalDate) : EvaluationFunction {

    private enum class SurgeryEvent {
        HAS_FINISHED_SURGERY_BETWEEN_MIN_AND_EVAL,
        HAS_CANCELLED_SURGERY,
        HAS_PLANNED_SURGERY_AFTER_EVAL,
        HAS_UNEXPECTED_SURGERY,
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val summary = record.surgeries.filter { minDate.isBefore(it.endDate) }
            .map { surgery ->
                val isFuture = evaluationDate.isBefore(surgery.endDate)
                when {
                    surgery.status == SurgeryStatus.CANCELLED -> SurgeryEvent.HAS_CANCELLED_SURGERY
                    isFuture && surgery.status == SurgeryStatus.PLANNED -> SurgeryEvent.HAS_PLANNED_SURGERY_AFTER_EVAL
                    !isFuture && surgery.status == SurgeryStatus.FINISHED -> SurgeryEvent.HAS_FINISHED_SURGERY_BETWEEN_MIN_AND_EVAL
                    else -> SurgeryEvent.HAS_UNEXPECTED_SURGERY
                }
            }
            .toSet()

        val surgicalTreatmentsOccurredAfterMinDate = record.oncologicalHistory
            .filter { it.categories().contains(TreatmentCategory.SURGERY) }
            .map { isAfterDate(minDate, it.startYear, it.startMonth) }

        return when {
            SurgeryEvent.HAS_FINISHED_SURGERY_BETWEEN_MIN_AND_EVAL in summary -> {
                EvaluationFactory.pass("Has had surgery after ${date(minDate)}")
            }

            surgicalTreatmentsOccurredAfterMinDate.any { it == true } -> {
                EvaluationFactory.pass("Has had surgery after ${date(minDate)}")
            }

            SurgeryEvent.HAS_PLANNED_SURGERY_AFTER_EVAL in summary -> {
                EvaluationFactory.warn("Has surgery planned")
            }

            SurgeryEvent.HAS_UNEXPECTED_SURGERY in summary -> {
                EvaluationFactory.warn("Has potential recent surgery")
            }

            surgicalTreatmentsOccurredAfterMinDate.any { it == null } -> {
                EvaluationFactory.undetermined("Undetermined if previous surgery is recent")
            }

            SurgeryEvent.HAS_CANCELLED_SURGERY in summary -> {
                EvaluationFactory.fail("Recent surgery got cancelled")
            }

            else -> {
                EvaluationFactory.fail("Has not received surgery after ${date(minDate)}")
            }
        }
    }
}