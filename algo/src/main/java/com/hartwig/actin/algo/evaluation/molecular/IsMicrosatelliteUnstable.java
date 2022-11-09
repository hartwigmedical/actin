package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.util.MolecularEventFactory;

import org.jetbrains.annotations.NotNull;

public class IsMicrosatelliteUnstable implements EvaluationFunction {

    static final Set<String> MSI_GENES = Sets.newHashSet();

    static {
        MSI_GENES.add("MLH1");
        MSI_GENES.add("MSH2");
        MSI_GENES.add("MSH6");
        MSI_GENES.add("PMS2");
        MSI_GENES.add("EPCAM");
    }

    IsMicrosatelliteUnstable() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> msiGenesWithDriver = Sets.newHashSet();
        Set<String> msiGenesWithPreviousMutation = Sets.newHashSet();
        for (String gene : MSI_GENES) {
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
            for (PriorMolecularTest priorTest : record.clinical().priorMolecularTests()) {
                if (priorTest.item().equals(gene)) {
                    // TODO Determine whether a mutation was found
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
                        .addPassSpecificMessages(
                                "Microsatellite instability (MSI) status detected, together with drivers in MSI genes: " + Format.concat(
                                        msiGenesWithDriver))
                        .addPassGeneralMessages("MSI")
                        .addInclusionMolecularEvents(MolecularEventFactory.MICROSATELLITE_UNSTABLE)
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addWarnSpecificMessages(
                                "Microsatellite instability (MSI) detected, but no drivers in MSI genes (" + Format.concat(MSI_GENES)
                                        + ") were detected")
                        .addWarnGeneralMessages("MSI")
                        .addInclusionMolecularEvents(MolecularEventFactory.MICROSATELLITE_POTENTIALLY_UNSTABLE)
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