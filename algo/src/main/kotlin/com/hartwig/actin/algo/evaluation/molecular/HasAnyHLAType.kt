package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import java.time.LocalDate

class HasAnyHLAType(
    private val hlaAllelesToFind: Set<String>,
    maxTestAge: LocalDate? = null,
    private val matchOnHlaGroup: Boolean = false
) :
    MolecularEvaluationFunction(maxTestAge, true) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val molecular = test as? MolecularRecord ?: return EvaluationFactory.undetermined(
            "HLA type not tested",
            isMissingMolecularResultForEvaluation = true
        )
        val immunology = molecular.immunology
        if (!immunology.isReliable) {
            return EvaluationFactory.undetermined("HLA typing unreliable")
        }

        val isMatch: (HlaAllele) -> Boolean = if (matchOnHlaGroup) {
            { allele -> hlaAllelesToFind.any { group -> allele.name.startsWith(group) } }
        } else {
            { allele -> hlaAllelesToFind.contains(allele.name) }
        }

        if (!test.hasSufficientQuality) {
            val matchedHlaAlleles = immunology.hlaAlleles.filter(isMatch).map { it.name }
            return when {
                matchedHlaAlleles.isNotEmpty() -> {
                    EvaluationFactory.warn(
                        "Has required HLA type ${Format.concatLowercaseWithCommaAndAnd(matchedHlaAlleles)} however undetermined " +
                                "whether allele is present in tumor", inclusionEvents = matchedHlaAlleles.map { "HLA-${it}" }.toSet()
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

        val matchingHlaAlellesString = Format.concatLowercaseWithCommaAndAnd(matchingHlaAlleles.map { it.name })

        return when {
            matchingAllelesUnmodifiedInTumor.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Has HLA type $matchingHlaAlellesString (allele present without somatic variants in tumor)",
                    inclusionEvents = matchingHlaAlleles.map { "HLA-${it.name}" }.toSet()
                )
            }

            matchingAllelesModifiedInTumor.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Has required HLA type $matchingHlaAlellesString but somatic mutation present in this allele in tumor",
                    inclusionEvents = matchingHlaAlleles.map { "HLA-${it.name}" }.toSet()
                )
            }

            else -> {
                EvaluationFactory.fail("Does not have HLA type ${Format.concatLowercaseWithCommaAndOr(hlaAllelesToFind)}")
            }
        }
    }
}