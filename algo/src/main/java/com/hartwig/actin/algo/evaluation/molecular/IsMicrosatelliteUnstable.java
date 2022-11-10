package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents;

import org.jetbrains.annotations.NotNull;

public class IsMicrosatelliteUnstable implements EvaluationFunction {

    IsMicrosatelliteUnstable() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> msiGenesWithDriver = Sets.newHashSet();
        for (String gene : com.hartwig.actin.algo.evaluation.molecular.MolecularConstants.MSI_GENES) {
            for (Variant variant : record.molecular().drivers().variants()) {
                if (variant.gene().equals(gene) && variant.isReportable()) {
                    msiGenesWithDriver.add(gene);
                }
            }
            for (Loss loss : record.molecular().drivers().losses()) {
                if (loss.gene().equals(gene)) {
                    msiGenesWithDriver.add(gene);
                }
            }
        }

        Boolean isMicrosatelliteUnstable = record.molecular().characteristics().isMicrosatelliteUnstable();
        if (isMicrosatelliteUnstable == null) {
            if (!msiGenesWithDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(
                                "Unknown microsatellite instability (MSI) status, but drivers in MSI genes: " + Format.concat(
                                        msiGenesWithDriver) + " are detected; an MSI test may be recommended")
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
            if (!msiGenesWithDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE)
                        .addPassSpecificMessages(
                                "Microsatellite instability (MSI) status detected, together with drivers in MSI genes: " + Format.concat(
                                        msiGenesWithDriver))
                        .addPassGeneralMessages("MSI")
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.MICROSATELLITE_POTENTIALLY_UNSTABLE)
                        .addWarnSpecificMessages("Microsatellite instability (MSI) detected, but no drivers in MSI genes (" + Format.concat(
                                com.hartwig.actin.algo.evaluation.molecular.MolecularConstants.MSI_GENES) + ") were detected")
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