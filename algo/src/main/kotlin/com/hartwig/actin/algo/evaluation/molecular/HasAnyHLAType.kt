package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele

class HasAnyHLAType(
    private val hlaAllelesToFind: Set<String>, private val matchOnHlaGroup: Boolean = false
) : MolecularEvaluationFunction(true) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val immunology = test.immunology ?: return EvaluationFactory.undetermined(
            "HLA type not tested",
            isMissingMolecularResultForEvaluation = true
        )
        
        if (!immunology.isReliable) {
            return EvaluationFactory.undetermined("HLA typing unreliable", isMissingMolecularResultForEvaluation = true)
        }

        val isMatch: (HlaAllele) -> Boolean = if (matchOnHlaGroup) {
            { allele -> hlaAllelesToFind.any { group -> allele.name.startsWith(group) } }
        } else {
            { allele -> hlaAllelesToFind.contains(allele.name) }
        }

        val matchingHlaAlleles = immunology.hlaAlleles.filter(isMatch)

        if (!test.hasSufficientQuality) {
            val matchedHlaAlleles = matchingHlaAlleles.map { it.name }
            return when {
                matchedHlaAlleles.isNotEmpty() -> {
                    EvaluationFactory.warn(
                        "Has required HLA type ${Format.concatLowercaseWithCommaAndAnd(matchedHlaAlleles)} however undetermined " +
                                "whether allele is present in tumor",
                        inclusionEvents = matchedHlaAlleles.map { "HLA-${it}" }.toSet()
                    )
                }

                else -> {
                    EvaluationFactory.fail("Does not have HLA type ${Format.concatLowercaseWithCommaAndOr(hlaAllelesToFind)}")
                }
            }
        }

        if (matchingHlaAlleles.isEmpty()) {
            return EvaluationFactory.fail("Does not have HLA type ${Format.concatLowercaseWithCommaAndOr(hlaAllelesToFind)}")
        }

        val matchingHlaAlellesString = Format.concatLowercaseWithCommaAndAnd(matchingHlaAlleles.map { it.name })
        val inclusionEvents = matchingHlaAlleles.map { "HLA-${it.name}" }.toSet()

        val matchingAllelesUnmodifiedInTumor = matchingHlaAlleles.filter { hlaAllele ->
            val alleleIsPresentInTumor = hlaAllele.tumorCopyNumber?.let { it >= 0.5 } == true
            val alleleHasSomaticMutations = hlaAllele.hasSomaticMutations
            alleleIsPresentInTumor && alleleHasSomaticMutations == false
        }

        if (matchingAllelesUnmodifiedInTumor.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Has HLA type $matchingHlaAlellesString (allele present without somatic variants in tumor)",
                inclusionEvents = inclusionEvents
            )
        }

        val hasSomaticMutationInMatchingAllele = matchingHlaAlleles.any { it.hasSomaticMutations == true }
        val hasLowTumorCopyNumberInMatchingAllele = matchingHlaAlleles.any { it.tumorCopyNumber?.let { cn -> cn < 0.5 } == true }

        if (hasSomaticMutationInMatchingAllele) {
            return EvaluationFactory.warn(
                "Has required HLA type $matchingHlaAlellesString but somatic mutation present in this allele in tumor",
                inclusionEvents = inclusionEvents
            )
        }

        if (hasLowTumorCopyNumberInMatchingAllele) {
            return EvaluationFactory.warn(
                "Has required HLA type $matchingHlaAlellesString but allele has low copy number in tumor",
                inclusionEvents = inclusionEvents
            )
        }

        return EvaluationFactory.pass(
            "Has HLA type $matchingHlaAlellesString",
            inclusionEvents = inclusionEvents
        )
    }
}