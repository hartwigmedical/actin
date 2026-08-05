package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene

class HasUGT1A1Haplotype(private val haplotypeToFind: String, labels: EvaluationLabels.Molecular) :
    MolecularEvaluationFunction(useInsufficientQualityRecords = true, labels = labels) {

    override fun noMolecularTestEvaluation(): Evaluation {
        return EvaluationFactory.undetermined(
            labels.hasUgt1a1HaplotypeUndeterminedNoData(),
            isMissingMolecularResultForEvaluation = true
        )
    }

    override fun evaluate(test: MolecularTest): Evaluation {
        val pharmaco = test.pharmaco.firstOrNull { it.gene == PharmacoGene.UGT1A1 }
            ?: return EvaluationFactory.undetermined(labels.hasUgt1a1HaplotypeUndetermined(), isMissingMolecularResultForEvaluation = true)

        return if (hasUGT1A1Type(pharmaco, haplotypeToFind)) {
            EvaluationFactory.pass(labels.hasUgt1a1HaplotypePass(haplotypeToFind), inclusionEvents = setOf(haplotypeToFind))
        } else {
            EvaluationFactory.fail(labels.hasUgt1a1HaplotypeFail(haplotypeToFind))
        }
    }

    private fun hasUGT1A1Type(pharmacoEntry: PharmacoEntry, haplotypeToFind: String): Boolean {
        return pharmacoEntry.gene == PharmacoGene.UGT1A1 &&
                pharmacoEntry.haplotypes.any { it.toHaplotypeString().equals(haplotypeToFind, ignoreCase = true) }
    }
}