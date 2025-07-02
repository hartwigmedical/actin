package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.createFullExpandedDoidTree
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.soc.MolecularDecisions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

private val EXCLUDED_CRC_TUMOR_DOIDS = setOf(
    DoidConstants.RECTUM_NEUROENDOCRINE_NEOPLASM_DOID,
    DoidConstants.NEUROENDOCRINE_TUMOR_DOID,
    DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID
)

class AnyGeneHasDriverEventWithApprovedTherapy(
    private val genes: Set<String>?,
    val doidModel: DoidModel,
    private val evaluationFunctionFactory: EvaluationFunctionFactory,
    private val maxTestAge: LocalDate? = null
) : MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(record: PatientRecord): Evaluation {
        val isNsclc = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
        val tumorDoids = createFullExpandedDoidTree(doidModel, record.tumor.doids)
        val isColorectalCancer =
            DoidConstants.COLORECTAL_CANCER_DOID in tumorDoids && (EXCLUDED_CRC_TUMOR_DOIDS intersect tumorDoids).isEmpty()

        return when {
            record.molecularHistory.molecularTests.isEmpty() -> EvaluationFactory.fail("No molecular data")

            isNsclc -> {
                val evaluation = HasMolecularDriverEventInNsclc(
                    genesToInclude = genes?.toSet(),
                    genesToExclude = emptySet(),
                    maxTestAge = maxTestAge,
                    warnForMatchesOutsideGenesToInclude = false,
                    withAvailableSoc = true
                ).evaluate(record)

                if (evaluation.result in setOf(
                        EvaluationResult.UNDETERMINED,
                        EvaluationResult.FAIL
                    ) && genes?.let { !NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_ANY_LINE.containsAll(genes) } ?: false
                ) {
                    val unevaluatedGenes = genes!!.subtract(NSCLC_DRIVER_GENES_WITH_AVAILABLE_SOC_ANY_LINE)
                    EvaluationFactory.undetermined("Possible presence of driver events for gene(s) ${Format.concat(unevaluatedGenes)} could not be determined")
                } else {
                    evaluation
                }
            }

            isColorectalCancer -> hasMolecularEventWithSocForCRC(record)

            else -> {
                EvaluationFactory.undetermined("Undetermined if there are driver events with approved therapy")
            }
        }
    }

    private fun hasMolecularEventWithSocForCRC(record: PatientRecord): Evaluation {
        val functions = MolecularDecisions.nonWildTypeMolecularDecisions.map { evaluationFunctionFactory.create(it) }
        return Or(functions).evaluate(record).copy(inclusionMolecularEvents = emptySet(), exclusionMolecularEvents = emptySet())
    }
}