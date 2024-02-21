package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents

class IsHomologousRepairDeficient : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val hrdGenesWithBiallelicDriver: MutableSet<String> = mutableSetOf()
        val hrdGenesWithNonBiallelicDriver: MutableSet<String> = mutableSetOf()
        for (gene in MolecularConstants.HRD_GENES) {
            for (variant in molecular.drivers.variants) {
                if (variant.gene == gene && variant.isReportable) {
                    if (variant.isBiallelic) {
                        hrdGenesWithBiallelicDriver.add(gene)
                    } else {
                        hrdGenesWithNonBiallelicDriver.add(gene)
                    }
                }
            }
            for (copyNumber in molecular.drivers.copyNumbers) {
                if (copyNumber.type == CopyNumberType.LOSS && copyNumber.gene == gene) {
                    hrdGenesWithBiallelicDriver.add(gene)
                }
            }
            for (homozygousDisruption in molecular.drivers.homozygousDisruptions) {
                if (homozygousDisruption.gene == gene) {
                    hrdGenesWithBiallelicDriver.add(gene)
                }
            }
            for (disruption in molecular.drivers.disruptions) {
                if (disruption.gene == gene && disruption.isReportable) {
                    hrdGenesWithNonBiallelicDriver.add(gene)
                }
            }
        }
        return when (molecular.characteristics.isHomologousRepairDeficient) {
            null -> {
                if (hrdGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but biallelic drivers in HR genes: "
                                + concat(hrdGenesWithBiallelicDriver) + " are detected; an HRD test may be recommended",
                        "Unknown HRD status but biallelic drivers in HR genes"
                    )
                } else if (hrdGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.undetermined(
                        "Unknown homologous repair deficiency (HRD) status, but non-biallelic drivers in HR genes: "
                                + concat(hrdGenesWithBiallelicDriver) + " are detected; an HRD test may be recommended",
                        "Unknown HRD status but non-biallelic drivers in HR genes"
                    )
                } else {
                    EvaluationFactory.fail("Unknown homologous repair deficiency (HRD) status", "Unknown HRD status")
                }
            }

            true -> {
                val inclusionMolecularEvents = setOf(MolecularCharacteristicEvents.HOMOLOGOUS_REPAIR_DEFICIENT)
                if (hrdGenesWithBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.pass(
                        "Homologous repair deficiency (HRD) status detected, together with biallelic drivers in HR genes: "
                                + concat(hrdGenesWithBiallelicDriver),
                        "Tumor is HRD and biallelic drivers in HR genes detected",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else if (hrdGenesWithNonBiallelicDriver.isNotEmpty()) {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) status detected, together with non-biallelic drivers in HR genes ("
                                + concat(MolecularConstants.HRD_GENES) + ") were detected",
                        "Tumor is HRD (but with only non-biallelic drivers in HR genes)",
                        inclusionEvents = inclusionMolecularEvents
                    )
                } else {
                    EvaluationFactory.warn(
                        "Homologous repair deficiency (HRD) status detected, without drivers in HR genes ("
                                + concat(MolecularConstants.HRD_GENES) + ") detected",
                        "Tumor is HRD (but without detected drivers in HR genes)",
                        inclusionEvents = inclusionMolecularEvents
                    )
                }
            }

            else -> {
                EvaluationFactory.fail("No homologous repair deficiency (HRD) detected", "Tumor is not HRD")
            }
        }
    }
}