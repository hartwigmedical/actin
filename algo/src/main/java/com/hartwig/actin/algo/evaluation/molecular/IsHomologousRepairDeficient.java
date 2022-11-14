package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents;

import org.jetbrains.annotations.NotNull;

public class IsHomologousRepairDeficient implements EvaluationFunction {

    IsHomologousRepairDeficient() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> hrdGenesWithBiallelicDriver = Sets.newHashSet();
        Set<String> hrdGenesWithNonBiallelicDriver = Sets.newHashSet();

        for (String gene : MolecularConstants.HRD_GENES) {
            for (Variant variant : record.molecular().drivers().variants()) {
                if (variant.gene().equals(gene) && variant.isReportable()) {
                    if (variant.isBiallelic()) {
                        hrdGenesWithBiallelicDriver.add(gene);
                    } else {
                        hrdGenesWithNonBiallelicDriver.add(gene);
                    }
                }
            }

            for (Loss loss : record.molecular().drivers().losses()) {
                if (loss.gene().equals(gene)) {
                    hrdGenesWithBiallelicDriver.add(gene);
                }
            }

            for (HomozygousDisruption homozygousDisruption : record.molecular().drivers().homozygousDisruptions()) {
                if (homozygousDisruption.gene().equals(gene)) {
                    hrdGenesWithBiallelicDriver.add(gene);
                }
            }

            for (Disruption disruption : record.molecular().drivers().disruptions()) {
                if (disruption.gene().equals(gene) && disruption.isReportable()) {
                    hrdGenesWithNonBiallelicDriver.add(gene);
                }
            }
        }

        Boolean isHomologousRepairDeficient = record.molecular().characteristics().isHomologousRepairDeficient();
        if (isHomologousRepairDeficient == null) {
            if (!hrdGenesWithBiallelicDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(
                                "Unknown homologous repair deficiency (HRD) status, but biallelic drivers in HRD genes: " + Format.concat(
                                        hrdGenesWithBiallelicDriver) + " are detected; an HRD test may be recommended")
                        .addUndeterminedGeneralMessages("Unknown HRD status")
                        .build();
            } else if (!hrdGenesWithNonBiallelicDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.UNDETERMINED)
                        .addUndeterminedSpecificMessages(
                                "Unknown homologous repair deficiency (HRD) status, but non-biallelic drivers in HRD genes: "
                                        + Format.concat(hrdGenesWithBiallelicDriver) + " are detected; an HRD test may be recommended")
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
            if (!hrdGenesWithBiallelicDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.HOMOLOGOUS_REPAIR_DEFICIENT)
                        .addPassSpecificMessages(
                                "Homologous repair deficiency (HRD) status detected, together with biallelic drivers in HRD genes: "
                                        + Format.concat(hrdGenesWithBiallelicDriver))
                        .addPassGeneralMessages("HRD")
                        .build();
            } else if (!hrdGenesWithNonBiallelicDriver.isEmpty()) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.HOMOLOGOUS_REPAIR_POTENTIALLY_DEFICIENT)
                        .addWarnSpecificMessages(
                                "Homologous repair deficiency (HRD) status detected, together with non-biallelic drivers in HRD genes ("
                                        + Format.concat(MolecularConstants.HRD_GENES) + ") were detected")
                        .addWarnGeneralMessages("HRD")
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.HOMOLOGOUS_REPAIR_POTENTIALLY_DEFICIENT)
                        .addWarnSpecificMessages(
                                "Homologous repair deficiency (HRD) status detected, without drivers in HRD genes (" + Format.concat(
                                        MolecularConstants.HRD_GENES) + ") detected")
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