package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import java.time.LocalDate

class HasRecentlyReceivedMedicationOfApproximateCategory internal constructor(
    private val selector: MedicationSelector, private val categoryToFind: String,
    private val minStopDate: LocalDate
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        if (minStopDate.isBefore(record.clinical().patient().registrationDate())) {
            return EvaluationFactory.undetermined(
                "Required stop date prior to registration date for recent medication usage evaluation of $categoryToFind",
                "Recent $categoryToFind medication"
            )
        }
        val medications = selector.activeOrRecentlyStoppedWithCategory(record.clinical().medications(), categoryToFind, minStopDate)
        if (medications.isNotEmpty()) {
            val names = medications.map { it.name() }
            EvaluationFactory.pass(
                "Patient recently received medication " + concat(names) + ", which belong(s) to category " + categoryToFind,
                "Recent $categoryToFind medication"
            )
        }
        return EvaluationFactory.fail(
            "Patient has not recently received medication of category $categoryToFind",
            "No recent $categoryToFind medication"
        )
    }
}