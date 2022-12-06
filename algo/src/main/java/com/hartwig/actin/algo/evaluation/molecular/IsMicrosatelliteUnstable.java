package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents;

import org.jetbrains.annotations.NotNull;

public class IsMicrosatelliteUnstable implements EvaluationFunction {

    IsMicrosatelliteUnstable() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> msiGenesWithBiallelicDriver = Sets.newHashSet();
        Set<String> msiGenesWithNonBiallelicDriver = Sets.newHashSet();

        for (String gene : MolecularConstants.MSI_GENES) {
            for (Variant variant : record.molecular().drivers().variants()) {
                if (variant.gene().equals(gene) && variant.isReportable()) {
                    if (variant.isBiallelic()) {
                        msiGenesWithBiallelicDriver.add(gene);
                    } else {
                        msiGenesWithNonBiallelicDriver.add(gene);
                    }
                }
            }

            for (CopyNumber copyNumber : record.molecular().drivers().copyNumbers()) {
                if (copyNumber.type() == CopyNumberType.LOSS && copyNumber.gene().equals(gene)) {
                    msiGenesWithBiallelicDriver.add(gene);
                }
            }

            for (HomozygousDisruption homozygousDisruption : record.molecular().drivers().homozygousDisruptions()) {
                if (homozygousDisruption.gene().equals(gene)) {
                    msiGenesWithBiallelicDriver.add(gene);
                }
            }

            for (Disruption disruption : record.molecular().drivers().disruptions()) {
                if (disruption.gene().equals(gene) && disruption.isReportable()) {
                    msiGenesWithNonBiallelicDriver.add(gene);
                }
            }
        }

        Boolean isMicrosatelliteUnstable = record.molecular().characteristics().isMicrosatelliteUnstable();
        if (isMicrosatelliteUnstable == null) {
            if (!msiGenesWithBiallelicDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(
                                "Unknown microsatellite instability (MSI) status, but biallelic drivers in MSI genes: " + Format.concat(
                                        msiGenesWithBiallelicDriver) + " are detected; an MSI test may be recommended")
                        .addUndeterminedGeneralMessages("Unknown MSI status")
                        .build();
            } else if (!msiGenesWithNonBiallelicDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(
                                "Unknown microsatellite instability (MSI) status, but non-biallelic drivers in MSI genes: " + Format.concat(
                                        msiGenesWithNonBiallelicDriver) + " are detected; an MSI test may be recommended")
                        .addUndeterminedGeneralMessages("Unknown MSI status")
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.FAIL)
                        .addFailSpecificMessages("Unknown microsatellite instability (MSI) status")
                        .addFailGeneralMessages("Unknown MSI status")
                        .build();
            }
        } else if (isMicrosatelliteUnstable) {
            if (!msiGenesWithBiallelicDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
                        .addPassSpecificMessages(
                                "Microsatellite instability (MSI) status detected, together with biallelic drivers in MSI genes: "
                                        + Format.concat(msiGenesWithBiallelicDriver))
                        .addPassGeneralMessages("MSI")
                        .build();
            } else if (!msiGenesWithNonBiallelicDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.MICROSATELLITE_POTENTIALLY_UNSTABLE)
                        .addWarnSpecificMessages(
                                "Microsatellite instability (MSI) detected, together with non-biallelic drivers in MSI genes ("
                                        + Format.concat(com.hartwig.actin.algo.evaluation.molecular.MolecularConstants.MSI_GENES)
                                        + ") were detected")
                        .addWarnGeneralMessages("MSI")
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.MICROSATELLITE_POTENTIALLY_UNSTABLE)
                        .addWarnSpecificMessages("Microsatellite instability (MSI) detected, without drivers in MSI genes ("
                                + Format.concat(com.hartwig.actin.algo.evaluation.molecular.MolecularConstants.MSI_GENES) + ") detected")
                        .addWarnGeneralMessages("MSI")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No microsatellite instability (MSI) status detected")
                .addFailGeneralMessages("MSI")
                .build();
    }
}