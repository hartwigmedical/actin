package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.filter.MolecularTestFilter
import java.time.LocalDate

private val IHC_FUSION_GENES = setOf("ALK", "ROS1")

class HasFusionInGene(private val gene: String, maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge) {
    private val molecularTestFilter = MolecularTestFilter(maxTestAge)
    override fun genes() = listOf(gene)

    override fun evaluate(record: PatientRecord): Evaluation {
        val recentMolecularTests = molecularTestFilter.apply(record.molecularHistory.molecularTests)

        if (gene !in IHC_FUSION_GENES) {
            if (recentMolecularTests.isEmpty()) {
                return EvaluationFactory.undetermined(
                    "No molecular data",
                    "No molecular data"
                )
            }

            if (recentMolecularTests.none { it.testsGene(gene) }) {
                return EvaluationFactory.undetermined(
                    "Gene $gene not tested in molecular data",
                    "Gene $gene not tested"
                )
            }
        }

        val molecularEvaluations =
            recentMolecularTests.map { MolecularEvaluation(it, evaluateMolecularTest(it)) }

        return molecularEvaluations
            .takeIf { it.isNotEmpty() }
            ?.let { MolecularEvaluation.combine(it) }
            ?: evaluateIHC(record.priorIHCTests)
            ?: EvaluationFactory.undetermined(
                "Insufficient molecular data",
                "Insufficient molecular data"
            )
    }

    private fun evaluateIHC(priorIHCTests: List<PriorIHCTest>): Evaluation? {
        if (gene !in IHC_FUSION_GENES) {
            return null
        }

        val positiveIHC = priorIHCTests.any { it.test == "IHC" && it.item == gene && it.scoreText == "Positive" }
        val negativeIHC = priorIHCTests.any() { it.test == "IHC" && it.item == gene && it.scoreText == "Negative" }

        return when (positiveIHC to negativeIHC) {
            true to false -> EvaluationFactory.pass(
                "Fusion(s) detected from IHC in gene $gene",
                "Fusion(s) detected in gene $gene",
                inclusionEvents = setOf("$gene Fusion IHC Positive")
            )

            false to true -> EvaluationFactory.fail(
                "No fusion detected from IHC in gene $gene",
                "No fusion in gene $gene"
            )

            true to true -> EvaluationFactory.warn(
                "Conflicting fusion evidence from IHC for $gene ",
                "Conflicting fusion for $gene",
                inclusionEvents = setOf("$gene Fusion IHC Positive", "$gene Fusion IHC Negative")
            )

            else -> null
        }
    }

    private fun evaluateMolecularTest(test: MolecularTest): Evaluation {
        val matchingFusions: MutableSet<String> = mutableSetOf()
        val fusionsWithNoEffect: MutableSet<String> = mutableSetOf()
        val fusionsWithNoHighDriverLikelihoodWithGainOfFunction: MutableSet<String> = mutableSetOf()
        val fusionsWithNoHighDriverLikelihoodOther: MutableSet<String> = mutableSetOf()
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
                        if (isGainOfFunction) {
                            fusionsWithNoHighDriverLikelihoodWithGainOfFunction.add(fusion.event)
                        } else {
                            fusionsWithNoHighDriverLikelihoodOther.add(fusion.event)
                        }
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

        if (matchingFusions.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Fusion(s) ${concat(matchingFusions)} detected in gene $gene",
                "Fusion(s) detected in gene $gene",
                inclusionEvents = matchingFusions
            )
        }

        val potentialWarnEvaluation = evaluatePotentialWarns(
            fusionsWithNoEffect,
            fusionsWithNoHighDriverLikelihoodWithGainOfFunction,
            fusionsWithNoHighDriverLikelihoodOther,
            unreportableFusionsWithGainOfFunction,
            evidenceSource
        )

        return potentialWarnEvaluation ?: EvaluationFactory.fail(
            "No fusion detected with gene $gene",
            "No fusion in gene $gene"
        )
    }

    private fun evaluatePotentialWarns(
        fusionsWithNoEffect: Set<String>,
        fusionsWithNoHighDriverLikelihoodWithGainOfFunction: Set<String>,
        fusionsWithNoHighDriverLikelihoodOther: Set<String>,
        unreportableFusionsWithGainOfFunction: Set<String>,
        evidenceSource: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    fusionsWithNoEffect,
                    "Fusion(s) ${concat(fusionsWithNoEffect)} detected in gene $gene but annotated with having no protein effect evidence in $evidenceSource",
                    "Fusion(s) detected in $gene but annotated with having no protein effect evidence in $evidenceSource"
                ),
                EventsWithMessages(
                    fusionsWithNoHighDriverLikelihoodWithGainOfFunction,
                    "Fusion(s) ${concat(fusionsWithNoHighDriverLikelihoodWithGainOfFunction)} detected in gene $gene"
                            + " without high driver likelihood but annotated with having gain-of-function evidence in $evidenceSource",
                    "Fusion(s) detected in gene $gene without high driver likelihood "
                            + "but annotated with having gain-of-function evidence in $evidenceSource"
                ),
                EventsWithMessages(
                    fusionsWithNoHighDriverLikelihoodOther,
                    "Fusion(s) ${concat(fusionsWithNoHighDriverLikelihoodOther)} detected in gene $gene but not with high driver likelihood",
                    "Fusion(s) detected in gene $gene but no high driver likelihood"
                ),
                EventsWithMessages(
                    unreportableFusionsWithGainOfFunction,
                    "Fusion(s) ${concat(unreportableFusionsWithGainOfFunction)} detected in gene $gene"
                            + " but not considered reportable; however fusion is annotated with having gain-of-function evidence in $evidenceSource",
                    "No reportable fusion(s) detected in gene $gene but annotated with having gain-of-function evidence in $evidenceSource"
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