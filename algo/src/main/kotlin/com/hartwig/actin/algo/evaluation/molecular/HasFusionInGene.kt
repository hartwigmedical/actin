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
        val fusionsWithNoHighDriverLikelihoodWithGainOfFunction: MutableSet<String> = Sets.newHashSet()
        val fusionsWithNoHighDriverLikelihoodOther: MutableSet<String> = Sets.newHashSet()
        val unreportableFusionsWithGainOfFunction: MutableSet<String> = Sets.newHashSet()

        for (fusion in record.molecular().drivers().fusions()) {
            val isAllowedDriverType =
                (fusion.geneStart() == gene && fusion.geneStart() == fusion.geneEnd()) ||
                        (fusion.geneStart() == gene && ALLOWED_DRIVER_TYPES_FOR_GENE_5.contains(fusion.driverType())) ||
                        (fusion.geneEnd() == gene && ALLOWED_DRIVER_TYPES_FOR_GENE_3.contains(fusion.driverType()))
            if (isAllowedDriverType) {
                val isGainOfFunction =
                    (fusion.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION ||
                            fusion.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
                if (fusion.isReportable) {
                    val hasNoEffect =
                        (fusion.proteinEffect() == ProteinEffect.NO_EFFECT
                                || fusion.proteinEffect() == ProteinEffect.NO_EFFECT_PREDICTED)
                    if (fusion.driverLikelihood() != DriverLikelihood.HIGH) {
                        if (isGainOfFunction) {
                            fusionsWithNoHighDriverLikelihoodWithGainOfFunction.add(fusion.event())
                        } else {
                            fusionsWithNoHighDriverLikelihoodOther.add(fusion.event())
                        }
                    } else if (hasNoEffect) {
                        fusionsWithNoEffect.add(fusion.event())
                    } else {
                        matchingFusions.add(fusion.event())
                    }
                } else {
                    if (isGainOfFunction) {
                        unreportableFusionsWithGainOfFunction.add(fusion.event())
                    }
                }
            }
        }

        if (matchingFusions.isNotEmpty()) {
            return unrecoverable().result(EvaluationResult.PASS).addAllInclusionMolecularEvents(matchingFusions)
                .addPassSpecificMessages("Fusion(s) " + concat(matchingFusions) + " detected in gene " + gene)
                .addPassGeneralMessages("Fusion(s) detected in gene $gene").build()
        }

        val potentialWarnEvaluation = evaluatePotentialWarns(
            fusionsWithNoEffect,
            fusionsWithNoHighDriverLikelihoodWithGainOfFunction,
            fusionsWithNoHighDriverLikelihoodOther,
            unreportableFusionsWithGainOfFunction
        )

        return potentialWarnEvaluation ?: unrecoverable().result(EvaluationResult.FAIL)
            .addFailSpecificMessages("No fusion detected with gene $gene")
            .addFailGeneralMessages("No fusion in gene $gene").build()
    }

    private fun evaluatePotentialWarns(
        fusionsWithNoEffect: Set<String>,
        fusionsWithNoHighDriverLikelihoodWithGainOfFunction: Set<String>,
        fusionsWithNoHighDriverLikelihoodOther: Set<String>,
        unreportableFusionsWithGainOfFunction: Set<String>
    ): Evaluation? {
        val warnEvents: MutableSet<String> = Sets.newHashSet()
        val warnSpecificMessages: MutableSet<String> = Sets.newHashSet()
        val warnGeneralMessages: MutableSet<String> = Sets.newHashSet()

        if (fusionsWithNoEffect.isNotEmpty()) {
            warnEvents.addAll(fusionsWithNoEffect)
            warnSpecificMessages.add(
                "Fusion(s) " + concat(fusionsWithNoEffect) + " detected in gene " + gene +
                        " but annotated with having no protein effect evidence"
            )
            warnGeneralMessages.add("Fusion(s) detected in $gene but annotated with having no protein effect evidence")
        }

        if (fusionsWithNoHighDriverLikelihoodWithGainOfFunction.isNotEmpty()) {
            warnEvents.addAll(fusionsWithNoHighDriverLikelihoodWithGainOfFunction)
            warnSpecificMessages.add(
                "Fusion(s) " + concat(fusionsWithNoHighDriverLikelihoodWithGainOfFunction) + " detected in gene " + gene +
                        " without high driver likelihood but annotated with having gain-of-function evidence"
            )
            warnGeneralMessages.add(
                "Fusion(s) detected in gene $gene without high driver likelihood " +
                        "but annotated with having gain-of-function evidence"
            )
        }

        if (fusionsWithNoHighDriverLikelihoodOther.isNotEmpty()) {
            warnEvents.addAll(fusionsWithNoHighDriverLikelihoodOther)
            warnSpecificMessages.add(
                "Fusion(s) " + concat(fusionsWithNoHighDriverLikelihoodOther) + " detected in gene " + gene +
                        " but not with high driver likelihood"
            )
            warnGeneralMessages.add("Fusion(s) detected in gene $gene but no high driver likelihood")
        }

        if (unreportableFusionsWithGainOfFunction.isNotEmpty()) {
            warnEvents.addAll(unreportableFusionsWithGainOfFunction)
            warnSpecificMessages.add(
                "Fusion(s) " + concat(unreportableFusionsWithGainOfFunction) + " detected in gene " + gene +
                        " but not considered reportable; however fusion is annotated with having gain-of-function evidence"
            )
            warnGeneralMessages.add("Not reportable fusion(s) detected in gene $gene but annotated with having gain-of-function evidence")
        }

        return if (warnEvents.isNotEmpty() && warnSpecificMessages.isNotEmpty() && warnGeneralMessages.isNotEmpty()) {
            unrecoverable().result(EvaluationResult.WARN).addAllInclusionMolecularEvents(warnEvents)
                .addAllWarnSpecificMessages(warnSpecificMessages).addAllWarnGeneralMessages(warnGeneralMessages).build()
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