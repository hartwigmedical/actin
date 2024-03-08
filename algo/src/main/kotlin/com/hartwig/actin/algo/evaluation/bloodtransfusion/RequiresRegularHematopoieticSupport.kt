package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.AtcTree
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.date
import com.hartwig.actin.clinical.datamodel.Medication
import java.time.LocalDate

class RequiresRegularHematopoieticSupport(private val atcTree: AtcTree, private val minDate: LocalDate, private val maxDate: LocalDate) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val inBetweenRange = "between " + date(minDate) + " and " + date(maxDate)
        for (transfusion in record.bloodTransfusions) {
            if (transfusion.date.isAfter(minDate) && transfusion.date.isBefore(maxDate)) {
                return EvaluationFactory.pass(
                    "Patient has had blood transfusion $inBetweenRange",
                    "Has received recent hematopoietic support"
                )
            }
        }
        val resolvedCategories = hematopoieticMedicationCategories(atcTree)
        val medications = record.medications
            .filter { activeBetweenDates(it) }
            .filter { it.atc?.chemicalSubGroup in resolvedCategories }
            .map { it.name }
        return if (medications.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has had medications " + concat(medications) + " " + inBetweenRange,
                "Has received recent hematopoietic support"
            )
        } else
            EvaluationFactory.fail(
                "Patient has not received blood transfusions or hematopoietic medication $inBetweenRange",
                "Has not received recent hematopoietic support"
            )
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