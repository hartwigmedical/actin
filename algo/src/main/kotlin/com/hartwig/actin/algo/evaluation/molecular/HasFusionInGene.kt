package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.hmf.driver.FusionDriverType

class HasFusionInGene(private val gene: String) : MolecularEvaluationFunction {

    override fun evaluate(molecularHistory: MolecularHistory): Evaluation {

        val orangeMolecular = molecularHistory.latestOrangeMolecularRecord()
        val orangeMolecularEvaluation = if (orangeMolecular != null) findMatchingFusionsInOrangeMolecular(orangeMolecular) else null
        val panelEvaluation = findMatchingFusionsInPanels(molecularHistory)

        val groupedEvaluationsByResult = listOfNotNull(orangeMolecularEvaluation, panelEvaluation)
            .groupBy { evaluation -> evaluation.result }
            .mapValues { entry ->
                entry.value.reduce { acc, y -> acc.addMessagesAndEvents(y) }
            }
        return groupedEvaluationsByResult[EvaluationResult.PASS]
            ?: groupedEvaluationsByResult[EvaluationResult.WARN]
            ?: groupedEvaluationsByResult[EvaluationResult.FAIL]
            ?: EvaluationFactory.undetermined("Gene $gene not tested in molecular data", "Gene $gene not tested")
    }

    private fun findMatchingFusionsInOrangeMolecular(molecular: MolecularRecord): Evaluation {
        val matchingFusions: MutableSet<String> = mutableSetOf()
        val fusionsWithNoEffect: MutableSet<String> = mutableSetOf()
        val fusionsWithNoHighDriverLikelihoodWithGainOfFunction: MutableSet<String> = mutableSetOf()
        val fusionsWithNoHighDriverLikelihoodOther: MutableSet<String> = mutableSetOf()
        val unreportableFusionsWithGainOfFunction: MutableSet<String> = mutableSetOf()
        val evidenceSource = molecular.evidenceSource

        for (fusion in molecular.drivers.fusions) {
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

        return potentialWarnEvaluation ?: EvaluationFactory.fail("No fusion detected with gene $gene", "No fusion in gene $gene")
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

    private fun findMatchingFusionsInPanels(molecularHistory: MolecularHistory): Evaluation? {
        val matchedFusions = molecularHistory.allPanels()
            .flatMap { it.events() }
            .filter { it.impactsGene(gene) }
            .map { it.display() }
            .toSet()

        if (matchedFusions.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Fusion(s) ${concat(matchedFusions)} detected in gene $gene in panel(s)",
                "Fusion(s) detected in gene $gene",
                inclusionEvents = matchedFusions
            )
        }

        val isGeneTested = molecularHistory.allGenericPanels().any { gene in it.testedGenes() }
        return if (isGeneTested) {
            EvaluationFactory.fail("No fusion detected with gene $gene in panel(s)", "No fusion in gene $gene")
        } else {
            null
        }
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