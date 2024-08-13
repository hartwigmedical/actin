package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.soc.MolecularDecisions
import com.hartwig.actin.doid.DoidModel

class AnyGeneHasDriverEventWithApprovedTherapy(
    private val genes: List<String>,
    val doidModel: DoidModel,
    private val evaluationFunctionFactory: EvaluationFunctionFactory
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val isLungCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_CANCER_DOID)
        val isColorectalCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.COLORECTAL_CANCER_DOID)

        return when {
            isLungCancer -> HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(genes.toSet(), emptySet()).evaluate(record)
            isColorectalCancer -> hasMolecularEventWithSocForCRC(record)
            !record.molecularHistory.hasMolecularData() -> EvaluationFactory.fail("No molecular data")

            else -> {
                EvaluationFactory.undetermined(
                    "Driver events in genes with approved therapy are currently not determined",
                    "Undetermined if there are driver events with approved therapy"
                )
            }
        }
    }

    private fun hasMolecularEventWithSocForCRC(record: PatientRecord): Evaluation {
        val functions = MolecularDecisions.nonWildTypeMolecularDecisions.map { evaluationFunctionFactory.create(it) }
        return Or(functions).evaluate(record).copy(inclusionMolecularEvents = emptySet(), exclusionMolecularEvents = emptySet())
    }
}