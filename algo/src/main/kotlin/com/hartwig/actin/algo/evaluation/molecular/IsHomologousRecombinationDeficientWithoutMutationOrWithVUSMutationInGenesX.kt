package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import java.time.LocalDate

class IsHomologousRecombinationDeficientWithoutMutationOrWithVUSMutationInGenesX(
    private val genesToFind: Set<String>, maxTestAge: LocalDate? = null
) : MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val isHRD = test.characteristics.homologousRecombination?.isDeficient

        with(HomologousRecombinationDeficiencyGeneSummary.createForDrivers(test.drivers)) {
            val genesToFindWithDeletionOrPartialDel = genesInGenesToFind(hrdGenesWithDeletionOrPartialDel)
            val genesToFindWithBiallelicCav = genesInGenesToFind(hrdGenesWithBiallelicCav)
            val genesToFindWithNonBiallelicCav = genesInGenesToFind(hrdGenesWithNonBiallelicCav)

            val warnEvaluations = mutableSetOf<String>()
            addToWarnEvaluations(
                warnEvaluations,
                "non-cancer-associated variant biallelic high driver(s)",
                genesInGenesToFind(hrdGenesWithBiallelicNonCavHighDriver)
            )
            addToWarnEvaluations(
                warnEvaluations,
                "non-cancer-associated variant biallelic non-high driver(s)",
                genesInGenesToFind(hrdGenesWithBiallelicNonCavNonHighDriver)
            )
            addToWarnEvaluations(
                warnEvaluations,
                "non-cancer-associated variant non-biallelic high driver(s)",
                genesInGenesToFind(hrdGenesWithNonBiallelicNonCavHighDriver)
            )
            addToWarnEvaluations(
                warnEvaluations,
                "non-cancer-associated variant non-biallelic non-high driver(s)",
                genesInGenesToFind(hrdGenesWithNonBiallelicNonCavNonHighDriver)
            )
            addToWarnEvaluations(warnEvaluations, "homozygous disruption", genesInGenesToFind(hrdGenesWithHomozygousDisruption))
            addToWarnEvaluations(warnEvaluations, "non-homozygous disruption", genesInGenesToFind(hrdGenesWithNonHomozygousDisruption))

            return when {
                isHRD == null && hrdGenesWithBiallelicDriver.isNotEmpty() -> {
                    EvaluationFactory.undetermined(
                        "Unknown HRD status but biallelic drivers in HR genes",
                        isMissingMolecularResultForEvaluation = true
                    )
                }

                isHRD == null && hrdGenesWithNonBiallelicDriver.isNotEmpty() -> {
                    EvaluationFactory.undetermined(
                        "Unknown HRD status but non-biallelic drivers in HR genes",
                        isMissingMolecularResultForEvaluation = true
                    )
                }

                isHRD == null -> {
                    EvaluationFactory.fail("Unknown HRD status", isMissingMolecularResultForEvaluation = true)
                }

                isHRD == false -> {
                    EvaluationFactory.fail("Tumor is not HRD")
                }

                genesToFindWithBiallelicCav.isNotEmpty() || genesToFindWithNonBiallelicCav.isNotEmpty() -> {
                    EvaluationFactory.fail(
                        "Tumor is HRD with " +
                                "${concat(genesToFindWithNonBiallelicCav + genesToFindWithBiallelicCav)} cancer-associated variant"
                    )
                }

                genesToFindWithDeletionOrPartialDel.isNotEmpty() -> {
                    EvaluationFactory.fail("Tumor is HRD with ${concat(genesToFindWithDeletionOrPartialDel)} deletion or partial deletion")
                }

                warnEvaluations.isNotEmpty() -> {
                    warnEvaluation(warnEvaluations)
                }

                hrdGenesWithNonBiallelicDriver.isNotEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn("Tumor is HRD but with only non-biallelic drivers in HR genes")
                }

                hrdGenesWithNonBiallelicDriver.isEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn("Tumor is HRD but without drivers in HR genes")
                }

                else -> {
                    EvaluationFactory.pass("Tumor is HRD without any variants in ${concat(genesToFind)}")
                }
            }
        }
    }

    private fun genesInGenesToFind(genes: Iterable<String>): Set<String> {
        return genes.intersect(genesToFind)
    }

    private fun addToWarnEvaluations(warnEvaluations: MutableSet<String>, driverType: String, foundGenes: Set<String>) {
        if (foundGenes.isNotEmpty()) {
            warnEvaluations.add(driverType + " in " + concat(foundGenes))
        }
    }

    private fun warnEvaluation(driverTypeInFoundGenes: Set<String>): Evaluation {
        return EvaluationFactory.warn("Tumor is HRD with ${concat(driverTypeInFoundGenes)} which could be pathogenic")
    }
}