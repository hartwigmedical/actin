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
import com.hartwig.actin.molecular.util.MolecularEventFactory;

import org.jetbrains.annotations.NotNull;

public class IsHomologousRepairDeficient implements EvaluationFunction {

    IsHomologousRepairDeficient() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> hrdGenesWithDriver = Sets.newHashSet();
        for (String gene : MolecularConstants.HRD_GENES) {
            for (Variant variant : record.molecular().drivers().variants()) {
                if (variant.gene().equals(gene) && variant.isReportable()) {
                    hrdGenesWithDriver.add(gene);
                }
            }
            for (Loss loss : record.molecular().drivers().losses()) {
                if (loss.gene().equals(gene)) {
                    hrdGenesWithDriver.add(gene);
                }
            }
        }

        Boolean isHomologousRepairDeficient = record.molecular().characteristics().isHomologousRepairDeficient();
        if (isHomologousRepairDeficient == null) {
            if (!hrdGenesWithDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(
                                "Unknown homologous repair deficiency (HRD) status, but drivers in HRD genes: " + Format.concat(
                                        hrdGenesWithDriver) + " are detected; a HRD test may be recommended")
                        .addUndeterminedGeneralMessages("Unknown HRD status")
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.FAIL)
                        .addFailSpecificMessages("Unknown homologous repair deficiency (HRD) status")
                        .addFailGeneralMessages("Unknown HRD status")
                        .build();
            }
        } else if (isHomologousRepairDeficient) {
            if (!hrdGenesWithDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addInclusionMolecularEvents(MolecularEventFactory.HOMOLOGOUS_REPAIR_DEFICIENT)
                        .addPassSpecificMessages(
                                "Homologous repair deficiency (HRD) status detected, together with drivers in HRD genes: " + Format.concat(
                                        hrdGenesWithDriver))
                        .addPassGeneralMessages("HRD")
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addInclusionMolecularEvents(MolecularEventFactory.HOMOLOGOUS_REPAIR_POTENTIALLY_DEFICIENT)
                        .addWarnSpecificMessages(
                                "Homologous repair deficiency (HRD) status detected, but no drivers in HRD genes (" + Format.concat(
                                        MolecularConstants.HRD_GENES) + ") were detected")
                        .addWarnGeneralMessages("HRD")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No homologous repair deficiency (HRD) status detected")
                .addFailGeneralMessages("HRD")
                .build();
    }
}
