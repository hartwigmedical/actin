package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.util.MolecularEventFactory;

import org.jetbrains.annotations.NotNull;

public class GeneIsWildType implements EvaluationFunction {

    @NotNull
    private final String gene;

    GeneIsWildType(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> reportableEventsWithEffect = Sets.newHashSet();
        Set<String> reportableEventsWithNoEffect = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene) && variant.isReportable()) {
                String variantEvent = MolecularEventFactory.event(variant);
                boolean hasNoEffect =
                        variant.proteinEffect() == ProteinEffect.NO_EFFECT || variant.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(variantEvent);
                } else {
                    reportableEventsWithEffect.add(variantEvent);
                }
            }
        }

        for (Amplification amplification : record.molecular().drivers().amplifications()) {
            if (amplification.gene().equals(gene) && amplification.isReportable() && amplification.geneRole() != GeneRole.TSG) {
                String ampEvent = MolecularEventFactory.event(amplification);
                boolean hasNoEffect = amplification.proteinEffect() == ProteinEffect.NO_EFFECT
                        || amplification.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(ampEvent);
                } else {
                    reportableEventsWithEffect.add(ampEvent);
                }
            }
        }

        for (Loss loss : record.molecular().drivers().losses()) {
            if (loss.gene().equals(gene) && loss.isReportable() && loss.geneRole() != GeneRole.ONCO) {
                String lossEvent = MolecularEventFactory.event(loss);
                boolean hasNoEffect =
                        loss.proteinEffect() == ProteinEffect.NO_EFFECT || loss.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(lossEvent);
                } else {
                    reportableEventsWithEffect.add(lossEvent);
                }
            }
        }

        for (HomozygousDisruption homozygousDisruption : record.molecular().drivers().homozygousDisruptions()) {
            if (homozygousDisruption.gene().equals(gene) && homozygousDisruption.isReportable()
                    && homozygousDisruption.geneRole() != GeneRole.ONCO) {
                String homDisruptionEvent = MolecularEventFactory.event(homozygousDisruption);
                boolean hasNoEffect = homozygousDisruption.proteinEffect() == ProteinEffect.NO_EFFECT
                        || homozygousDisruption.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(homDisruptionEvent);
                } else {
                    reportableEventsWithEffect.add(homDisruptionEvent);
                }
            }
        }

        for (Disruption disruption : record.molecular().drivers().disruptions()) {
            if (disruption.gene().equals(gene) && disruption.isReportable() && disruption.geneRole() != GeneRole.ONCO) {
                String disruptionEvent = MolecularEventFactory.event(disruption);
                boolean hasNoEffect = disruption.proteinEffect() == ProteinEffect.NO_EFFECT
                        || disruption.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(disruptionEvent);
                } else {
                    reportableEventsWithEffect.add(disruptionEvent);
                }
            }
        }

        for (Fusion fusion : record.molecular().drivers().fusions()) {
            if ((fusion.geneStart().equals(gene) || fusion.geneEnd().equals(gene)) && fusion.isReportable()) {
                String fusionEvent = MolecularEventFactory.event(fusion);
                boolean hasNoEffect =
                        fusion.proteinEffect() == ProteinEffect.NO_EFFECT || fusion.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED;
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(fusionEvent);
                } else {
                    reportableEventsWithEffect.add(fusionEvent);
                }
            }
        }

        if (!reportableEventsWithEffect.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Gene " + gene + " is not wild-type due to " + Format.concat(reportableEventsWithEffect))
                    .addFailGeneralMessages("No wild-type for gene " + gene)
                    .build();
        }

        if (!reportableEventsWithNoEffect.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages(
                            "Gene " + gene + " may be wild-type but consider events " + Format.concat(reportableEventsWithNoEffect))
                    .addWarnGeneralMessages("Potential wild-type for gene " + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages("Gene " + gene + " is wild-type")
                .addPassGeneralMessages("Gene " + gene + " is wild-type")
                .build();
    }
}
