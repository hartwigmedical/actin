package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneAlteration

class IsHomologousRepairDeficientWithoutMutationOrWithVUSMutationInBRCA : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val hrdGenesWithNonBiallelicHotspot: MutableSet<String> = mutableSetOf()
        val hrdGenesWithBiallelicHotspot: MutableSet<String> = mutableSetOf()
        val hrdGenesWithNonBiallelicNonHotspotHighDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithNonBiallelicNonHotspotNonHighDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithBiallelicNonHotspotHighDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithBiallelicNonHotspotNonHighDriver: MutableSet<String> = mutableSetOf()

        molecular.drivers.variants.filter { it.gene in MolecularConstants.HRD_GENES && it.isReportable }.forEach { variant ->
            when {
                variant.isHotspot && variant.isBiallelic -> {
                    hrdGenesWithBiallelicHotspot.add(variant.gene)
                }
                variant.isHotspot -> {
                    hrdGenesWithNonBiallelicHotspot.add(variant.gene)
                }
                variant.isBiallelic && variant.driverLikelihood == DriverLikelihood.HIGH -> {
                    hrdGenesWithBiallelicNonHotspotHighDriver.add(variant.gene)
                }
                variant.isBiallelic -> {
                    hrdGenesWithBiallelicNonHotspotNonHighDriver.add(variant.gene)
                }
                variant.driverLikelihood == DriverLikelihood.HIGH -> {
                    hrdGenesWithNonBiallelicNonHotspotHighDriver.add(variant.gene)
                }
                else -> {
                    hrdGenesWithNonBiallelicNonHotspotNonHighDriver.add(variant.gene)
                }
            }
        }

        val hrdGenesWithDeletionOrPartialLoss = molecular.drivers.copyNumbers
            .filter { it.type == CopyNumberType.LOSS && it.gene in MolecularConstants.HRD_GENES }
            .map(GeneAlteration::gene)
            .toSet()
        val hrdGenesWithHomozygousDisruption = molecular.drivers.homozygousDisruptions.filter { it.gene in MolecularConstants.HRD_GENES }
            .map(GeneAlteration::gene)
            .toSet()
        val hrdGenesWithNonHomozygousDisruption = molecular.drivers.disruptions
            .filter { it.gene in MolecularConstants.HRD_GENES && it.isReportable }
            .map(GeneAlteration::gene)
            .toSet()

        val hrdGenesWithBiallelicDriver = hrdGenesWithBiallelicHotspot + hrdGenesWithBiallelicNonHotspotHighDriver + hrdGenesWithBiallelicNonHotspotNonHighDriver + hrdGenesWithHomozygousDisruption + hrdGenesWithDeletionOrPartialLoss
        val hrdGenesWithNonBiallelicDriver = hrdGenesWithNonBiallelicNonHotspotHighDriver + hrdGenesWithNonBiallelicHotspot + hrdGenesWithNonHomozygousDisruption + hrdGenesWithNonBiallelicNonHotspotNonHighDriver
        val isHRD = molecular.characteristics.isHomologousRepairDeficient

        return when {
                isHRD == null && hrdGenesWithBiallelicDriver.isNotEmpty() -> {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but biallelic drivers in HR genes: ${concat(hrdGenesWithBiallelicDriver)} are detected; an HRD test may be recommended",
                        "Unknown HRD status but biallelic drivers in HR genes"
                    )
                }

                isHRD == null && hrdGenesWithNonBiallelicDriver.isNotEmpty() -> {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but non-biallelic drivers in HR genes: ${concat(hrdGenesWithNonBiallelicDriver)} are detected; an HRD test may be recommended",
                        "Unknown HRD status but non-biallelic drivers in HR genes"
                    )
                }

                isHRD == null -> {
                    EvaluationFactory.fail("Unknown homologous repair deficiency (HRD) status", "Unknown HRD status")
                }

                isHRD == false -> {
                    EvaluationFactory.fail("No homologous repair deficiency (HRD) detected", "Tumor is not HRD")
                }

                containsBRCA(hrdGenesWithBiallelicHotspot) || containsBRCA(hrdGenesWithNonBiallelicHotspot) -> {
                    EvaluationFactory.fail(
                        "Homologous repair deficiency (HRD) detected with BRCA1/2 hotspot",
                        "Tumor is HRD with BRCA1/2 hotspot"
                    )
                }

                containsBRCA(hrdGenesWithDeletionOrPartialLoss) -> {
                    EvaluationFactory.fail(
                        "Homologous repair deficiency (HRD) detected with deletion or partial loss in BRCA1/2",
                        "Tumor is HRD with BRCA1/2 deletion or partial loss"
                    )
                }

                containsBRCA(hrdGenesWithBiallelicNonHotspotHighDriver) -> {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, together with non-hotspot biallelic high driver(s) in BRCA1/2 which could potentially be pathogenic",
                        "Tumor is HRD with non-hotspot biallelic high driver(s) in BRCA1/2 which could be pathogenic",
                    )
                }

                containsBRCA(hrdGenesWithBiallelicNonHotspotNonHighDriver) -> {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, together with non-hotspot biallelic non-high driver(s) in BRCA1/2 which could potentially be pathogenic",
                        "Tumor is HRD with non-hotspot biallelic non-high driver(s) in BRCA1/2 which could be pathogenic",
                    )
                }

                containsBRCA(hrdGenesWithNonBiallelicNonHotspotHighDriver) -> {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, together with non-hotspot non-biallelic high driver(s) in BRCA1/2 which could potentially be pathogenic",
                        "Tumor is HRD with non-hotspot non-biallelic high driver(s) in BRCA1/2 which could be pathogenic",
                    )
                }

                containsBRCA(hrdGenesWithNonBiallelicNonHotspotNonHighDriver) -> {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, together with non-hotspot non-biallelic non-high driver(s) in BRCA1/2 which could potentially be pathogenic",
                        "Tumor is HRD with non-hotspot non-biallelic non-high driver(s) in BRCA1/2 which could be pathogenic",
                    )
                }

                containsBRCA(hrdGenesWithHomozygousDisruption) -> {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, with homozygous disruption in BRCA1/2",
                        "Tumor is HRD with homozygous disruption in BRCA1/2",
                    )
                }

                hrdGenesWithNonBiallelicDriver.isNotEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) status detected, together with only non-biallelic drivers in HR genes (${concat(hrdGenesWithNonBiallelicDriver)})",
                        "Tumor is HRD (but with only non-biallelic drivers in HR genes)",
                    )
                }

                containsBRCA(hrdGenesWithNonHomozygousDisruption) -> {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, with non-homozygous disruption in BRCA1/2",
                        "Tumor is HRD with non-homozygous disruption in BRCA1/2",
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
                        "Homologous repair deficiency (HRD) detected, without BRCA1/2 variants",
                        "Tumor is HRD without any BRCA1/2 variants",
                    )
                }
            }
        }

    private fun containsBRCA(genes: Iterable<String>): Boolean {
        return genes.any { MolecularConstants.BRCA_GENES.contains(it) }
    }

}