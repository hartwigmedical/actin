package com.hartwig.actin.soc.evaluation.molecular

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation

class IsMicrosatelliteUnstable internal constructor() : EvaluationFunction {
    fun evaluate(record: PatientRecord): Evaluation {
        val msiGenesWithBiallelicDriver: MutableSet<String> = Sets.newHashSet()
        val msiGenesWithNonBiallelicDriver: MutableSet<String> = Sets.newHashSet()
        for (gene in MolecularConstants.MSI_GENES) {
            for (variant in record.molecular().drivers().variants()) {
                if (variant.gene() == gene && variant.isReportable) {
                    if (variant.isBiallelic) {
                        msiGenesWithBiallelicDriver.add(gene)
                    } else {
                        msiGenesWithNonBiallelicDriver.add(gene)
                    }
                }
            }
            for (copyNumber in record.molecular().drivers().copyNumbers()) {
                if (copyNumber.type() == CopyNumberType.LOSS && copyNumber.gene() == gene) {
                    msiGenesWithBiallelicDriver.add(gene)
                }
            }
            for (homozygousDisruption in record.molecular().drivers().homozygousDisruptions()) {
                if (homozygousDisruption.gene() == gene) {
                    msiGenesWithBiallelicDriver.add(gene)
                }
            }
            for (disruption in record.molecular().drivers().disruptions()) {
                if (disruption.gene() == gene && disruption.isReportable) {
                    msiGenesWithNonBiallelicDriver.add(gene)
                }
            }
        }
        val isMicrosatelliteUnstable = record.molecular().characteristics().isMicrosatelliteUnstable
        if (isMicrosatelliteUnstable == null) {
            return if (!msiGenesWithBiallelicDriver.isEmpty()) {
                EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(
                                "Unknown microsatellite instability (MSI) status, but biallelic drivers in MSI genes: " + Format.concat(
                                        msiGenesWithBiallelicDriver) + " are detected; an MSI test may be recommended")
                        .addUndeterminedGeneralMessages("Unknown MSI status")
                        .build()
            } else if (!msiGenesWithNonBiallelicDriver.isEmpty()) {
                EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(
                                "Unknown microsatellite instability (MSI) status, but non-biallelic drivers in MSI genes: " + Format.concat(
                                        msiGenesWithNonBiallelicDriver) + " are detected; an MSI test may be recommended")
                        .addUndeterminedGeneralMessages("Unknown MSI status")
                        .build()
            } else {
                EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.FAIL)
                        .addFailSpecificMessages("Unknown microsatellite instability (MSI) status")
                        .addFailGeneralMessages("Unknown MSI status")
                        .build()
            }
        } else if (isMicrosatelliteUnstable) {
            return if (!msiGenesWithBiallelicDriver.isEmpty()) {
                EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
                        .addPassSpecificMessages("Microsatellite instability (MSI) status detected, together with biallelic drivers in MSI genes: "
                                + Format.concat(msiGenesWithBiallelicDriver))
                        .addPassGeneralMessages("MSI")
                        .build()
            } else if (!msiGenesWithNonBiallelicDriver.isEmpty()) {
                EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.MICROSATELLITE_POTENTIALLY_UNSTABLE)
                        .addWarnSpecificMessages(
                                ("Microsatellite instability (MSI) detected, together with non-biallelic drivers in MSI genes ("
                                        + Format.concat(com.hartwig.actin.algo.evaluation.molecular.MolecularConstants.MSI_GENES)
                                        ) + ") were detected")
                        .addWarnGeneralMessages("MSI")
                        .build()
            } else {
                EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.MICROSATELLITE_POTENTIALLY_UNSTABLE)
                        .addWarnSpecificMessages(("Microsatellite instability (MSI) detected, without drivers in MSI genes ("
                                + Format.concat(com.hartwig.actin.algo.evaluation.molecular.MolecularConstants.MSI_GENES)) + ") detected")
                        .addWarnGeneralMessages("MSI")
                        .build()
            }
        }
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No microsatellite instability (MSI) status detected")
                .addFailGeneralMessages("MSI")
                .build()
    }
}