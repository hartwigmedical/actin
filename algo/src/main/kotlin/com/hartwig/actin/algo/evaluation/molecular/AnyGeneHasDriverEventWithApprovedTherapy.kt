package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.createFullExpandedDoidTree
import com.hartwig.actin.algo.soc.MolecularDecisions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

private val EXCLUDED_CRC_TUMOR_DOIDS = setOf(
    DoidConstants.RECTUM_NEUROENDOCRINE_NEOPLASM_DOID,
    DoidConstants.NEUROENDOCRINE_TUMOR_DOID,
    DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID
)

class AnyGeneHasDriverEventWithApprovedTherapy(
    private val genes: List<String>,
    val doidModel: DoidModel,
    private val evaluationFunctionFactory: EvaluationFunctionFactory,
    private val maxTestAge: LocalDate? = null
) : MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(record: PatientRecord): Evaluation {
        val isLungCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_CANCER_DOID)
        val tumorDoids = createFullExpandedDoidTree(doidModel, record.tumor.doids)
        val isColorectalCancer =
            DoidConstants.COLORECTAL_CANCER_DOID in tumorDoids && (EXCLUDED_CRC_TUMOR_DOIDS intersect tumorDoids).isEmpty()

        return when {
            record.molecularHistory.molecularTests.isEmpty() -> EvaluationFactory.fail("No molecular data")
            isLungCancer -> HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(genes.toSet(), emptySet(), maxTestAge).evaluate(record)
            isColorectalCancer -> hasMolecularEventWithSocForCRC(record)

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