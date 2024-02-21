package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents

class IsMicrosatelliteUnstable internal constructor() : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val msiGenesWithBiallelicDriver: MutableSet<String> = mutableSetOf()
        val msiGenesWithNonBiallelicDriver: MutableSet<String> = mutableSetOf()
        for (gene in MolecularConstants.MSI_GENES) {
            for (variant in molecular.drivers.variants) {
                if (variant.gene == gene && variant.isReportable) {
                    if (variant.isBiallelic) {
                        msiGenesWithBiallelicDriver.add(gene)
                    } else {
                        msiGenesWithNonBiallelicDriver.add(gene)
                    }
                }
            }
            for (copyNumber in molecular.drivers.copyNumbers) {
                if (copyNumber.type == CopyNumberType.LOSS && copyNumber.gene == gene) {
                    msiGenesWithBiallelicDriver.add(gene)
                }
            }
            for (homozygousDisruption in molecular.drivers.homozygousDisruptions) {
                if (homozygousDisruption.gene == gene) {
                    msiGenesWithBiallelicDriver.add(gene)
                }
            }
            for (disruption in molecular.drivers.disruptions) {
                if (disruption.gene == gene && disruption.isReportable) {
                    msiGenesWithNonBiallelicDriver.add(gene)
                }
            }
        }
        val isMicrosatelliteUnstable = molecular.characteristics.isMicrosatelliteUnstable
        if (isMicrosatelliteUnstable == null) {
            return if (msiGenesWithBiallelicDriver.isNotEmpty()) {
                EvaluationFactory.undetermined(
                    "Unknown microsatellite instability (MSI) status, but biallelic drivers in MSI genes: "
                            + Format.concat(msiGenesWithBiallelicDriver) + " are detected; an MSI test may be recommended",
                    "Unknown MSI status but biallelic drivers in MSI genes"
                )
            } else if (msiGenesWithNonBiallelicDriver.isNotEmpty()) {
                EvaluationFactory.undetermined(
                    "Unknown microsatellite instability (MSI) status, but non-biallelic drivers in MSI genes: "
                            + Format.concat(msiGenesWithNonBiallelicDriver) + " are detected; an MSI test may be recommended",
                    "Unknown MSI status but non-biallelic drivers in MSI genes"
                )
            } else {
                EvaluationFactory.fail("Unknown microsatellite instability (MSI) status", "Unknown MSI status")
            }
        } else if (isMicrosatelliteUnstable) {
            val inclusionMolecularEvents = setOf(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
            return if (msiGenesWithBiallelicDriver.isNotEmpty()) {
                EvaluationFactory.pass(
                    "Microsatellite instability (MSI) status detected, together with biallelic drivers in MSI genes: "
                            + Format.concat(msiGenesWithBiallelicDriver),
                    "Tumor is MSI and biallelic drivers in MSI genes detected",
                    inclusionEvents = inclusionMolecularEvents
                )
            } else if (msiGenesWithNonBiallelicDriver.isNotEmpty()) {
                EvaluationFactory.warn(
                    "Microsatellite instability (MSI) detected, together with non-biallelic drivers in MSI genes ("
                            + Format.concat(MolecularConstants.MSI_GENES) + ") were detected",
                    "Tumor is MSI (but with only non-biallelic drivers in MSI genes)",
                    inclusionEvents = inclusionMolecularEvents
                )
            } else {
                EvaluationFactory.warn(
                    "Microsatellite instability (MSI) detected, without drivers in MSI genes ("
                            + Format.concat(MolecularConstants.MSI_GENES) + ") detected",
                    "Tumor is MSI (but without detected drivers in MSI genes)",
                    inclusionEvents = inclusionMolecularEvents
                )
            }
        }
        return EvaluationFactory.fail("Tumor is microsatellite stable (MSS)", "Tumor is MSS")
    }
}