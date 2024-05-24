package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.wgs.driver.Disruption
import com.hartwig.actin.molecular.datamodel.wgs.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.wgs.driver.WgsVariant

class GeneIsWildType internal constructor(private val gene: String) : MolecularEvaluationFunction {

    override fun evaluate(molecularHistory: MolecularHistory): Evaluation {
        val orangeMolecular = molecularHistory.latestOrangeMolecularRecord()
        val orangeMolecularEvaluation = orangeMolecular?.let { evaluateInOrangeMolecular(it) }

        val panelEvaluation = evaluateInPanels(molecularHistory)

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

    private fun evaluateInOrangeMolecular(molecular: MolecularRecord): Evaluation {
        val reportableEventsWithEffect: MutableSet<String> = mutableSetOf()
        val reportableEventsWithEffectPotentiallyWildtype: MutableSet<String> = mutableSetOf()
        val reportableEventsWithNoEffect: MutableSet<String> = mutableSetOf()
        val evidenceSource = molecular.evidenceSource

        val drivers = molecular.drivers
        sequenceOf(
            drivers.variants.asSequence(),
            drivers.copyNumbers.asSequence(),
            drivers.homozygousDisruptions.asSequence().filter { it.geneRole != GeneRole.ONCO },
            drivers.disruptions.asSequence().filter { it.geneRole != GeneRole.ONCO },
        ).flatten()
            .filter { it.gene == gene && it.isReportable }
            .forEach {
                if (it.proteinEffect == ProteinEffect.NO_EFFECT || it.proteinEffect == ProteinEffect.NO_EFFECT_PREDICTED) {
                    reportableEventsWithNoEffect.add(it.event)
                } else if ((it is WgsVariant && it.driverLikelihood == DriverLikelihood.HIGH)
                    || it is HomozygousDisruption || it is Disruption
                ) {
                    reportableEventsWithEffect.add(it.event)
                } else {
                    reportableEventsWithEffectPotentiallyWildtype.add(it.event)
                }
            }

        for (fusion in drivers.fusions) {
            if ((fusion.geneStart == gene || fusion.geneEnd == gene) && fusion.isReportable) {
                val hasNoEffect =
                    fusion.proteinEffect == ProteinEffect.NO_EFFECT || fusion.proteinEffect == ProteinEffect.NO_EFFECT_PREDICTED
                if (hasNoEffect) {
                    reportableEventsWithNoEffect.add(fusion.event)
                } else {
                    reportableEventsWithEffect.add(fusion.event)
                }
            }
        }
        if (reportableEventsWithEffect.isNotEmpty()) {
            return EvaluationFactory.fail(
                "Gene $gene is not considered wild-type due to ${Format.concat(reportableEventsWithEffect)}",
                "$gene not wild-type"
            )
        }
        val potentialWarnEvaluation =
            evaluatePotentialWarns(reportableEventsWithNoEffect, reportableEventsWithEffectPotentiallyWildtype, evidenceSource)
        return potentialWarnEvaluation
            ?: EvaluationFactory.pass(
                "Gene $gene is considered wild-type", "$gene is wild-type", inclusionEvents = setOf("$gene wild-type")
            )
    }

    private fun evaluateInPanels(molecularHistory: MolecularHistory): Evaluation {

        val allPanels = molecularHistory.allPanels()
        val isTestedInAnyPanel = allPanels.any { panel -> panel.isGeneTested(gene) }
        val events = allPanels.flatMap { it.events() }.filter { it.impactsGene(gene) }.toSet()
        val hasResultInAnyPanel = events.isNotEmpty()

        return if (!isTestedInAnyPanel) {
            EvaluationFactory.undetermined("Gene $gene is not tested in panel", "$gene not tested")
        } else if (!hasResultInAnyPanel) {
            EvaluationFactory.pass("Gene $gene is considered wild-type", "$gene is wild-type", inclusionEvents = setOf("$gene wild-type"))
        } else {
            EvaluationFactory.fail(
                "Gene $gene is not considered wild-type due to ${Format.concatItems(events)}",
                "$gene not wild-type"
            )
        }
    }

    private fun evaluatePotentialWarns(
        reportableEventsWithNoEffect: Set<String>,
        reportableEventsWithEffectPotentiallyWildtype: Set<String>, evidenceSource: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    reportableEventsWithNoEffect,
                    "Reportable event(s) in $gene are detected: ${Format.concat(reportableEventsWithNoEffect)}, however these are annotated"
                            + " with protein effect 'no effect' in $evidenceSource and thus may potentially be considered wild-type",
                    "$gene potentially wild-type: event(s) are reportable but protein effect 'no effect' in $evidenceSource"
                ),
                EventsWithMessages(
                    reportableEventsWithEffectPotentiallyWildtype,
                    "Reportable event(s) in $gene are detected: ${Format.concat(reportableEventsWithEffectPotentiallyWildtype)}"
                            + " which may potentially be considered wild-type",
                    "$gene potentially wild-type but event(s) are reportable and have a protein effect in $evidenceSource"
                ),
            )
        )
    }
}