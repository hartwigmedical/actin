package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import java.time.LocalDate

class HasHadRecentResection(private val minDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        var hasHadResectionAfterMinDate = false
        var hasHadResectionAfterMoreLenientMinDate = false
        var mayHaveHadResectionAfterMinDate = false

        for (treatmentHistoryEntry in record.oncologicalHistory) {
            val isPastMinDate = isAfterDate(minDate, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)
            val isPastMoreLenientMinDate =
                isAfterDate(minDate.minusWeeks(2), treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)
            val isResection =
                treatmentHistoryEntry.treatments.any { it.name.lowercase().contains(RESECTION_KEYWORD) }
            val isPotentialResection = treatmentHistoryEntry.treatments.any {
                it.categories().contains(TreatmentCategory.SURGERY) && it.name.isEmpty()
            }
            if (isResection) {
                if (isPastMinDate == null) {
                    mayHaveHadResectionAfterMinDate = true
                } else if (isPastMinDate) {
                    hasHadResectionAfterMinDate = true
                }
                if (isPastMoreLenientMinDate != null && isPastMoreLenientMinDate) {
                    hasHadResectionAfterMoreLenientMinDate = true
                }
            }
            if (isPastMinDate != null && isPastMinDate && isPotentialResection) {
                mayHaveHadResectionAfterMinDate = true
            }
        }

        return when {
            hasHadResectionAfterMinDate -> {
                EvaluationFactory.pass("Has had recent resection")
            }

            hasHadResectionAfterMoreLenientMinDate -> {
                EvaluationFactory.warn("Has had reasonably recent resection")
            }

            mayHaveHadResectionAfterMinDate -> {
                EvaluationFactory.undetermined("May have had a recent resection")
            }

            else -> {
                EvaluationFactory.fail("Has not had recent resection")
            }
        }
    }

    companion object {
        const val RESECTION_KEYWORD = "resection"
    }
}