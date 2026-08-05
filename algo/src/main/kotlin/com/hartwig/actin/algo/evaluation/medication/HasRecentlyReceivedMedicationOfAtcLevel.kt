package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import java.time.LocalDate

class HasRecentlyReceivedMedicationOfAtcLevel(
    private val selector: MedicationSelector,
    private val categoryName: String,
    private val categoryAtcLevels: Set<AtcLevel>,
    private val minStopDate: LocalDate,
    private val labels: EvaluationLabels.Medication
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (minStopDate.isBefore(record.patient.registrationDate)) {
            return EvaluationFactory.undetermined(labels.hasRecentlyReceivedMedicationOfAtcLevelUndetermined(categoryName))
        }

        val medications = record.medications ?: return medicationNotProvided(labels)
        val activeOrRecentlyStopped = selector.activeOrRecentlyStopped(medications, minStopDate)
            .filter { (it.allLevels() intersect categoryAtcLevels).isNotEmpty() }

        val foundMedicationNames = activeOrRecentlyStopped.map { it.name }.filter { it.isNotEmpty() }

        return if (activeOrRecentlyStopped.isNotEmpty()) {
            val foundMedicationString =
                if (foundMedicationNames.isNotEmpty()) concatLowercaseWithCommaAndAnd(foundMedicationNames) else ""
            EvaluationFactory.recoverablePass(labels.hasRecentlyReceivedMedicationOfAtcLevelRecoverablePass(categoryName, foundMedicationString))
        } else {
            EvaluationFactory.recoverableFail(labels.hasRecentlyReceivedMedicationOfAtcLevelRecoverableFail(categoryName))
        }
    }
}