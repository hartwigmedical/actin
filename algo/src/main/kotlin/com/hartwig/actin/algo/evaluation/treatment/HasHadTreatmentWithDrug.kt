package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.Therapy
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

class HasHadTreatmentWithDrug(private val drugs: Set<Drug>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val namesToMatch = drugs.map { it.name().lowercase() }.toSet()
        val matchingDrugs = record.clinical().treatmentHistory()
            .flatMap(TreatmentHistoryEntry::treatments)
            .flatMap { (it as? Therapy)?.drugs() ?: emptyList() }
            .filter { it.name().lowercase() in namesToMatch }
            .map(Drug::name)

        val drugList = drugs.joinToString(" or ") { it.name().lowercase() }
        return when {
            matchingDrugs.isNotEmpty() -> {
                EvaluationFactory.pass("Has received treatments with ${concatLowercaseWithAnd(matchingDrugs)}")
            }

            record.clinical().treatmentHistory().any(TreatmentHistoryEntry::isTrial) -> {
                EvaluationFactory.undetermined("Undetermined if received any treatments containing $drugList")
            }

            else -> {
                EvaluationFactory.fail("Has not received any treatments containing $drugList")
            }
        }
    }
}