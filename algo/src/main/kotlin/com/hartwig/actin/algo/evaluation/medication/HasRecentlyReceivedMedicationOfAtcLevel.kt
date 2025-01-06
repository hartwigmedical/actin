package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import java.time.LocalDate

class HasRecentlyReceivedMedicationOfAtcLevel(
    private val selector: MedicationSelector,
    private val categoryName: String,
    private val categoryAtcLevels: Set<AtcLevel>,
    private val minStopDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (minStopDate.isBefore(record.patient.registrationDate)) {
            return EvaluationFactory.undetermined("Recent $categoryName medication use undetermined (required stop date prior to registration date)")
        }

        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val activeOrRecentlyStopped = selector.activeOrRecentlyStopped(medications, minStopDate)
            .filter { (it.allLevels() intersect categoryAtcLevels).isNotEmpty() }

        val foundMedicationNames = activeOrRecentlyStopped.map { it.name }.filter { it.isNotEmpty() }

        return if (activeOrRecentlyStopped.isNotEmpty()) {
            val foundMedicationString =
                if (foundMedicationNames.isNotEmpty()) ": ${concatLowercaseWithAnd(foundMedicationNames)}" else ""
            EvaluationFactory.recoverablePass("Recent $categoryName medication use$foundMedicationString")
        } else {
            EvaluationFactory.recoverableFail("No recent $categoryName medication use")
        }
    }
}