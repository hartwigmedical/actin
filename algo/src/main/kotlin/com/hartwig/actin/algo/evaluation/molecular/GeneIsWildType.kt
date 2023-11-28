package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format

class GeneIsWildType internal constructor(private val gene: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val reportableEventsWithEffect: MutableSet<String> = mutableSetOf()
        val reportableEventsWithEffectPotentiallyWildtype: MutableSet<String> = mutableSetOf()
        val reportableEventsWithNoEffect: MutableSet<String> = mutableSetOf()
        val evidenceSource = record.molecular().evidenceSource()

        for (variant in record.molecular().drivers().variants()) {
            if (variant.gene() == gene && variant.isReportable) {
                val hasNoEffect =
                    variant.proteinEffect() == ProteinEffect.NO_EFFECT || variant.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED
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
                val hasNoEffect =
                    fusion.proteinEffect() == ProteinEffect.NO_EFFECT || fusion.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(fusion.event())
                } else {
                    reportableEventsWithEffect.add(fusion.event())
                }
            }
        }
        if (reportableEventsWithEffect.isNotEmpty()) {
            return EvaluationFactory.fail(
                "Gene " + gene + " is not considered wild-type due to " + Format.concat(reportableEventsWithEffect),
                "$gene not wild-type"
            )
        }
        val potentialWarnEvaluation =
            evaluatePotentialWarns(reportableEventsWithNoEffect, reportableEventsWithEffectPotentiallyWildtype, evidenceSource)
        return potentialWarnEvaluation
            ?: EvaluationFactory.unrecoverable()
                .result(EvaluationResult.PASS)
                .addInclusionMolecularEvents("$gene wild-type")
                .addPassSpecificMessages("Gene $gene is considered wild-type")
                .addPassGeneralMessages("$gene is wild-type")
                .build()
    }

    private fun evaluatePotentialWarns(
        reportableEventsWithNoEffect: Set<String>,
        reportableEventsWithEffectPotentiallyWildtype: Set<String>, evidenceSource: String
    ): Evaluation? {
        val warnEvents: MutableSet<String> = mutableSetOf()
        val warnSpecificMessages: MutableSet<String> = mutableSetOf()
        val warnGeneralMessages: MutableSet<String> = mutableSetOf()
        if (reportableEventsWithNoEffect.isNotEmpty()) {
            warnEvents.addAll(reportableEventsWithNoEffect)
            warnSpecificMessages.add("Reportable event(s) in " + gene + " are detected: " + Format.concat(reportableEventsWithNoEffect) + ", however these are annotated with protein effect 'no effect' in $evidenceSource and thus may potentially be considered wild-type")
            warnGeneralMessages.add("$gene potentially wild-type: event(s) are reportable but protein effect 'no effect' in $evidenceSource")
        }
        if (reportableEventsWithEffectPotentiallyWildtype.isNotEmpty()) {
            warnEvents.addAll(reportableEventsWithEffectPotentiallyWildtype)
            warnSpecificMessages.add(
                "Reportable event(s) in " + gene + " are detected: " + Format.concat(reportableEventsWithEffectPotentiallyWildtype) + " which may potentially be considered wild-type"
            )
            warnGeneralMessages.add("$gene potentially wild-type but event(s) are reportable and have a protein effect in $evidenceSource")
        }
        return if (warnEvents.isNotEmpty() && warnSpecificMessages.isNotEmpty() && warnGeneralMessages.isNotEmpty()) {
            EvaluationFactory.unrecoverable()
                .result(EvaluationResult.WARN)
                .addAllInclusionMolecularEvents(warnEvents)
                .addAllWarnSpecificMessages(warnSpecificMessages)
                .addAllWarnGeneralMessages(warnGeneralMessages)
                .build()
        } else null
    }
}