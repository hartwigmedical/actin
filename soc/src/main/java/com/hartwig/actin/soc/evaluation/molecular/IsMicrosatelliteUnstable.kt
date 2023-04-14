package com.hartwig.actin.soc.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import com.hartwig.actin.soc.evaluation.EvaluationFactory
import com.hartwig.actin.soc.evaluation.EvaluationFunction
import com.hartwig.actin.soc.evaluation.util.Format

class IsMicrosatelliteUnstable internal constructor() : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val msiGenesWithBiallelicDriver: MutableSet<String> = mutableSetOf()
        val msiGenesWithNonBiallelicDriver: MutableSet<String> = mutableSetOf()
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
            return if (msiGenesWithBiallelicDriver.isNotEmpty()) {
                EvaluationFactory.undetermined("Unknown microsatellite instability (MSI) status, but biallelic drivers in MSI genes: "
                        + Format.concat(msiGenesWithBiallelicDriver) + " are detected; an MSI test may be recommended",
                        "Unknown MSI status")
            } else if (msiGenesWithNonBiallelicDriver.isNotEmpty()) {
                EvaluationFactory.undetermined("Unknown microsatellite instability (MSI) status, but non-biallelic drivers in MSI genes: "
                        + Format.concat(msiGenesWithNonBiallelicDriver) + " are detected; an MSI test may be recommended",
                        "Unknown MSI status")
            } else {
                EvaluationFactory.fail("Unknown microsatellite instability (MSI) status", "Unknown MSI status")
            }
        } else if (isMicrosatelliteUnstable) {
            return if (msiGenesWithBiallelicDriver.isNotEmpty()) {
                EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
                        .addPassSpecificMessages("Microsatellite instability (MSI) status detected, together with biallelic drivers in MSI genes: "
                                + Format.concat(msiGenesWithBiallelicDriver))
                        .addPassGeneralMessages("MSI")
                        .build()
            } else if (msiGenesWithNonBiallelicDriver.isNotEmpty()) {
                EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.MICROSATELLITE_POTENTIALLY_UNSTABLE)
                        .addWarnSpecificMessages(
                                ("Microsatellite instability (MSI) detected, together with non-biallelic drivers in MSI genes ("
                                        + Format.concat(MolecularConstants.MSI_GENES)
                                        ) + ") were detected")
                        .addWarnGeneralMessages("MSI")
                        .build()
            } else {
                EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.MICROSATELLITE_POTENTIALLY_UNSTABLE)
                        .addWarnSpecificMessages(("Microsatellite instability (MSI) detected, without drivers in MSI genes ("
                                + Format.concat(MolecularConstants.MSI_GENES)) + ") detected")
                        .addWarnGeneralMessages("MSI")
                        .build()
            }
        }
        return EvaluationFactory.fail("No microsatellite instability (MSI) status detected", "MSI")
    }
}