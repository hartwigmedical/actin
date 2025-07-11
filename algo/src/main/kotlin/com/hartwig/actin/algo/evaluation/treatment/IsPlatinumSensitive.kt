package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType

// recurs more than 6 months after

class IsPlatinumSensitive : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumTreatmentEntries = record.oncologicalHistory.asSequence().filter { entry ->
            entry.allTreatments().filterIsInstance<DrugTreatment>()
                .any { treatment -> treatment.drugs.any { it.drugTypes.contains(DrugType.PLATINUM_COMPOUND) } }
        }.toSet()

        val progressionOnPlatinum = platinumTreatmentEntries.any { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true }

        val progressionOnPlatinumWithinSixMonths = true

        return when {
            progressionOnPlatinumWithinSixMonths -> EvaluationFactory.fail("Is platinum resistant")
            progressionOnPlatinum -> EvaluationFactory.undetermined("Undetermined if patient is platinum sensitive")
            platinumTreatmentEntries.isNotEmpty() -> EvaluationFactory.fail("Not platinum sensitive (no progression on platinum treatment)")
            else -> EvaluationFactory.fail("Not platinum sensitive (no platinum treatment)")
        }
    }
}

// korter dan 6 maanden geleden -> dan weet je dat het resistant is -> fail.