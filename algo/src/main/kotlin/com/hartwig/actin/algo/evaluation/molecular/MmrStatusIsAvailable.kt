package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import java.time.LocalDate

class MmrStatusIsAvailable(maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge) {

    override fun noMolecularRecordEvaluation() =
        EvaluationFactory.fail("No molecular data to determine MMR status")

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        return when (molecular.characteristics.isMicrosatelliteUnstable) {
            null -> {
                EvaluationFactory.recoverableFail("No MMR status result", missingGenesForEvaluation = true)
            }

            true, false -> {
                EvaluationFactory.pass(
                    "MMR status is known",
                    inclusionEvents = setOf(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
                )
            }
        }
    }
}