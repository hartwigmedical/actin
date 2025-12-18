package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele

class HasAnyHLAType(
    private val hlaAllelesToFind: Set<String>, private val matchOnHlaGroup: Boolean = false
) : MolecularEvaluationFunction(true) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val molecular = MolecularHistory(listOf(test)).latestOrangeMolecularRecord() ?: return EvaluationFactory.undetermined(
            "HLA type not tested",
            isMissingMolecularResultForEvaluation = true
        )
        
        val immunology = molecular.immunology!!
        if (!immunology.isReliable) {
            return EvaluationFactory.undetermined("HLA typing unreliable", isMissingMolecularResultForEvaluation = true)
        }

        val isMatch: (HlaAllele) -> Boolean = { allele ->
            val alleleToMatch = "${allele.gene.removePrefix("HLA-")}*${allele.alleleGroup}:${allele.hlaProtein}"
            if (matchOnHlaGroup) {
                hlaAllelesToFind.any { group -> alleleToMatch.startsWith(group) }
            } else {
                hlaAllelesToFind.contains(alleleToMatch)
            }
        }

        if (!test.hasSufficientQuality) {
            val matchedHlaAlleles = immunology.hlaAlleles.filter(isMatch).map { it.event }.toSet()
            return when {
                matchedHlaAlleles.isNotEmpty() -> {
                    EvaluationFactory.warn(
                        "Has required HLA type ${Format.concatLowercaseWithCommaAndAnd(matchedHlaAlleles)} however undetermined " +
                                "whether allele is present in tumor",
                        inclusionEvents = matchedHlaAlleles
                    )
                }

                else -> {
                    EvaluationFactory.fail("Does not have HLA type ${Format.concatLowercaseWithCommaAndOr(hlaAllelesToFind)}")
                }
            }
        }

        val matchingHlaAlleles = immunology.hlaAlleles.filter(isMatch)

        val (matchingAllelesUnmodifiedInTumor, matchingAllelesModifiedInTumor) = matchingHlaAlleles
            .partition { hlaAllele ->
                val alleleIsPresentInTumor = hlaAllele.tumorCopyNumber >= 0.5
                val alleleHasSomaticMutations = hlaAllele.hasSomaticMutations
                alleleIsPresentInTumor && !alleleHasSomaticMutations
            }

        val matchingHlaAlellesString = Format.concatLowercaseWithCommaAndAnd(matchingHlaAlleles.map { it.event })

        return when {
            matchingAllelesUnmodifiedInTumor.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Has HLA type $matchingHlaAlellesString (allele present without somatic variants in tumor)",
                    inclusionEvents = matchingHlaAlleles.map { it.event }.toSet()
                )
            }

            matchingAllelesModifiedInTumor.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Has required HLA type $matchingHlaAlellesString but somatic mutation present in this allele in tumor",
                    inclusionEvents = matchingHlaAlleles.map { it.event }.toSet()
                )
            }

            else -> {
                EvaluationFactory.fail("Does not have HLA type ${Format.concatLowercaseWithCommaAndOr(hlaAllelesToFind)}")
            }
        }
    }
}