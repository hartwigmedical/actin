package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import java.time.LocalDate

class HasRecentlyReceivedCancerTherapyWithDrug(
    private val drugsToFind: Set<Drug>, private val interpreter: MedicationStatusInterpreter, private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val lowercaseNamesToFind = drugsToFind.map { it.name.lowercase() }.toSet()

        val medicationsFound = record.medications
            ?.filter {
                (lowercaseNamesToFind.contains(it.name.lowercase()) || lowercaseNamesToFind.contains(it.drug?.name?.lowercase())) && interpreter.interpret(
                    it
                ) == MedicationStatusInterpretation.ACTIVE
            }
            ?.map { it.drug?.name ?: it.name }?.toSet() ?: emptySet()

        val matchingTreatments = record.oncologicalHistory
            .mapNotNull { entry ->
                TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry) {
                    it is DrugTreatment && it.drugs.intersect(drugsToFind).isNotEmpty()
                }
            }
        val treatmentDrugsFound = matchingTreatments.flatMap { it.treatments }
            .flatMap { (it as? DrugTreatment)?.drugs?.intersect(drugsToFind) ?: emptySet() }
            .map { it.name }

        val namesFound = medicationsFound + treatmentDrugsFound

        return when {
            medicationsFound.isNotEmpty() || matchingTreatments.any {
                TreatmentVersusDateFunctions.treatmentSinceMinDate(it, minDate, false)
            } -> {
                EvaluationFactory.pass("Has recently received treatment with medication ${Format.concat(namesFound)} " +
                        "- pay attention to washout period"
                )
            }

            matchingTreatments.any { TreatmentVersusDateFunctions.treatmentSinceMinDate(it, minDate, true) } -> {
                EvaluationFactory.undetermined(
                    "Treatment containing '${Format.concatItemsWithOr(drugsToFind)}' administered with unknown date"
                )
            }

            else -> {
                EvaluationFactory.fail("Has not received recent treatments with name " + Format.concatItemsWithOr(drugsToFind))
            }
        }
    }
}