package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Collections;
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

import org.jetbrains.annotations.NotNull;

//TODO: Add implementation for previous molecular tests
public class IsHomologousRepairDeficient implements EvaluationFunction {

    static final Set<String> HRD_GENES = Sets.newHashSet();

    static {
        HRD_GENES.add("BRCA1");
        HRD_GENES.add("BRCA2");
        HRD_GENES.add("RAD51C");
        HRD_GENES.add("PALB2");
    }

    IsHomologousRepairDeficient() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> hrdGenesWithVariant = Sets.newHashSet();
        Set<String> hrdGenesWithLoss = Sets.newHashSet();

        for (String gene : HRD_GENES) {
            for (Variant variant : record.molecular().drivers().variants()) {
                if (variant.gene().equals(gene) && variant.isReportable()) {
                    hrdGenesWithVariant.add(gene);
                }
            }
            for (Loss loss : record.molecular().drivers().losses()) {
                if (loss.gene().equals(gene)) {
                    hrdGenesWithLoss.add(gene);
                }
            }
        }

        Set<String> hrdGenesWithDriver = Sets.union(hrdGenesWithVariant, hrdGenesWithLoss);
        Boolean hasHrdGenesWithDriver = !hrdGenesWithDriver.isEmpty();

        Boolean isHomologousRepairDeficient = record.molecular().characteristics().isHomologousRepairDeficient();

        if (isHomologousRepairDeficient == null) {
            if (hasHrdGenesWithDriver) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages("Unknown homologous repair deficiency (HRD) status, but drivers in HRD genes: "
                                + Format.concat(hrdGenesWithDriver) + " are detected; a HRD test may be recommended")
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
            if (hasHrdGenesWithDriver) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages(
                                "Homologous repair deficiency (HRD) status detected, together with drivers in HRD genes: " + Format.concat(
                                        hrdGenesWithDriver))
                        .addPassGeneralMessages("HRD")
                        .addAllInclusionMolecularEvents(Collections.singleton("HRD"))
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addWarnSpecificMessages(
                                "Homologous repair deficiency (HRD) status detected, but no drivers in HRD genes (" + Format.concat(
                                        HRD_GENES) + ") were detected")
                        .addWarnGeneralMessages("HRD")
                        .addAllInclusionMolecularEvents(Collections.singleton("HRD"))
                        .build();
            }
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("No homologous repair deficiency (HRD) status detected")
                    .addFailGeneralMessages("HRD")
                    .build();
        }
    }
}
