package com.hartwig.actin.algo.evaluation.molecular

import com.google.common.collect.Sets.union
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.concatStringsWithAnd
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import java.time.LocalDate

class IsHomologousRepairDeficientWithoutMutationOrWithVUSMutationInGenesX(private val genesToFind: Set<String>, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(maxTestAge) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val hrdGenesWithNonBiallelicHotspot: MutableSet<String> = mutableSetOf()
        val hrdGenesWithBiallelicHotspot: MutableSet<String> = mutableSetOf()
        val hrdGenesWithNonBiallelicNonHotspotHighDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithNonBiallelicNonHotspotNonHighDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithBiallelicNonHotspotHighDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithBiallelicNonHotspotNonHighDriver: MutableSet<String> = mutableSetOf()


        test.drivers.variants.filter { it.gene in MolecularConstants.HRD_GENES && it.isReportable && it.extendedVariantDetails != null }
            .forEach { variant ->
                when {
                    variant.isHotspot && variant.extendedVariantDetails?.isBiallelic == true -> {
                        hrdGenesWithBiallelicHotspot.add(variant.gene)
                    }

                    variant.isHotspot && variant.extendedVariantDetails?.isBiallelic == false -> {
                        hrdGenesWithNonBiallelicHotspot.add(variant.gene)
                    }

                    variant.extendedVariantDetails?.isBiallelic == true && variant.driverLikelihood == DriverLikelihood.HIGH -> {
                        hrdGenesWithBiallelicNonHotspotHighDriver.add(variant.gene)
                    }

                    variant.extendedVariantDetails?.isBiallelic == true -> {
                        hrdGenesWithBiallelicNonHotspotNonHighDriver.add(variant.gene)
                    }

                    variant.driverLikelihood == DriverLikelihood.HIGH && variant.extendedVariantDetails?.isBiallelic == false -> {
                        hrdGenesWithNonBiallelicNonHotspotHighDriver.add(variant.gene)
                    }

                    else -> {
                        hrdGenesWithNonBiallelicNonHotspotNonHighDriver.add(variant.gene)
                    }
                }
            }

        val hrdGenesWithDeletionOrPartialLoss = test.drivers.copyNumbers
            .filter { it.type == CopyNumberType.LOSS && it.gene in MolecularConstants.HRD_GENES }
            .map(GeneAlteration::gene)
            .toSet()
        val hrdGenesWithHomozygousDisruption = test.drivers.homozygousDisruptions.filter { it.gene in MolecularConstants.HRD_GENES }
            .map(GeneAlteration::gene)
            .toSet()
        val hrdGenesWithNonHomozygousDisruption = test.drivers.disruptions
            .filter { it.gene in MolecularConstants.HRD_GENES && it.isReportable }
            .map(GeneAlteration::gene)
            .toSet()

        val hrdGenesWithBiallelicDriver =
            hrdGenesWithBiallelicHotspot + hrdGenesWithBiallelicNonHotspotHighDriver + hrdGenesWithBiallelicNonHotspotNonHighDriver + hrdGenesWithHomozygousDisruption + hrdGenesWithDeletionOrPartialLoss
        val hrdGenesWithNonBiallelicDriver =
            hrdGenesWithNonBiallelicNonHotspotHighDriver + hrdGenesWithNonBiallelicHotspot + hrdGenesWithNonHomozygousDisruption + hrdGenesWithNonBiallelicNonHotspotNonHighDriver
        val isHRD = test.characteristics.isHomologousRepairDeficient

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
                EvaluationFactory.undetermined(
                    "Unknown homologous repair deficiency (HRD) status, but biallelic drivers in HR genes: ${
                        concat(
                            hrdGenesWithBiallelicDriver
                        )
                    } are detected; an HRD test may be recommended",
                    "Unknown HRD status but biallelic drivers in HR genes"
                )
            }

            isHRD == null && hrdGenesWithNonBiallelicDriver.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Unknown homologous repair deficiency (HRD) status, but non-biallelic drivers in HR genes: ${
                        concat(
                            hrdGenesWithNonBiallelicDriver
                        )
                    } are detected; an HRD test may be recommended",
                    "Unknown HRD status but non-biallelic drivers in HR genes"
                )
            }

            isHRD == null -> {
                EvaluationFactory.fail("Unknown homologous repair deficiency (HRD) status", "Unknown HRD status")
            }

            isHRD == false -> {
                EvaluationFactory.fail("No homologous repair deficiency (HRD) detected", "Tumor is not HRD")
            }

            genesToFindWithBiallelicHotspot.isNotEmpty() || genesToFindWithNonBiallelicHotspot.isNotEmpty() -> {
                EvaluationFactory.fail(
                    "Homologous repair deficiency (HRD) detected with ${
                        concat(
                            union(
                                genesToFindWithNonBiallelicHotspot,
                                genesToFindWithBiallelicHotspot
                            )
                        )
                    } hotspot",
                    "Tumor is HRD with ${concat(union(genesToFindWithNonBiallelicHotspot, genesToFindWithBiallelicHotspot))} hotspot"
                )
            }

            genesToFindWithDeletionOrPartialLoss.isNotEmpty() -> {
                EvaluationFactory.fail(
                    "Homologous repair deficiency (HRD) detected with deletion or partial loss in ${
                        concat(
                            genesToFindWithDeletionOrPartialLoss
                        )
                    }",
                    "Tumor is HRD with ${concat(genesToFindWithDeletionOrPartialLoss)} deletion or partial loss"
                )
            }

            warnEvaluations.isNotEmpty() -> {
                warnEvaluation(warnEvaluations)
            }

            hrdGenesWithNonBiallelicDriver.isNotEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                EvaluationFactory.warn(
                    "Homologous repair deficiency (HRD) status detected, together with only non-biallelic drivers in HR genes (${
                        concat(
                            hrdGenesWithNonBiallelicDriver
                        )
                    })",
                    "Tumor is HRD (but with only non-biallelic drivers in HR genes)",
                )
            }

            hrdGenesWithNonBiallelicDriver.isEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                EvaluationFactory.warn(
                    "Homologous repair deficiency (HRD) status detected, without drivers in HR genes",
                    "Tumor is HRD (but without detected drivers in HR genes)",
                )
            }

            else -> {
                EvaluationFactory.pass(
                    "Homologous repair deficiency (HRD) detected, without variants in ${concat(genesToFind)}",
                    "Tumor is HRD without any variants in ${concat(genesToFind)}",
                )
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
        return EvaluationFactory.warn(
            "Homologous repair deficiency (HRD) detected, together with ${concatStringsWithAnd(driverTypeInFoundGenes)} which could be pathogenic",
            "Tumor is HRD with ${concatStringsWithAnd(driverTypeInFoundGenes)} which could be pathogenic"
        )
    }
}