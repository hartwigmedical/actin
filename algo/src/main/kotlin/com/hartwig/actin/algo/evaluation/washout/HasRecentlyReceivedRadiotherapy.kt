package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import java.time.YearMonth

class HasRecentlyReceivedRadiotherapy(private val referenceYear: Int, private val referenceMonth: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val radiotherapyEvaluations =
            record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }
            .map {
                it.startYear?.let { year ->
                    val month = it.startMonth
                    year >= referenceYear && (month == null || month >= referenceMonth ||
                            YearMonth.of(year, month).isAfter(YearMonth.of(referenceYear, referenceMonth)))
                }
            }

        return if (radiotherapyEvaluations.any { it == true }) {
            EvaluationFactory.pass(
                "Patient has recently received radiotherapy - pay attention to washout period",
                "Has recently received radiotherapy - pay attention to washout period"
            )
        } else if (radiotherapyEvaluations.any { it == null }) {
            EvaluationFactory.pass("Has received prior radiotherapy with unknown date - if recent: pay attention to washout period",
                "Has received prior radiotherapy with unknown date - pay attention to washout period")
        } else {
            EvaluationFactory.fail("Patient has not recently received radiotherapy - no concerns related to washout period",
                "No recent radiotherapy")
        }
    }
}