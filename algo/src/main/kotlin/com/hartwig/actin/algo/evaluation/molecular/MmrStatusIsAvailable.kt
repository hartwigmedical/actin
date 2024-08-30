package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents

class MmrStatusIsAvailable : MolecularEvaluationFunction {

    override fun noMolecularRecordEvaluation() =
        EvaluationFactory.fail("No molecular data to determine mismatch repair (MMR) status", "No molecular data to determine MMR status")

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        return when (molecular.characteristics.isMicrosatelliteUnstable) {
            null -> {
                EvaluationFactory.fail("Unknown mismatch repair (MMR) status", "Unknown MMR status")
            }

            true, false -> {
                EvaluationFactory.pass(
                    "Mismatch repair (MMR) status is known",
                    "MMR status is known",
                    inclusionEvents = setOf(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
                )
            }
        }
    }
}