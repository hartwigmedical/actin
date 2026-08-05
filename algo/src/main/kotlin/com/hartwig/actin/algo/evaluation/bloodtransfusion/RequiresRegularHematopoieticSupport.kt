package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.medication.medicationNotProvided
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.medication.AtcTree
import java.time.LocalDate

class RequiresRegularHematopoieticSupport(
    private val atcTree: AtcTree,
    private val minDate: LocalDate,
    private val maxDate: LocalDate,
    private val bloodTransfusionLabels: EvaluationLabels.BloodTransfusion,
    private val medicationLabels: EvaluationLabels.Medication
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        for (transfusion in record.bloodTransfusions) {
            if (transfusion.date.isAfter(minDate) && transfusion.date.isBefore(maxDate)) {
                return EvaluationFactory.pass(bloodTransfusionLabels.requiresRegularHematopoieticSupportPassTransfusion(transfusion.product))
            }
        }
        val resolvedCategories = hematopoieticMedicationCategories(atcTree)
        val medications = record.medications ?: return medicationNotProvided(medicationLabels)
        val filteredMedications = medications
            .filter { activeBetweenDates(it) }
            .filter { it.atc?.chemicalSubGroup in resolvedCategories }
            .map { it.name }
        return if (filteredMedications.isNotEmpty()) {
            EvaluationFactory.pass(
                bloodTransfusionLabels.requiresRegularHematopoieticSupportPassMedication(concatLowercaseWithCommaAndAnd(filteredMedications))
            )
        } else
            EvaluationFactory.fail(bloodTransfusionLabels.requiresRegularHematopoieticSupportFail())
    }

    private fun activeBetweenDates(medication: Medication): Boolean {
        val start = medication.startDate
        val stop = medication.stopDate
        val startedBetweenDates = start != null && start.isAfter(minDate) && start.isBefore(maxDate)
        val stoppedBetweenDates = stop != null && stop.isAfter(minDate) && stop.isBefore(maxDate)
        val runningBetweenDates = start != null && start.isBefore(minDate) && (stop == null || stop.isAfter(minDate))
        return startedBetweenDates || stoppedBetweenDates || runningBetweenDates
    }

    companion object {
        fun hematopoieticMedicationCategories(atcTree: AtcTree) = setOf("B03XA", "L03AA").map { atcTree.resolve(it) }.toSet()
    }
}