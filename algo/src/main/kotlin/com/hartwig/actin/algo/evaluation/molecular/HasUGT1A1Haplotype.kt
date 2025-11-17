package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene
import java.time.LocalDate

class HasUGT1A1Haplotype(private val haplotypeToFind: String, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge, true) {

    override fun noMolecularTestEvaluation(): Evaluation {
        return EvaluationFactory.undetermined(
            "No molecular data to determine UGT1A1 haplotype",
            isMissingMolecularResultForEvaluation = true
        )
    }

    override fun evaluate(test: MolecularTest): Evaluation {
        val pharmaco = test.pharmaco.firstOrNull { it.gene == PharmacoGene.UGT1A1 }
            ?: return EvaluationFactory.undetermined("UGT1A1 haplotype undetermined", isMissingMolecularResultForEvaluation = true)

        return if (hasUGT1A1Type(pharmaco, haplotypeToFind)) {
            EvaluationFactory.pass("Has UGT1A1 type $haplotypeToFind", inclusionEvents = setOf(haplotypeToFind))
        } else {
            EvaluationFactory.fail("Does not have required UGT1A1 type $haplotypeToFind")
        }
    }

    private fun hasUGT1A1Type(pharmacoEntry: PharmacoEntry, haplotypeToFind: String): Boolean {
        return pharmacoEntry.gene == PharmacoGene.UGT1A1 &&
                pharmacoEntry.haplotypes.any { it.toHaplotypeString().equals(haplotypeToFind, ignoreCase = true) }
    }
}