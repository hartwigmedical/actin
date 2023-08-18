package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import java.time.LocalDate

class HasRecentlyReceivedMedicationOfCategory(
    private val selector: MedicationSelector, private val categories: Map<String, Set<String>?>, private val minStopDate: LocalDate
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val categoriesToFind = categories.values.first()
        val categoryName = categories.keys.first()

        if (minStopDate.isBefore(record.clinical().patient().registrationDate())) {
            return EvaluationFactory.undetermined(
                "Required stop date prior to registration date for recent medication usage evaluation of $categoryName",
                "Recent $categoryName medication"
            )
        }
        val medications = selector.activeOrRecentlyStoppedWithCategory(record.clinical().medications(), categoriesToFind!!, minStopDate)
        return if (medications.isNotEmpty()) {
            val names = medications.map { it.name() }
            EvaluationFactory.pass(
                "Patient recently received medication " + concatLowercaseWithAnd(names) + ", which belong(s) to category $categoryName",
                "Recent $categoryName medication"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not recently received medication of category $categoryName",
                "No recent $categoryName medication"
            )
        }
    }
}