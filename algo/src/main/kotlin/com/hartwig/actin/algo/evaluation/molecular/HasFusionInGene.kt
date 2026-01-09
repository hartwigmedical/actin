package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.concatFusions
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.molecular.util.GeneConstants

class HasFusionInGene(override val gene: String) :
    MolecularEvaluationFunction(targetCoveragePredicate = specific(MolecularTestTarget.FUSION, "Fusion in")) {

    override fun evaluate(test: MolecularTest, ihcTests: List<IhcTest>): Evaluation {
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

        val ihcTestEvaluation = if (gene in GeneConstants.IHC_FUSION_EVALUABLE_GENES) IhcTestEvaluation.create(gene, ihcTests) else null

        val (ihcEventsThatQualify, ihcEventsThatAreIndeterminate) = when {
            ihcTestEvaluation?.hasCertainExactPositiveResultsForItem() == true -> setOf("$gene positive by IHC") to emptySet()
            ihcTestEvaluation?.hasPossiblePositiveResultsForItem() == true -> emptySet<String>() to setOf("$gene result indeterminate by IHC")
            else -> emptySet<String>() to emptySet()
        }

        val anyWarns =
            listOf(fusionsWithNoEffect, fusionsWithNoHighDriverLikelihood, unreportableFusionsWithGainOfFunction).any { it.isNotEmpty() }

        return when {
            matchingFusions.isNotEmpty() && !anyWarns -> {
                EvaluationFactory.pass("Fusion(s) ${concatFusions(matchingFusions)} in $gene", inclusionEvents = matchingFusions)
            }

            matchingFusions.isNotEmpty() -> {
                val eventWarningDescriptions = concat(
                    listOf(
                        fusionsWithNoEffect.map { event -> "$event (no protein effect)" },
                        fusionsWithNoHighDriverLikelihood.map { event -> "$event (no high driver likelihood)" },
                        unreportableFusionsWithGainOfFunction.map { event -> "$event (gain-of-function evidence but not considered reportable)" },
                        ihcEventsThatQualify.map { finding -> "$finding (may indicate a fusion)" },
                        ihcEventsThatAreIndeterminate.map { finding -> "$finding (undetermined if this may indicate a fusion)" },
                    ).flatten()
                )

                EvaluationFactory.warn(
                    "Fusion(s) ${concatFusions(matchingFusions)} in $gene together with other fusion events(s): " + eventWarningDescriptions,
                    inclusionEvents = matchingFusions + fusionsWithNoEffect + fusionsWithNoHighDriverLikelihood + unreportableFusionsWithGainOfFunction + ihcEventsThatQualify + ihcEventsThatAreIndeterminate
                )
            }

            else -> {
                val potentialWarnEvaluation = evaluatePotentialWarns(
                    fusionsWithNoEffect,
                    fusionsWithNoHighDriverLikelihood,
                    unreportableFusionsWithGainOfFunction,
                    ihcEventsThatQualify,
                    ihcEventsThatAreIndeterminate,
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
        ihcEventsThatQualify: Set<String>,
        ihcEventsThatAreIndeterminate: Set<String>,
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
                ),
                EventsWithMessages(
                    ihcEventsThatQualify,
                    "$gene IHC result(s) may indicate $gene fusion"
                ),
                EventsWithMessages(
                    ihcEventsThatAreIndeterminate,
                    "$gene IHC result(s) are indeterminate - undetermined if this may indicate $gene fusion"
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