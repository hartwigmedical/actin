package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology

class HasAnyHLAType(
    private val hlaAllelesToFind: Set<String>,
    private val matchOnHlaGroup: Boolean = false
) : MolecularEvaluationFunction(true) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val immunology = test.immunology
        val evaluation = when {
            immunology == null -> EvaluationFactory.undetermined(
                "HLA type not tested",
                isMissingMolecularResultForEvaluation = true
            )

            !immunology.isReliable -> EvaluationFactory.undetermined(
                "HLA typing unreliable",
                isMissingMolecularResultForEvaluation = true
            )

            else -> evaluateReliableImmunology(test, immunology)
        }
        return evaluation
    }

    private fun evaluateReliableImmunology(test: MolecularTest, immunology: MolecularImmunology): Evaluation {
        val matchingHlaAlleles = immunology.hlaAlleles.filter(::isMatch)
        val requiredTypes = Format.concatLowercaseWithCommaAndOr(hlaAllelesToFind)

        val evaluationWhenInsufficientQuality = if (!test.hasSufficientQuality) {
            val matchedEvents = matchingHlaAlleles.map(HlaAllele::event).toSet()
            if (matchedEvents.isNotEmpty()) {
                EvaluationFactory.warn(
                    "Has required HLA type ${Format.concatLowercaseWithCommaAndAnd(matchedEvents)} however undetermined whether allele is present in tumor",
                    inclusionEvents = matchedEvents
                )
            } else {
                EvaluationFactory.fail("Does not have HLA type $requiredTypes")
            }
        } else {
            null
        }

        return evaluationWhenInsufficientQuality
            ?: if (matchingHlaAlleles.isEmpty()) {
                EvaluationFactory.fail("Does not have HLA type $requiredTypes")
            } else {
                evaluateMatchingAllelesInSufficientQualityTest(matchingHlaAlleles)
            }
    }

    private fun evaluateMatchingAllelesInSufficientQualityTest(matchingHlaAlleles: List<HlaAllele>): Evaluation {
        val matchingAllelesString = Format.concatLowercaseWithCommaAndAnd(matchingHlaAlleles.map(HlaAllele::event))
        val inclusionEvents = matchingHlaAlleles.map(HlaAllele::event).toSet()

        val hasAllelePresentWithoutSomaticVariantsInTumor = matchingHlaAlleles.any { allele ->
            val alleleIsPresentInTumor = allele.tumorCopyNumber?.let { it >= 0.5 } == true
            val alleleHasSomaticMutations = allele.hasSomaticMutations
            alleleIsPresentInTumor && alleleHasSomaticMutations == false
        }

        val hasSomaticMutationInMatchingAllele = matchingHlaAlleles.any { it.hasSomaticMutations == true }
        val hasLowTumorCopyNumberInMatchingAllele = matchingHlaAlleles.any { it.tumorCopyNumber?.let { cn -> cn < 0.5 } == true }

        return when {
            hasAllelePresentWithoutSomaticVariantsInTumor -> EvaluationFactory.pass(
                "Has HLA type $matchingAllelesString (allele present without somatic variants in tumor)",
                inclusionEvents = inclusionEvents
            )

            hasSomaticMutationInMatchingAllele -> EvaluationFactory.warn(
                "Has required HLA type $matchingAllelesString but somatic mutation present in this allele in tumor",
                inclusionEvents = inclusionEvents
            )

            hasLowTumorCopyNumberInMatchingAllele -> EvaluationFactory.warn(
                "Has required HLA type $matchingAllelesString but allele has low copy number in tumor",
                inclusionEvents = inclusionEvents
            )

            else -> EvaluationFactory.pass(
                "Has HLA type $matchingAllelesString",
                inclusionEvents = inclusionEvents
            )
        }
    }

    private fun isMatch(allele: HlaAllele): Boolean {
        val alleleToMatch = "${allele.gene.removePrefix("HLA-")}*${allele.alleleGroup}:${allele.hlaProtein}"
        return if (matchOnHlaGroup) {
            hlaAllelesToFind.any { group -> alleleToMatch.startsWith(group) }
        } else {
            hlaAllelesToFind.contains(alleleToMatch)
        }
    }
}
