package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology

private const val HLA_PRESENCE_MIN_COPY_NUMBER = 0.5

class HasAnyHLAType(
    private val hlaAllelesToFind: Set<String>,
    private val matchOnHlaGroup: Boolean = false
) : MolecularEvaluationFunction(true) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val immunology = test.immunology
        return when {
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
    }

    private fun evaluateReliableImmunology(test: MolecularTest, immunology: MolecularImmunology): Evaluation {
        val matchingHlaAlleles = immunology.hlaAlleles.filter(::isMatch)
        val requiredTypes = Format.concatLowercaseWithCommaAndOr(hlaAllelesToFind)

        return when {
            matchingHlaAlleles.isEmpty() -> EvaluationFactory.fail("Does not have HLA type $requiredTypes")
            !test.hasSufficientQuality -> {
                val matchedEvents = matchingHlaAlleles.map(HlaAllele::event).toSet()
                EvaluationFactory.warn(
                    "Has required HLA type ${Format.concatLowercaseWithCommaAndAnd(matchedEvents)} however undetermined whether allele is present in tumor",
                    inclusionEvents = matchedEvents
                )
            }

            else -> evaluateMatchingAllelesInSufficientQualityTest(matchingHlaAlleles)
        }
    }

    private fun evaluateMatchingAllelesInSufficientQualityTest(matchingHlaAlleles: List<HlaAllele>): Evaluation {
        val matchingAllelesString = Format.concatLowercaseWithCommaAndAnd(matchingHlaAlleles.map(HlaAllele::event))
        val inclusionEvents = matchingHlaAlleles.map(HlaAllele::event).toSet()

        return when {
            matchingHlaAlleles.any { allele ->
                allele.tumorCopyNumber?.let { it >= HLA_PRESENCE_MIN_COPY_NUMBER } == true && allele.hasSomaticMutations == false
            } -> EvaluationFactory.pass(
                "Has HLA type $matchingAllelesString (allele present without somatic variants in tumor)",
                inclusionEvents = inclusionEvents
            )

            matchingHlaAlleles.any { it.hasSomaticMutations == true } -> EvaluationFactory.warn(
                "Has required HLA type $matchingAllelesString but somatic mutation present in this allele in tumor",
                inclusionEvents = inclusionEvents
            )

            matchingHlaAlleles.any { it.tumorCopyNumber?.let { cn -> cn < HLA_PRESENCE_MIN_COPY_NUMBER } == true } -> EvaluationFactory.warn(
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