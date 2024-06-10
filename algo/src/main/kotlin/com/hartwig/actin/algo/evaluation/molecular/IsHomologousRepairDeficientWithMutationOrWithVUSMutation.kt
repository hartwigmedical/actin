package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood

class IsHomologousRepairDeficientWithMutationOrWithVUSMutation : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val brcaNonBiallelicHotspot: MutableSet<String> = mutableSetOf()
        val brcaLowDriver: MutableSet<String> = mutableSetOf()
        val brcaBiallelicNonHotspot: MutableSet<String> = mutableSetOf()
        val brcaNonBiallelicNonHotspot: MutableSet<String> = mutableSetOf()
        val brcaBiallelicHotspot: MutableSet<String> = mutableSetOf()
        val brcaDeletionOrPartialLoss: MutableSet<String> = mutableSetOf()
        val brcaHomozygousDisruption: MutableSet<String> = mutableSetOf()
        val brcaNonHomozygousDisruption: MutableSet<String> = mutableSetOf()
        for (gene in MolecularConstants.HRD_GENES) {
            for (variant in molecular.drivers.variants) {
                if (variant.gene == gene && variant.isReportable) {
                    if (variant.isHotspot && variant.isBiallelic) {
                        brcaBiallelicHotspot.add(gene)
                    } else if (variant.isHotspot) {
                        brcaNonBiallelicHotspot.add(gene)
                    } else if (variant.isBiallelic && variant.driverLikelihood == DriverLikelihood.HIGH) {
                        brcaBiallelicNonHotspot.add(gene)
                    } else if (!variant.isBiallelic && variant.driverLikelihood == DriverLikelihood.HIGH) {
                        brcaNonBiallelicNonHotspot.add(gene)
                    } else {
                        brcaLowDriver.add(gene)
                    }
                }
            }
            for (copyNumber in molecular.drivers.copyNumbers) {
                if (copyNumber.type == CopyNumberType.LOSS && copyNumber.gene == gene) {
                    brcaDeletionOrPartialLoss.add(gene)
                }
            }
            for (homozygousDisruption in molecular.drivers.homozygousDisruptions) {
                if (homozygousDisruption.gene == gene) {
                    brcaHomozygousDisruption.add(gene)
                }
            }
            for (disruption in molecular.drivers.disruptions) {
                if (disruption.gene == gene && disruption.isReportable) {
                    brcaNonHomozygousDisruption.add(gene)
                }
            }
        }
        return when (molecular.characteristics.isHomologousRepairDeficient) {
            null -> {
                if (brcaBiallelicHotspot.isNotEmpty() || brcaBiallelicNonHotspot.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but biallelic drivers in HR genes: "
                                + concat(brcaBiallelicHotspot intersect brcaBiallelicNonHotspot) + " are detected; an HRD test may be recommended",
                        "Unknown HRD status but biallelic drivers in HR genes"
                    )
                } else if (brcaNonBiallelicNonHotspot.isNotEmpty() || brcaNonBiallelicHotspot.isNotEmpty() || brcaLowDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but non-biallelic drivers in HR genes: "
                                + concat(brcaNonBiallelicNonHotspot intersect brcaNonBiallelicHotspot intersect brcaLowDriver) + " are detected; an HRD test may be recommended",
                        "Unknown HRD status but non-biallelic drivers in HR genes"
                    )
                } else {
                    EvaluationFactory.fail("Unknown homologous repair deficiency (HRD) status", "Unknown HRD status")
                }
            }

            true -> {
                if (containsBRCA(brcaBiallelicHotspot) || containsBRCA(brcaNonBiallelicHotspot)) {
                    EvaluationFactory.fail(
                        "Homologous repair deficiency (HRD) detected with BRCA hotspot",
                        "Tumor is HRD with BRCA hotspot"
                    )
                } else if (containsBRCA(brcaDeletionOrPartialLoss)) {
                    EvaluationFactory.fail(
                        "Homologous repair deficiency (HRD) detected with deletion or partial loss in BRCA",
                        "Tumor is HRD with BRCA deletion or partial loss"
                    )
                } else if (containsBRCA(brcaBiallelicNonHotspot)) {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, together with non-hotspot biallelic high driver(s) in BRCA were detected",
                        "Tumor is HRD with non-hotspot biallelic high driver(s) in BRCA",
                    )
                } else if (containsBRCA(brcaNonBiallelicNonHotspot)) {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, together with non-hotspot non-biallelic high driver(s) in BRCA were detected",
                        "Tumor is HRD with non-hotspot non-biallelic high driver(s) in BRCA",
                    )
                } else if (containsBRCA(brcaLowDriver)) {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) detected, together with low or medium driver in BRCA",
                        "Tumor is HRD with low or medium driver in BRCA",
                    )
                } else if (containsBRCA(brcaNonHomozygousDisruption)) {
                    EvaluationFactory.pass(
                        "Homologous repair deficiency (HRD) detected, with non-homozygous disruption in BRCA",
                        "Tumor is HRD with non-homozygous disruption in BRCA",
                    )
                } else if (containsBRCA(brcaHomozygousDisruption)) {
                    EvaluationFactory.pass(
                        "Homologous repair deficiency (HRD) detected, with homozygous disruption in BRCA",
                        "Tumor is HRD with homozygous disruption in BRCA",
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

    private fun containsBRCA(genes: MutableSet<String>): Boolean {
        return genes.any { MolecularConstants.BRCA_GENES.contains(it) }
    }

}