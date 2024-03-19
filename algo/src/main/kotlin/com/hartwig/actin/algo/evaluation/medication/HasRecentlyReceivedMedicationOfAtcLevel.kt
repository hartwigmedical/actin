package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.AtcLevel
import java.time.LocalDate

class HasRecentlyReceivedMedicationOfAtcLevel(
    private val selector: MedicationSelector,
    private val categoryName: String,
    private val categoryAtcLevels: Set<AtcLevel>,
    private val minStopDate: LocalDate,
    private val onlyCheckSystemic: Boolean
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (minStopDate.isBefore(record.clinical.patient.registrationDate)) {
            return EvaluationFactory.undetermined(
                "Required stop date prior to registration date for recent medication usage evaluation of $categoryName",
                "Recent $categoryName medication"
            )
        }

        val medications = selector.activeOrRecentlyStopped(record.clinical.medications, minStopDate)
            .filter { !onlyCheckSystemic || selector.isSystemic(it) }
            .filter { (it.allLevels() intersect categoryAtcLevels).isNotEmpty() }

        val foundMedicationNames = medications.map { it.name }.filter { it.isNotEmpty() }

        return if (medications.isNotEmpty()) {
            val foundMedicationString = if (foundMedicationNames.isNotEmpty()) ": ${concatLowercaseWithAnd(foundMedicationNames)}" else ""
            EvaluationFactory.recoverablePass(
                "Patient recently received medication$foundMedicationString which belong(s) to category '$categoryName'",
                "Recent $categoryName medication use$foundMedicationString"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient has not recently received medication of category '$categoryName'",
                "No recent $categoryName medication use"
            )
        }

    }
}