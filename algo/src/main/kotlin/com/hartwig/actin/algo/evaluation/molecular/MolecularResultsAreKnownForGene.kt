package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest

class MolecularResultsAreKnownForGene(private val gene: String, private val labels: EvaluationLabels.Molecular) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val orangeMolecular = MolecularHistory(record.molecularTests).latestOrangeMolecularRecord()
        if (orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME && orangeMolecular.hasSufficientQuality) {
            return EvaluationFactory.pass(labels.molecularResultsAreKnownForGenePassWgs(gene))
        }

        if (orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_TARGETED && orangeMolecular.hasSufficientQuality) {
            val geneIsTested = orangeMolecular.drivers.copyNumbers
                .any { it.gene == gene }
            return if (geneIsTested) {
                EvaluationFactory.pass(labels.molecularResultsAreKnownForGenePassOncoact(gene))
            } else {
                EvaluationFactory.warn(labels.molecularResultsAreKnownForGeneWarnOncoactUnsure(gene))
            }
        }

        if (isGeneTestedInPanel(record.molecularTests)) {
            return EvaluationFactory.pass(labels.molecularResultsAreKnownForGenePassPanel(gene))
        }

        val (indeterminateIhcTestsForGene, conclusiveIhcTestsForGene) = record.ihcTests
            .filter { it.item == gene }
            .partition(IhcTest::impliesPotentialIndeterminateStatus)

        return when {
            conclusiveIhcTestsForGene.isNotEmpty() -> {
                EvaluationFactory.pass(labels.molecularResultsAreKnownForGenePassIhc(gene))
            }

            orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME -> {
                EvaluationFactory.undetermined(labels.molecularResultsAreKnownForGeneUndeterminedWgs(gene))
            }

            orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_TARGETED -> {
                EvaluationFactory.undetermined(labels.molecularResultsAreKnownForGeneUndeterminedOncoact(gene))
            }

            indeterminateIhcTestsForGene.isNotEmpty() -> {
                EvaluationFactory.undetermined(labels.molecularResultsAreKnownForGeneUndeterminedIhc(gene))
            }

            else -> {
                EvaluationFactory.recoverableFail(labels.molecularResultsAreKnownForGeneRecoverableFail(gene))
            }
        }
    }

    private fun isGeneTestedInPanel(molecularTests: List<MolecularTest>): Boolean {
        return MolecularHistory(molecularTests).allPanels().any { it.testsGene(gene, any("Test coverage of")) }
    }
}