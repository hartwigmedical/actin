package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.util.ApplicationConfig
import java.time.LocalDate

class HasHadRecentResection internal constructor(private val minDate: LocalDate) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasHadResectionAfterMinDate = false
        var hasHadResectionAfterMoreLenientMinDate = false
        var mayHaveHadResectionAfterMinDate = false
        for (treatment in record.clinical().priorTumorTreatments()) {
            val isPastMinDate = isAfterDate(minDate, treatment.startYear(), treatment.startMonth())
            val isPastMoreLenientMinDate = isAfterDate(minDate.minusWeeks(2), treatment.startYear(), treatment.startMonth())
            val isResection =
                treatment.name().lowercase(ApplicationConfig.LOCALE).contains(RESECTION_KEYWORD.lowercase(ApplicationConfig.LOCALE))
            val isPotentialResection = treatment.categories().contains(TreatmentCategory.SURGERY) && treatment.name().isEmpty()
            if (isResection) {
                if (isPastMinDate == null) {
                    mayHaveHadResectionAfterMinDate = true
                }
                if (isPastMinDate != null && isPastMinDate) {
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
                EvaluationFactory.pass("Patient has had a recent resection", "Has had recent resection")
            }

            hasHadResectionAfterMoreLenientMinDate -> {
                EvaluationFactory.warn("Patient has had a reasonably recent resection", "Has had reasonably recent resection")
            }

            mayHaveHadResectionAfterMinDate -> {
                EvaluationFactory.undetermined("Patient may have had a recent resection", "Unknown if has had recent resection")
            }

            else -> {
                EvaluationFactory.fail("Patient has not had a recent resection", "Has not had recent resection")
            }
        }
    }

    companion object {
        const val RESECTION_KEYWORD = "resection"
    }
}