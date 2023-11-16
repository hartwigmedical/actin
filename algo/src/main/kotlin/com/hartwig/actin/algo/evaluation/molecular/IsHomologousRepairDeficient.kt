package com.hartwig.actin.algo.evaluation.molecular

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents

class IsHomologousRepairDeficient internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hrdGenesWithBiallelicDriver: MutableSet<String> = Sets.newHashSet()
        val hrdGenesWithNonBiallelicDriver: MutableSet<String> = Sets.newHashSet()
        for (gene in MolecularConstants.HRD_GENES) {
            for (variant in record.molecular().drivers().variants()) {
                if (variant.gene() == gene && variant.isReportable) {
                    if (variant.isBiallelic) {
                        hrdGenesWithBiallelicDriver.add(gene)
                    } else {
                        hrdGenesWithNonBiallelicDriver.add(gene)
                    }
                }
            }
            for (copyNumber in record.molecular().drivers().copyNumbers()) {
                if (copyNumber.type() == CopyNumberType.LOSS && copyNumber.gene() == gene) {
                    hrdGenesWithBiallelicDriver.add(gene)
                }
            }
            for (homozygousDisruption in record.molecular().drivers().homozygousDisruptions()) {
                if (homozygousDisruption.gene() == gene) {
                    hrdGenesWithBiallelicDriver.add(gene)
                }
            }
            for (disruption in record.molecular().drivers().disruptions()) {
                if (disruption.gene() == gene && disruption.isReportable) {
                    hrdGenesWithNonBiallelicDriver.add(gene)
                }
            }
        }
        val isHomologousRepairDeficient = record.molecular().characteristics().isHomologousRepairDeficient
        if (isHomologousRepairDeficient == null) {
            return if (hrdGenesWithBiallelicDriver.isNotEmpty()) {
                unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                        "Unknown homologous repair deficiency (HRD) status, but biallelic drivers in HR genes: " + concat(
                            hrdGenesWithBiallelicDriver
                        ) + " are detected; an HRD test may be recommended"
                    )
                    .addUndeterminedGeneralMessages("Unknown HRD status but biallelic drivers in HR genes")
                    .build()
            } else if (hrdGenesWithNonBiallelicDriver.isNotEmpty()) {
                unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                        "Unknown homologous repair deficiency (HRD) status, but non-biallelic drivers in HR genes: "
                                + concat(hrdGenesWithBiallelicDriver) + " are detected; an HRD test may be recommended"
                    )
                    .addUndeterminedGeneralMessages("Unknown HRD status but non-biallelic drivers in HR genes")
                    .build()
            } else {
                unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Unknown homologous repair deficiency (HRD) status")
                    .addFailGeneralMessages("Unknown HRD status")
                    .build()
            }
        } else if (isHomologousRepairDeficient) {
            return if (hrdGenesWithBiallelicDriver.isNotEmpty()) {
                unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addInclusionMolecularEvents(MolecularCharacteristicEvents.HOMOLOGOUS_REPAIR_DEFICIENT)
                    .addPassSpecificMessages(
                        "Homologous repair deficiency (HRD) status detected, together with biallelic drivers in HR genes: "
                                + concat(hrdGenesWithBiallelicDriver)
                    )
                    .addPassGeneralMessages("Tumor is HRD and biallelic drivers in HR genes detected")
                    .build()
            } else if (hrdGenesWithNonBiallelicDriver.isNotEmpty()) {
                unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addInclusionMolecularEvents(MolecularCharacteristicEvents.HOMOLOGOUS_REPAIR_DEFICIENT)
                    .addWarnSpecificMessages(
                        "Homologous repair deficiency (HRD) status detected, together with non-biallelic drivers in HR genes ("
                                + concat(MolecularConstants.HRD_GENES) + ") were detected"
                    )
                    .addWarnGeneralMessages("Tumor is HRD (but with only non-biallelic drivers in HR genes)")
                    .build()
            } else {
                unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addInclusionMolecularEvents(MolecularCharacteristicEvents.HOMOLOGOUS_REPAIR_DEFICIENT)
                    .addWarnSpecificMessages(
                        "Homologous repair deficiency (HRD) status detected, without drivers in HR genes (" + concat(
                            MolecularConstants.HRD_GENES
                        ) + ") detected"
                    )
                    .addWarnGeneralMessages("Tumor is HRD (but without detected drivers in HR genes)")
                    .build()
            }
        }
        return unrecoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("No homologous repair deficiency (HRD) detected")
            .addFailGeneralMessages("Tumor is not HRD")
            .build()
    }
}