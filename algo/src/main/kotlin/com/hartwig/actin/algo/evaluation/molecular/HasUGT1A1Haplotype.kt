package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoGene
import java.time.LocalDate

class HasUGT1A1Haplotype(private val haplotypeToFind: String, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge, true) {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val pharmaco = molecular.pharmaco.firstOrNull { it.gene == PharmacoGene.UGT1A1 }
            ?: return EvaluationFactory.undetermined("UGT1A1 haplotype is undetermined", "UGT1A1 haplotype undetermined")

        return if (hasUGT1A1Type(pharmaco, haplotypeToFind)) {
            EvaluationFactory.pass(
                "Patient has required UGT1A1 type $haplotypeToFind",
                "Patient has required UGT1A1 type",
                inclusionEvents = setOf(haplotypeToFind)
            )
        } else {
            EvaluationFactory.fail("Patient does not have required HLA type $haplotypeToFind", "Patient does not have required HLA type")
        }
    }

    private fun hasUGT1A1Type(pharmacoEntry: PharmacoEntry, hapolotypeToFind: String): Boolean {
        return pharmacoEntry.gene == PharmacoGene.UGT1A1 &&
                pharmacoEntry.haplotypes.any { it.toHaplotypeString().lowercase() == hapolotypeToFind.lowercase() }
    }
}