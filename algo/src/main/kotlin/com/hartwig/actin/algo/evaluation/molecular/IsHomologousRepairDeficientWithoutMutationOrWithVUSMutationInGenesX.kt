package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import java.time.LocalDate

class IsHomologousRepairDeficientWithoutMutationOrWithVUSMutationInGenesX(
    private val genesToFind: Set<String>, maxTestAge: LocalDate? = null
) : MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val isHRD = test.characteristics.isHomologousRepairDeficient

        with(HomologousRepairDeficiencyGeneSummary.createForDrivers(test.drivers)) {
            val genesToFindWithDeletionOrPartialLoss = genesInGenesToFind(hrdGenesWithDeletionOrPartialLoss)
            val genesToFindWithBiallelicHotspot = genesInGenesToFind(hrdGenesWithBiallelicHotspot)
            val genesToFindWithNonBiallelicHotspot = genesInGenesToFind(hrdGenesWithNonBiallelicHotspot)

            val warnEvaluations = mutableSetOf<String>()
            addToWarnEvaluations(
                warnEvaluations,
                "non-hotspot biallelic high driver(s)",
                genesInGenesToFind(hrdGenesWithBiallelicNonHotspotHighDriver)
            )
            addToWarnEvaluations(
                warnEvaluations,
                "non-hotspot biallelic non-high driver(s)",
                genesInGenesToFind(hrdGenesWithBiallelicNonHotspotNonHighDriver)
            )
            addToWarnEvaluations(
                warnEvaluations,
                "non-hotspot non-biallelic high driver(s)",
                genesInGenesToFind(hrdGenesWithNonBiallelicNonHotspotHighDriver)
            )
            addToWarnEvaluations(
                warnEvaluations,
                "non-hotspot non-biallelic non-high driver(s)",
                genesInGenesToFind(hrdGenesWithNonBiallelicNonHotspotNonHighDriver)
            )
            addToWarnEvaluations(warnEvaluations, "homozygous disruption", genesInGenesToFind(hrdGenesWithHomozygousDisruption))
            addToWarnEvaluations(warnEvaluations, "non-homozygous disruption", genesInGenesToFind(hrdGenesWithNonHomozygousDisruption))

            return when {
                isHRD == null && hrdGenesWithBiallelicDriver.isNotEmpty() -> {
                    EvaluationFactory.undetermined("Unknown HRD status but biallelic drivers in HR genes - an HRD test may be recommended")
                }

                isHRD == null && hrdGenesWithNonBiallelicDriver.isNotEmpty() -> {
                    EvaluationFactory.undetermined("Unknown HRD status but non-biallelic drivers in HR genes - an HRD test may be recommended")
                }

                isHRD == null -> {
                    EvaluationFactory.fail("Unknown HRD status")
                }

                isHRD == false -> {
                    EvaluationFactory.fail("Tumor is not HRD")
                }

                genesToFindWithBiallelicHotspot.isNotEmpty() || genesToFindWithNonBiallelicHotspot.isNotEmpty() -> {
                    EvaluationFactory.fail("Tumor is HRD with ${concat(genesToFindWithNonBiallelicHotspot + genesToFindWithBiallelicHotspot)} hotspot")
                }

                genesToFindWithDeletionOrPartialLoss.isNotEmpty() -> {
                    EvaluationFactory.fail("Tumor is HRD with ${concat(genesToFindWithDeletionOrPartialLoss)} deletion or partial loss")
                }

                warnEvaluations.isNotEmpty() -> {
                    warnEvaluation(warnEvaluations)
                }

                hrdGenesWithNonBiallelicDriver.isNotEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn("Tumor is HRD (but with only non-biallelic drivers in HR genes)")
                }

                hrdGenesWithNonBiallelicDriver.isEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn("Tumor is HRD (but without drivers in HR genes)")
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