package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents

class MmrStatusIsGenerallyAvailable : MolecularEvaluationFunction {

    override fun noMolecularRecordEvaluation() =
        EvaluationFactory.fail("No molecular data to determine microsatellite instability (MSI) status", "No molecular data to determine MSI status")

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        return when (molecular.characteristics.isMicrosatelliteUnstable) {
            null -> {
                EvaluationFactory.fail("Unknown microsatellite instability (MSI) status", "Unknown MSI status")
            }

            true, false -> {
                EvaluationFactory.pass(
                    "Microsatellite instability (MSI) status is known",
                    "MSI status is known",
                    inclusionEvents = setOf(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
                )
            }
        }
    }
}