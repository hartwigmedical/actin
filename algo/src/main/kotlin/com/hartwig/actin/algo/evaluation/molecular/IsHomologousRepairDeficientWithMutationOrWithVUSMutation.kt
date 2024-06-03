package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood

class IsHomologousRepairDeficientWithMutationOrWithVUSMutation : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val brcaNonHotspotBiallelicDriver: MutableSet<String> = mutableSetOf()
        val brcaNonHotspotNonBiallelicDriver: MutableSet<String> = mutableSetOf()
        val brcaHotspot: MutableSet<String> = mutableSetOf()
        val brcaLowDriver: MutableSet<String> = mutableSetOf()
        val brcaDeletionOrHomozygousDisruption: MutableSet<String> = mutableSetOf()
        val brcaNonHomozygousDisruption: MutableSet<String> = mutableSetOf()
        for (gene in MolecularConstants.HRD_GENES) {
            for (variant in molecular.drivers.variants) {
                if (variant.gene == gene && variant.isReportable) {
                    if (variant.isHotspot) {
                        brcaHotspot.add(gene)
                    } else if (variant.isBiallelic && variant.driverLikelihood == DriverLikelihood.HIGH) {
                        brcaNonHotspotBiallelicDriver.add(gene)
                    } else if (!variant.isBiallelic && variant.driverLikelihood == DriverLikelihood.HIGH) {
                        brcaNonHotspotNonBiallelicDriver.add(gene)
                    } else { brcaLowDriver.add(gene)}
                }
            }
            for (copyNumber in molecular.drivers.copyNumbers) {
                if (copyNumber.type == CopyNumberType.LOSS && copyNumber.gene == gene && copyNumber.driverLikelihood == DriverLikelihood.HIGH) {
                    brcaDeletionOrHomozygousDisruption.add(gene)
                }
            }
            for (homozygousDisruption in molecular.drivers.homozygousDisruptions) {
                if (homozygousDisruption.gene == gene && homozygousDisruption.driverLikelihood == DriverLikelihood.HIGH) {
                    brcaDeletionOrHomozygousDisruption.add(gene)
                }
            }
            for (disruption in molecular.drivers.disruptions) {
                if (disruption.gene == gene && disruption.isReportable && disruption.driverLikelihood == DriverLikelihood.HIGH) {
                    brcaNonHomozygousDisruption.add(gene)
                }
            }
        }
        return when (molecular.characteristics.isHomologousRepairDeficient) {
            null -> {
                if (brcaHotspot.isNotEmpty() || brcaDeletionOrHomozygousDisruption.isNotEmpty() || brcaNonHotspotBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but biallelic drivers in HR genes: "
                                + Format.concat(brcaHotspot intersect brcaDeletionOrHomozygousDisruption intersect brcaNonHotspotBiallelicDriver) + " are detected; an HRD test may be recommended",
                        "Unknown HRD status but biallelic drivers in HR genes"
                    )
                } else if (brcaNonHotspotNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but non-biallelic drivers in HR genes: "
                                + Format.concat(brcaNonHotspotNonBiallelicDriver) + " are detected; an HRD test may be recommended",
                        "Unknown HRD status but non-biallelic drivers in HR genes"
                    )
                } else {
                    EvaluationFactory.fail("Unknown homologous repair deficiency (HRD) status", "Unknown HRD status")
                }
            }

            true -> {
                if (brcaHotspot.any { MolecularConstants.BRCA_GENES.contains(it) }) {
                    EvaluationFactory.fail("Homologous repair deficiency (HRD) detected, with BRCA hotspot", "Tumor is HRD with BRCA hotspot")
                }
                else if (brcaDeletionOrHomozygousDisruption.any { MolecularConstants.BRCA_GENES.contains(it)}) {
                    EvaluationFactory.fail("Homologous repair deficiency (HRD) detected, with deletion or homozygous disruption in BRCA", "Tumor is HRD with BRCA deletion or homozygous disruption")
                }
                else if (brcaNonHotspotBiallelicDriver.any { MolecularConstants.BRCA_GENES.contains(it)}) {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, together with non-hotspot biallelic driver(s) in BRCA were detected",
                        "Tumor is HRD with non-hotspot biallelic high driver(s) in BRCA",
                    )
                } else if (brcaNonHotspotNonBiallelicDriver.any { MolecularConstants.BRCA_GENES.contains(it)}) {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, together with non-hotspot non-biallelic driver(s) in BRCA were detected",
                        "Tumor is HRD with non-hotspot non-biallelic high drivers in BRCA)",
                    )
                } else if (brcaLowDriver.any { MolecularConstants.BRCA_GENES.contains(it)}) {
                    EvaluationFactory.pass(
                        "Homologous repair deficiency (HRD) detected, together with low driver in BRCA",
                        "Tumor is HRD with low driver in BRCA",
                    )
                } else if (brcaNonHomozygousDisruption.any { MolecularConstants.BRCA_GENES.contains(it)}){
                        EvaluationFactory.pass(
                            "Homologous repair deficiency (HRD) detected, with non-homozygous disruption in BRCA",
                            "Tumor is HRD with non-homozygous disruption in BRCA",
                        )
                } else {
                    EvaluationFactory.pass(
                        "Homologous repair deficiency (HRD) detected, without BRCA variants",
                        "Tumor is HRD without any BRCA variants",
                    )
                }
            }

            else -> {
                EvaluationFactory.fail("No homologous repair deficiency (HRD) detected", "Tumor is not HRD")
            }
        }
    }
}