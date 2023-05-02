package com.hartwig.actin.algo.evaluation.molecular

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect

class HasFusionInGene internal constructor(private val gene: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingFusions: MutableSet<String> = Sets.newHashSet()
        val fusionsWithNoEffect: MutableSet<String> = Sets.newHashSet()
        val fusionsWithNoHighDriverLikelihood: MutableSet<String> = Sets.newHashSet()
        val unreportableFusionsWithGainOfFunction: MutableSet<String> = Sets.newHashSet()
        for (fusion in record.molecular().drivers().fusions()) {
            val isAllowedDriverType =
                fusion.geneStart() == fusion.geneEnd() || fusion.geneStart() == gene && ALLOWED_DRIVER_TYPES_FOR_GENE_5.contains(fusion.driverType()) || fusion.geneEnd() == gene && ALLOWED_DRIVER_TYPES_FOR_GENE_3.contains(
                    fusion.driverType()
                )
            if (isAllowedDriverType) {
                if (fusion.isReportable) {
                    val hasNoEffect = (fusion.proteinEffect() == ProteinEffect.NO_EFFECT
                            || fusion.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED)
                    if (fusion.driverLikelihood() != DriverLikelihood.HIGH) {
                        fusionsWithNoHighDriverLikelihood.add(fusion.event())
                    } else if (hasNoEffect) {
                        fusionsWithNoEffect.add(fusion.event())
                    } else {
                        matchingFusions.add(fusion.event())
                    }
                } else {
                    val isGainOfFunction = (fusion.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                            || fusion.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
                    if (isGainOfFunction) {
                        unreportableFusionsWithGainOfFunction.add(fusion.event())
                    }
                }
            }
        }
        if (!matchingFusions.isEmpty()) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addAllInclusionMolecularEvents(matchingFusions)
                .addPassSpecificMessages("Fusion(s) " + concat(matchingFusions) + " detected in gene " + gene)
                .addPassGeneralMessages("Fusion(s) detected in gene $gene")
                .build()
        }
        val potentialWarnEvaluation =
            evaluatePotentialWarns(fusionsWithNoEffect, fusionsWithNoHighDriverLikelihood, unreportableFusionsWithGainOfFunction)
        return potentialWarnEvaluation
            ?: unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No fusion detected with gene $gene")
                .addFailGeneralMessages("No fusion in gene $gene")
                .build()
    }

    private fun evaluatePotentialWarns(
        fusionsWithNoEffect: Set<String>,
        fusionsWithNoHighDriverLikelihood: Set<String>, unreportableFusionsWithGainOfFunction: Set<String>
    ): Evaluation? {
        val warnEvents: MutableSet<String> = Sets.newHashSet()
        val warnSpecificMessages: MutableSet<String> = Sets.newHashSet()
        val warnGeneralMessages: MutableSet<String> = Sets.newHashSet()
        if (!fusionsWithNoEffect.isEmpty()) {
            warnEvents.addAll(fusionsWithNoEffect)
            warnSpecificMessages.add(
                "Fusion(s) " + concat(fusionsWithNoEffect) + " detected in gene " + gene
                        + " but annotated as having no protein effect"
            )
            warnGeneralMessages.add("Fusion(s) detected in $gene but annotated as having no protein effect")
        }
        if (!fusionsWithNoHighDriverLikelihood.isEmpty()) {
            warnEvents.addAll(fusionsWithNoHighDriverLikelihood)
            warnSpecificMessages.add(
                "Fusion(s) " + concat(fusionsWithNoHighDriverLikelihood) + " detected in gene " + gene
                        + " but not with high driver likelihood"
            )
            warnGeneralMessages.add("Fusion(s) detected in gene $gene but no high driver likelihood")
        }
        if (!unreportableFusionsWithGainOfFunction.isEmpty()) {
            warnEvents.addAll(unreportableFusionsWithGainOfFunction)
            warnSpecificMessages.add(
                "Fusion(s) " + concat(unreportableFusionsWithGainOfFunction) + " detected in gene " + gene
                        + " but not considered reportable; however fusion is annotated as having gain-of-function"
            )
            warnGeneralMessages.add("Fusion(s) detected in gene $gene but unreportable but with gain-of-function")
        }
        return if (!warnEvents.isEmpty() && !warnSpecificMessages.isEmpty() && !warnGeneralMessages.isEmpty()) {
            unrecoverable()
                .result(EvaluationResult.WARN)
                .addAllInclusionMolecularEvents(warnEvents)
                .addAllWarnSpecificMessages(warnSpecificMessages)
                .addAllWarnGeneralMessages(warnGeneralMessages)
                .build()
        } else null
    }

    companion object {
        val ALLOWED_DRIVER_TYPES_FOR_GENE_5: Set<FusionDriverType> = Sets.newHashSet(
            FusionDriverType.KNOWN_PAIR,
            FusionDriverType.KNOWN_PAIR_DEL_DUP,
            FusionDriverType.PROMISCUOUS_BOTH,
            FusionDriverType.PROMISCUOUS_5
        )
        val ALLOWED_DRIVER_TYPES_FOR_GENE_3: Set<FusionDriverType> = Sets.newHashSet(
            FusionDriverType.KNOWN_PAIR,
            FusionDriverType.KNOWN_PAIR_DEL_DUP,
            FusionDriverType.PROMISCUOUS_BOTH,
            FusionDriverType.PROMISCUOUS_3,
            FusionDriverType.PROMISCUOUS_ENHANCER_TARGET
        )
    }
}