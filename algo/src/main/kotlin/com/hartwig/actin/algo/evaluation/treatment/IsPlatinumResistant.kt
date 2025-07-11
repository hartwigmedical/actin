package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType

// recurs within 6 months

class IsPlatinumResistant : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumTreatmentEntries = record.oncologicalHistory.asSequence().filter { entry ->
            entry.allTreatments().filterIsInstance<DrugTreatment>()
                .any { treatment -> treatment.drugs.any { it.drugTypes.contains(DrugType.PLATINUM_COMPOUND) } }
        }.toSet()

        val hasProgressionOnPlatinum = platinumTreatmentEntries.any { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true }

        return when {
            hasProgressionOnPlatinum -> EvaluationFactory.undetermined("Undetermined if patient is platinum resistant")
            platinumTreatmentEntries.isNotEmpty() -> EvaluationFactory.fail("Not platinum resistant (no progression on platinum treatment)")
            else -> EvaluationFactory.fail("Not platinum resistant (no progression on platinum treatment)")
        }
    }
}