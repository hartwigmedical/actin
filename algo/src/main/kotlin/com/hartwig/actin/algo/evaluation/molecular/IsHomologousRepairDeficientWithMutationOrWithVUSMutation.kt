package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood

class IsHomologousRepairDeficientWithMutationOrWithVUSMutation : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val brcaWithBiallelicDriver: MutableSet<String> = mutableSetOf()
        val brcaWithNonBiallelicDriver: MutableSet<String> = mutableSetOf()
        val brcaHotspot: MutableSet<String> = mutableSetOf()
        val brcaLowDriver: MutableSet<String> = mutableSetOf()
        for (gene in BRCA_GENES) {
            for (variant in molecular.drivers.variants) {
                if (variant.gene == gene && variant.isReportable) {
                    if (variant.isHotspot) {
                        brcaHotspot.add(gene)
                    } else if (variant.isBiallelic && variant.driverLikelihood == DriverLikelihood.HIGH) {
                        brcaWithBiallelicDriver.add(gene)
                    } else if (!variant.isBiallelic && variant.driverLikelihood == DriverLikelihood.HIGH) {
                        brcaWithNonBiallelicDriver.add(gene)
                    } else { brcaLowDriver.add(gene)}
                }
            }
            for (copyNumber in molecular.drivers.copyNumbers) {
                if (copyNumber.type == CopyNumberType.LOSS && copyNumber.gene == gene && copyNumber.driverLikelihood == DriverLikelihood.HIGH) {
                    brcaWithBiallelicDriver.add(gene)
                }
            }
            for (homozygousDisruption in molecular.drivers.homozygousDisruptions) {
                if (homozygousDisruption.gene == gene && homozygousDisruption.driverLikelihood == DriverLikelihood.HIGH) {
                    brcaWithBiallelicDriver.add(gene)
                }
            }
            for (disruption in molecular.drivers.disruptions) {
                if (disruption.gene == gene && disruption.isReportable && disruption.driverLikelihood == DriverLikelihood.HIGH) {
                    brcaWithNonBiallelicDriver.add(gene)
                }
            }
        }
        return when (molecular.characteristics.isHomologousRepairDeficient) {
            null -> {
                if (brcaWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but biallelic drivers in BRCA1 or 2 are detected; an HRD test may be recommended",
                        "Unknown HRD status but biallelic high drivers in BRCA1 or 2"
                    )
                } else if (brcaWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but non-biallelic drivers in BRCA are detected; an HRD test may be recommended",
                        "Unknown HRD status but non-biallelic high drivers in BRCA"
                    )
                } else {
                    EvaluationFactory.fail("Unknown homologous repair deficiency (HRD) status", "Unknown HRD status")
                }
            }

            true -> {
                if (brcaHotspot.isNotEmpty()) {
                    EvaluationFactory.fail("Homologous repair deficiency (HRD) detected, with BRCA hotspot", "Tumor is HRD with BRCA hotspot")
                }
                else if (brcaWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.pass(
                        "Homologous repair deficiency (HRD) detected, together with biallelic drivers in BRCA",
                        "Tumor is HRD and biallelic high drivers in BRCA1 or 2 detected",
                    )
                } else if (brcaWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, together with non-biallelic drivers in BRCA were detected",
                        "Tumor is HRD (but with only non-biallelic high drivers in BRCA)",
                    )
                } else {
                    EvaluationFactory.fail(
                        "Homologous repair deficiency (HRD) detected, without high drivers in BRCA detected",
                        "Tumor is HRD (but without detected high drivers in BRCA)",
                    )
                }
            }

            else -> {
                EvaluationFactory.fail("No homologous repair deficiency (HRD) detected", "Tumor is not HRD")
            }
        }
    }

    companion object {
        private val BRCA_GENES = setOf("BRCA1", "BRCA2")
    }
}