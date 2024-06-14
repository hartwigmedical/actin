package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood

class IsHomologousRepairDeficientWithoutMutationOrWithVUSMutationInBRCA : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val hrdGenesWithNonBiallelicHotspot: MutableSet<String> = mutableSetOf()
        val hrdGenesWithBiallelicHotspot: MutableSet<String> = mutableSetOf()

        val hrdGenesWithNonBiallelicNonHotspotHighDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithNonBiallelicNonHotspotLowDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithBiallelicNonHotspotHighDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithBiallelicNonHotspotLowDriver: MutableSet<String> = mutableSetOf()

        val hrdGenesWithDeletionOrPartialLoss: MutableSet<String> = mutableSetOf()
        val hrdGenesWithHomozygousDisruption: MutableSet<String> = mutableSetOf()
        val hrdGenesWithNonHomozygousDisruption: MutableSet<String> = mutableSetOf()

        for (gene in MolecularConstants.HRD_GENES) {
            for (variant in molecular.drivers.variants) {
                if (variant.gene == gene && variant.isReportable) {
                    when {
                        variant.isHotspot && variant.isBiallelic -> {
                            hrdGenesWithBiallelicHotspot.add(gene)
                        }

                        variant.isHotspot -> {
                            hrdGenesWithNonBiallelicHotspot.add(gene)
                        }

                        variant.isBiallelic && variant.driverLikelihood == DriverLikelihood.HIGH -> {
                            hrdGenesWithBiallelicNonHotspotHighDriver.add(gene)
                        }

                        variant.isBiallelic -> {
                            hrdGenesWithBiallelicNonHotspotLowDriver.add(gene)
                        }

                        variant.driverLikelihood == DriverLikelihood.HIGH -> {
                            hrdGenesWithNonBiallelicNonHotspotHighDriver.add(gene)
                        }

                        else -> {
                            hrdGenesWithNonBiallelicNonHotspotLowDriver.add(gene)
                        }
                    }
                }
            }
            for (copyNumber in molecular.drivers.copyNumbers) {
                if (copyNumber.type == CopyNumberType.LOSS && copyNumber.gene == gene) {
                    hrdGenesWithDeletionOrPartialLoss.add(gene)
                }
            }
            for (homozygousDisruption in molecular.drivers.homozygousDisruptions) {
                if (homozygousDisruption.gene == gene) {
                    hrdGenesWithHomozygousDisruption.add(gene)
                }
            }
            for (disruption in molecular.drivers.disruptions) {
                if (disruption.gene == gene && disruption.isReportable) {
                    hrdGenesWithNonHomozygousDisruption.add(gene)
                }
            }
        }

        val hrdGenesWithBiallelicDriver = hrdGenesWithBiallelicHotspot + hrdGenesWithBiallelicNonHotspotHighDriver + hrdGenesWithBiallelicNonHotspotLowDriver + hrdGenesWithHomozygousDisruption + hrdGenesWithDeletionOrPartialLoss
        val hrdGenesWithNonBiallelicDriver = hrdGenesWithNonBiallelicNonHotspotHighDriver + hrdGenesWithNonBiallelicHotspot + hrdGenesWithNonHomozygousDisruption + hrdGenesWithNonBiallelicNonHotspotLowDriver

        return when (molecular.characteristics.isHomologousRepairDeficient) {
            null -> {
                when {
                    hrdGenesWithBiallelicDriver.isNotEmpty() -> {
                        EvaluationFactory.undetermined(
                            "Unknown homologous repair deficiency (HRD) status, but biallelic drivers in HR genes: ${concat(hrdGenesWithBiallelicDriver)} are detected; an HRD test may be recommended",
                            "Unknown HRD status but biallelic drivers in HR genes"
                        )
                    }

                    hrdGenesWithNonBiallelicDriver.isNotEmpty() -> {
                        EvaluationFactory.undetermined(
                            "Unknown homologous repair deficiency (HRD) status, but non-biallelic drivers in HR genes: ${concat(hrdGenesWithNonBiallelicDriver)} are detected; an HRD test may be recommended",
                            "Unknown HRD status but non-biallelic drivers in HR genes"
                        )
                    }

                    else -> {
                        EvaluationFactory.fail("Unknown homologous repair deficiency (HRD) status", "Unknown HRD status")
                    }
                }
            }

            true -> {
                when {
                    containsBRCA(hrdGenesWithBiallelicHotspot) || containsBRCA(hrdGenesWithNonBiallelicHotspot) -> {
                        EvaluationFactory.fail(
                            "Homologous repair deficiency (HRD) detected with BRCA hotspot",
                            "Tumor is HRD with BRCA hotspot"
                        )
                    }

                    containsBRCA(hrdGenesWithDeletionOrPartialLoss) -> {
                        EvaluationFactory.fail(
                            "Homologous repair deficiency (HRD) detected with deletion or partial loss in BRCA",
                            "Tumor is HRD with BRCA deletion or partial loss"
                        )
                    }

                    containsBRCA(hrdGenesWithBiallelicNonHotspotHighDriver) -> {
                        EvaluationFactory.warn(
                            "Homologous repair deficiency (HRD) detected, together with non-hotspot biallelic high driver(s) in BRCA which could potentially be pathogenic",
                            "Tumor is HRD with non-hotspot biallelic high driver(s) in BRCA which could be pathogenic",
                        )
                    }

                    containsBRCA(hrdGenesWithBiallelicNonHotspotLowDriver) -> {
                        EvaluationFactory.warn(
                            "Homologous repair deficiency (HRD) detected, together with non-hotspot biallelic low driver(s) in BRCA which could potentially be pathogenic",
                            "Tumor is HRD with non-hotspot biallelic low driver(s) in BRCA which could be pathogenic",
                        )
                    }

                    containsBRCA(hrdGenesWithNonBiallelicNonHotspotHighDriver) -> {
                        EvaluationFactory.warn(
                            "Homologous repair deficiency (HRD) detected, together with non-hotspot non-biallelic high driver(s) in BRCA which could potentially be pathogenic",
                            "Tumor is HRD with non-hotspot non-biallelic high driver(s) in BRCA which could be pathogenic",
                        )
                    }

                    containsBRCA(hrdGenesWithNonBiallelicNonHotspotLowDriver) -> {
                        EvaluationFactory.warn(
                            "Homologous repair deficiency (HRD) detected, together with non-hotspot non-biallelic low driver(s) in BRCA which could potentially be pathogenic",
                            "Tumor is HRD with non-hotspot non-biallelic low driver(s) in BRCA which could be pathogenic",
                        )
                    }

                    containsBRCA(hrdGenesWithHomozygousDisruption) -> {
                        EvaluationFactory.pass(
                            "Homologous repair deficiency (HRD) detected, with homozygous disruption in BRCA",
                            "Tumor is HRD with homozygous disruption in BRCA",
                        )
                    }

                    hrdGenesWithNonBiallelicDriver.isNotEmpty() && hrdGenesWithBiallelicDriver.isEmpty() -> {
                        EvaluationFactory.warn(
                            "Homologous repair deficiency (HRD) status detected, together with only non-biallelic drivers in HR genes (${concat(hrdGenesWithNonBiallelicDriver)})",
                            "Tumor is HRD (but with only non-biallelic drivers in HR genes)",
                        )
                    }

                    containsBRCA(hrdGenesWithNonHomozygousDisruption) -> {
                        EvaluationFactory.pass(
                            "Homologous repair deficiency (HRD) detected, with non-homozygous disruption in BRCA",
                            "Tumor is HRD with non-homozygous disruption in BRCA",
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
                            "Homologous repair deficiency (HRD) detected, without BRCA variants",
                            "Tumor is HRD without any BRCA variants",
                        )
                    }
                }
            }

            else -> {
                EvaluationFactory.fail("No homologous repair deficiency (HRD) detected", "Tumor is not HRD")
            }
        }
    }

    private fun containsBRCA(genes: MutableSet<String>): Boolean {
        return genes.any { MolecularConstants.BRCA_GENES.contains(it) }
    }

}