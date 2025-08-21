package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest

class MolecularResultsAreKnownForGene(private val gene: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (record.molecularTests.isEmpty()) {
            return EvaluationFactory.undetermined("No molecular data")
        }

        val orangeMolecular = MolecularHistory(record.molecularTests).latestOrangeMolecularRecord()
        if (orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME && orangeMolecular.containsTumorCells) {
            return EvaluationFactory.pass("WGS results available for $gene")
        }

        if (orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_TARGETED && orangeMolecular.containsTumorCells) {
            val geneIsTested = orangeMolecular.drivers.copyNumbers
                .any { it.gene == gene }
            return if (geneIsTested) {
                EvaluationFactory.pass("OncoAct tumor NGS panel results available for $gene")
            } else {
                EvaluationFactory.warn("Unsure if gene $gene results are available within performed OncoAct tumor NGS panel")
            }
        }

        if (isGeneTestedInPanel(record.molecularTests)) {
            return EvaluationFactory.pass("Panel results available for $gene")
        }

        val (indeterminateIhcTestsForGene, conclusiveIhcTestsForGene) = record.ihcTests
            .filter { it.item == gene }
            .partition(IhcTest::impliesPotentialIndeterminateStatus)

        return when {
            conclusiveIhcTestsForGene.isNotEmpty() -> {
                EvaluationFactory.pass("$gene tested before in IHC test")
            }

            orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME -> {
                EvaluationFactory.undetermined(
                    "WGS performed containing $gene but biopsy contained insufficient tumor cells for analysis"
                )
            }

            orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_TARGETED -> {
                EvaluationFactory.undetermined(
                    "OncoAct tumor NGS panel performed containing $gene but biopsy contained " +
                            "insufficient tumor cells for analysis"
                )
            }

            indeterminateIhcTestsForGene.isNotEmpty() -> {
                EvaluationFactory.undetermined("$gene IHC result available but indeterminate status")
            }

            else -> {
                EvaluationFactory.recoverableFail("$gene not tested")
            }
        }
    }

    private fun isGeneTestedInPanel(molecularTests: List<MolecularTest>): Boolean {
        return MolecularHistory(molecularTests).allPanels().any { it.testsGene(gene, any("Test coverage of")) }
    }
}