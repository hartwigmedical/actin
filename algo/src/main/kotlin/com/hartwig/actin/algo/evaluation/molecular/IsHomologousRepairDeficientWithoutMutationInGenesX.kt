package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType

class IsHomologousRepairDeficientWithoutMutationInGenesX(private val genesToFind: Set<String>) : MolecularEvaluationFunction {

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
        val genesToFindWithMutation = genesInGenesToFind(hrdGenesWithBiallelicDriver + hrdGenesWithNonBiallelicDriver)

        return when {
            isHRD == null && hrdGenesWithBiallelicDriver.isNotEmpty() && genesToFindWithMutation.isEmpty() -> {
                EvaluationFactory.undetermined(
                    "Unknown homologous repair deficiency (HRD) status, but biallelic drivers in HR genes: ${
                        concat(
                            hrdGenesWithBiallelicDriver
                        )
                    } are detected; an HRD test may be recommended",
                    "Unknown HRD status but biallelic drivers in HR genes"
                )
            }

            isHRD == null && hrdGenesWithNonBiallelicDriver.isNotEmpty() && genesToFindWithMutation.isEmpty() -> {
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

            genesToFindWithMutation.isNotEmpty() -> {
                EvaluationFactory.fail(
                    "Homologous repair deficiency (HRD) detected with variant in ${concat(genesToFindWithMutation)}",
                    "Tumor is HRD with variant in ${concat(genesToFindWithMutation)}"
                )
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

}