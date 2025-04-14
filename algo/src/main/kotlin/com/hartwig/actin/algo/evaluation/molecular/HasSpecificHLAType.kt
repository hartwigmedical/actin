package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import java.time.LocalDate

class HasSpecificHLAType(private val hlaAlleleToFind: String, maxTestAge: LocalDate? = null, private val matchOnHlaGroup: Boolean = false) :
    MolecularEvaluationFunction(maxTestAge, true) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val molecular = test as? MolecularRecord ?: return EvaluationFactory.undetermined(
            "Cannot evaluate HLA type without WGS",
            isMissingMolecularResultForEvaluation = true
        )
        val immunology = molecular.immunology
        if (!immunology.isReliable) {
            return EvaluationFactory.undetermined("HLA typing unreliable")
        }

        val isMatch: (HlaAllele) -> Boolean = if (matchOnHlaGroup) {
            { it.name.startsWith(hlaAlleleToFind) }
        } else {
            { it.name == hlaAlleleToFind }
        }

        if (!test.hasSufficientQuality) {
            return when {
                immunology.hlaAlleles.any(isMatch) -> {
                    EvaluationFactory.undetermined(
                        "Has required HLA type $hlaAlleleToFind however undetermined whether allele is present in tumor"
                    )
                }

                else -> {
                    EvaluationFactory.fail("Does not have HLA type $hlaAlleleToFind")
                }
            }
        }

        val (matchingAllelesUnmodifiedInTumor, matchingAllelesModifiedInTumor) = immunology.hlaAlleles
            .filter(isMatch)
            .partition { hlaAllele ->
                val alleleIsPresentInTumor = hlaAllele.tumorCopyNumber >= 0.5
                val alleleHasSomaticMutations = hlaAllele.hasSomaticMutations
                alleleIsPresentInTumor && !alleleHasSomaticMutations
            }
        return when {
            matchingAllelesUnmodifiedInTumor.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Has HLA type $hlaAlleleToFind (allele present without somatic variants in tumor)",
                    inclusionEvents = setOf("HLA-$hlaAlleleToFind")
                )
            }

            matchingAllelesModifiedInTumor.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Has required HLA type $hlaAlleleToFind but somatic mutation present in this allele in tumor",
                    inclusionEvents = setOf("HLA-$hlaAlleleToFind")
                )
            }

            else -> {
                EvaluationFactory.fail("Does not have HLA type $hlaAlleleToFind")
            }
        }
    }
}