package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.concatFusions
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import java.time.LocalDate

class HasFusionInGene(override val gene: String, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(targetCoveragePredicate = specific(MolecularTestTarget.FUSION, "Fusion in"), maxTestAge = maxTestAge) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val matchingFusions: MutableSet<String> = mutableSetOf()
        val fusionsWithNoEffect: MutableSet<String> = mutableSetOf()
        val fusionsWithNoHighDriverLikelihood: MutableSet<String> = mutableSetOf()
        val unreportableFusionsWithGainOfFunction: MutableSet<String> = mutableSetOf()
        val evidenceSource = test.evidenceSource

        for (fusion in test.drivers.fusions) {
            val isAllowedDriverType =
                (fusion.geneStart == gene && fusion.geneStart == fusion.geneEnd) ||
                        (fusion.geneStart == gene && ALLOWED_DRIVER_TYPES_FOR_GENE_5.contains(fusion.driverType)) ||
                        (fusion.geneEnd == gene && ALLOWED_DRIVER_TYPES_FOR_GENE_3.contains(fusion.driverType))
            if (isAllowedDriverType) {
                val isGainOfFunction =
                    (fusion.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION ||
                            fusion.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
                if (fusion.isReportable) {
                    val hasNoEffect =
                        (fusion.proteinEffect == ProteinEffect.NO_EFFECT || fusion.proteinEffect == ProteinEffect.NO_EFFECT_PREDICTED)
                    if (fusion.driverLikelihood != DriverLikelihood.HIGH) {
                        fusionsWithNoHighDriverLikelihood.add(fusion.event)
                    } else if (hasNoEffect) {
                        fusionsWithNoEffect.add(fusion.event)
                    } else {
                        matchingFusions.add(fusion.event)
                    }
                } else {
                    if (isGainOfFunction) {
                        unreportableFusionsWithGainOfFunction.add(fusion.event)
                    }
                }
            }
        }

        val anyWarns = listOf(
            fusionsWithNoEffect,
            fusionsWithNoHighDriverLikelihood,
            unreportableFusionsWithGainOfFunction
        ).any { it.isNotEmpty() }

        return when {
            matchingFusions.isNotEmpty() && !anyWarns -> {
                EvaluationFactory.pass("Fusion(s) ${concatFusions(matchingFusions)} in $gene", inclusionEvents = matchingFusions)
            }

            matchingFusions.isNotEmpty() -> {
                val eventWarningDescriptions = concat(listOf(
                    fusionsWithNoEffect.map { event -> "$event: Fusion having no protein effect" },
                    fusionsWithNoHighDriverLikelihood.map { event -> "$event: Fusion having no high driver likelihood" },
                    unreportableFusionsWithGainOfFunction.map { event -> "$event: Fusion having gain-of-function evidence but not considered reportable" }
                ).flatten())

                EvaluationFactory.warn(
                    "Fusion(s) ${concatFusions(matchingFusions)} in $gene together with other fusion events(s): " + eventWarningDescriptions,
                    inclusionEvents = matchingFusions + fusionsWithNoEffect + fusionsWithNoHighDriverLikelihood + unreportableFusionsWithGainOfFunction
                )
            }

            else -> {
                val potentialWarnEvaluation = evaluatePotentialWarns(
                    fusionsWithNoEffect,
                    fusionsWithNoHighDriverLikelihood,
                    unreportableFusionsWithGainOfFunction,
                    evidenceSource
                )

                potentialWarnEvaluation ?: EvaluationFactory.fail("No fusion in $gene")
            }
        }
    }

    private fun evaluatePotentialWarns(
        fusionsWithNoEffect: Set<String>,
        fusionsWithNoHighDriverLikelihood: Set<String>,
        unreportableFusionsWithGainOfFunction: Set<String>,
        evidenceSource: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    fusionsWithNoEffect,
                    "Fusion(s) ${concatFusions(fusionsWithNoEffect)} in $gene but annotated with having no protein effect evidence " +
                            "in $evidenceSource"
                ),
                EventsWithMessages(
                    fusionsWithNoHighDriverLikelihood,
                    "Fusion(s) ${concatFusions(fusionsWithNoHighDriverLikelihood)} in $gene but not with high driver likelihood",
                ),
                EventsWithMessages(
                    unreportableFusionsWithGainOfFunction,
                    "Unreportable fusion(s) ${concatFusions(unreportableFusionsWithGainOfFunction)} in $gene"
                            + " however annotated with having gain-of-function evidence in $evidenceSource"
                )
            )
        )
    }

    companion object {
        val ALLOWED_DRIVER_TYPES_FOR_GENE_5: Set<FusionDriverType> = setOf(
            FusionDriverType.KNOWN_PAIR,
            FusionDriverType.KNOWN_PAIR_DEL_DUP,
            FusionDriverType.PROMISCUOUS_BOTH,
            FusionDriverType.PROMISCUOUS_5
        )

        val ALLOWED_DRIVER_TYPES_FOR_GENE_3: Set<FusionDriverType> = setOf(
            FusionDriverType.KNOWN_PAIR,
            FusionDriverType.KNOWN_PAIR_DEL_DUP,
            FusionDriverType.PROMISCUOUS_BOTH,
            FusionDriverType.PROMISCUOUS_3,
            FusionDriverType.PROMISCUOUS_ENHANCER_TARGET
        )
    }
}