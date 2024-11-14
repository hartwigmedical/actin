package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import java.time.LocalDate

class HasSpecificHLAType(private val hlaAlleleToFind: String, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge, true) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val molecular = test as? MolecularRecord ?: return EvaluationFactory.undetermined(
            "Cannot evaluate HLA type without WGS",
            missingGenesForEvaluation = true
        )
        val immunology = molecular.immunology
        if (!immunology.isReliable) {
            return EvaluationFactory.recoverableUndetermined("HLA typing has not been performed reliably", "HLA typing unreliable")
        }

        if (!test.hasSufficientQuality) {
            return when {
                immunology.hlaAlleles.any { it.name == hlaAlleleToFind } -> {
                    EvaluationFactory.undetermined(
                        "Patient has HLA type $hlaAlleleToFind which is equal to required allele type $hlaAlleleToFind, however undetermined whether allele is present in tumor",
                        "Patient has required HLA type, however undetermined whether allele is present in tumor"
                    )
                }

                else -> {
                    EvaluationFactory.fail("Patient does not have HLA type '$hlaAlleleToFind'", "Patient does not have required HLA type")
                }
            }
        }

        val (matchingAllelesUnmodifiedInTumor, matchingAllelesModifiedInTumor) = immunology.hlaAlleles
            .filter { it.name == hlaAlleleToFind }
            .partition { hlaAllele ->
                val alleleIsPresentInTumor = hlaAllele.tumorCopyNumber >= 0.5
                val alleleHasSomaticMutations = hlaAllele.hasSomaticMutations
                alleleIsPresentInTumor && !alleleHasSomaticMutations
            }
        return when {
            matchingAllelesUnmodifiedInTumor.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Patient has HLA type $hlaAlleleToFind which is equal to required allele type $hlaAlleleToFind,"
                            + " this allele is present and without somatic variants in tumor",
                    "Patient has required HLA type",
                    inclusionEvents = setOf("HLA-$hlaAlleleToFind")
                )
            }

            matchingAllelesModifiedInTumor.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Patient has HLA type $hlaAlleleToFind which is equal to required allele type $hlaAlleleToFind,"
                            + " however, somatic mutation found in allele in tumor.",
                    "Patient has required HLA type but somatic mutation present in this allele in tumor",
                    inclusionEvents = setOf("HLA-$hlaAlleleToFind")
                )
            }

            else -> {
                EvaluationFactory.fail("Patient does not have HLA type '$hlaAlleleToFind'", "Patient does not have required HLA type")
            }
        }
    }
}