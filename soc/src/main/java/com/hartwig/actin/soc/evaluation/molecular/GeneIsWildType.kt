package com.hartwig.actin.soc.evaluation.molecular

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect

class GeneIsWildType internal constructor(private val gene: String) : EvaluationFunction {
    fun evaluate(record: PatientRecord): Evaluation {
        val reportableEventsWithEffect: MutableSet<String> = Sets.newHashSet()
        val reportableEventsWithEffectPotentiallyWildtype: MutableSet<String> = Sets.newHashSet()
        val reportableEventsWithNoEffect: MutableSet<String> = Sets.newHashSet()
        for (variant in record.molecular().drivers().variants()) {
            if (variant.gene() == gene && variant.isReportable) {
                val hasNoEffect = variant.proteinEffect() == ProteinEffect.NO_EFFECT || variant.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(variant.event())
                } else if (variant.driverLikelihood() != DriverLikelihood.HIGH) {
                    reportableEventsWithEffectPotentiallyWildtype.add(variant.event())
                } else {
                    reportableEventsWithEffect.add(variant.event())
                }
            }
        }
        for (copyNumber in record.molecular().drivers().copyNumbers()) {
            if (copyNumber.gene() == gene && copyNumber.isReportable) {
                val hasNoEffect = (copyNumber.proteinEffect() == ProteinEffect.NO_EFFECT
                        || copyNumber.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED)
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(copyNumber.event())
                } else {
                    reportableEventsWithEffectPotentiallyWildtype.add(copyNumber.event())
                }
            }
        }
        for (homozygousDisruption in record.molecular().drivers().homozygousDisruptions()) {
            if (homozygousDisruption.gene() == gene && homozygousDisruption.isReportable && homozygousDisruption.geneRole() != GeneRole.ONCO) {
                val hasNoEffect = (homozygousDisruption.proteinEffect() == ProteinEffect.NO_EFFECT
                        || homozygousDisruption.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED)
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(homozygousDisruption.event())
                } else {
                    reportableEventsWithEffect.add(homozygousDisruption.event())
                }
            }
        }
        for (disruption in record.molecular().drivers().disruptions()) {
            if (disruption.gene() == gene && disruption.isReportable && disruption.geneRole() != GeneRole.ONCO) {
                val hasNoEffect = (disruption.proteinEffect() == ProteinEffect.NO_EFFECT
                        || disruption.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED)
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(disruption.event())
                } else {
                    reportableEventsWithEffect.add(disruption.event())
                }
            }
        }
        for (fusion in record.molecular().drivers().fusions()) {
            if ((fusion.geneStart() == gene || fusion.geneEnd() == gene) && fusion.isReportable) {
                val hasNoEffect = fusion.proteinEffect() == ProteinEffect.NO_EFFECT || fusion.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(fusion.event())
                } else {
                    reportableEventsWithEffect.add(fusion.event())
                }
            }
        }
        if (!reportableEventsWithEffect.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages(
                            "Gene " + gene + " is not considered wild-type due to " + Format.concat(reportableEventsWithEffect))
                    .addFailGeneralMessages("$gene not wild-type")
                    .build()
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(reportableEventsWithNoEffect, reportableEventsWithEffectPotentiallyWildtype)
        return potentialWarnEvaluation
                ?: EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addInclusionMolecularEvents("$gene wild-type")
                        .addPassSpecificMessages("Gene $gene is considered wild-type")
                        .addPassGeneralMessages("$gene wild-type")
                        .build()
    }

    private fun evaluatePotentialWarns(reportableEventsWithNoEffect: Set<String>,
                                       reportableEventsWithEffectPotentiallyWildtype: Set<String>): Evaluation? {
        val warnEvents: MutableSet<String> = Sets.newHashSet()
        val warnSpecificMessages: MutableSet<String> = Sets.newHashSet()
        val warnGeneralMessages: MutableSet<String> = Sets.newHashSet()
        if (!reportableEventsWithNoEffect.isEmpty()) {
            warnEvents.addAll(reportableEventsWithNoEffect)
            warnSpecificMessages.add("Reportable event(s) in " + gene + " are detected: " + Format.concat(reportableEventsWithNoEffect) + ", however these are annotated with protein effect 'no effect' and thus may potentially be considered wild-type?")
            warnGeneralMessages.add("$gene potentially wild-type")
        }
        if (!reportableEventsWithEffectPotentiallyWildtype.isEmpty()) {
            warnEvents.addAll(reportableEventsWithEffectPotentiallyWildtype)
            warnSpecificMessages.add(
                    "Reportable event(s) in " + gene + " are detected: " + Format.concat(reportableEventsWithEffectPotentiallyWildtype) + " which may potentially be considered wild-type?")
            warnGeneralMessages.add("$gene potentially wild-type")
        }
        return if (!warnEvents.isEmpty() && !warnSpecificMessages.isEmpty() && !warnGeneralMessages.isEmpty()) {
            EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(warnEvents)
                    .addAllWarnSpecificMessages(warnSpecificMessages)
                    .addAllWarnGeneralMessages(warnGeneralMessages)
                    .build()
        } else null
    }
}