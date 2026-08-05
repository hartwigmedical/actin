package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant

private val NO_PROTEIN_EFFECT_SET = setOf(ProteinEffect.NO_EFFECT, ProteinEffect.NO_EFFECT_PREDICTED)

class GeneIsWildType(override val gene: String, labels: EvaluationLabels.Molecular) :
    MolecularEvaluationFunction(targetCoveragePredicate = atLeast(MolecularTestTarget.MUTATION, labels.geneIsWildTypeMessagePrefix()), labels = labels) {
    
    override fun evaluationPrecedence() = ::evaluationPrecedenceFunction

    private fun evaluationPrecedenceFunction(groupedEvaluationsByResult: Map<EvaluationResult, List<MolecularEvaluation>>) =
        (groupedEvaluationsByResult[EvaluationResult.FAIL]
            ?: groupedEvaluationsByResult[EvaluationResult.WARN]
            ?: groupedEvaluationsByResult[EvaluationResult.PASS]
            ?: groupedEvaluationsByResult[EvaluationResult.UNDETERMINED])

    override fun evaluate(test: MolecularTest): Evaluation {
        val reportableEventsWithEffect: MutableSet<String> = mutableSetOf()
        val reportableEventsWithEffectPotentiallyWildtype: MutableSet<String> = mutableSetOf()
        val reportableEventsWithNoEffect: MutableSet<String> = mutableSetOf()
        val evidenceSource = test.evidenceSource

        val drivers = test.drivers
        sequenceOf(
            drivers.variants.asSequence(),
            drivers.copyNumbers.asSequence(),
            drivers.homozygousDisruptions.asSequence().filter { it.geneRole != GeneRole.ONCO },
            drivers.disruptions.asSequence().filter { it.geneRole != GeneRole.ONCO },
        ).flatten()
            .filter { it.gene == gene && it.isReportable }
            .forEach {
                if (NO_PROTEIN_EFFECT_SET.contains(it.proteinEffect) && (it !is CopyNumber)) {
                    reportableEventsWithNoEffect.add(it.event)
                } else if ((it is Variant && it.driverLikelihood == DriverLikelihood.HIGH)
                    || it is HomozygousDisruption || it is Disruption
                ) {
                    reportableEventsWithEffect.add(it.event)
                } else {
                    reportableEventsWithEffectPotentiallyWildtype.add(it.event)
                }
            }

        for (fusion in drivers.fusions) {
            if ((fusion.geneStart == gene || fusion.geneEnd == gene) && fusion.isReportable) {
                if (NO_PROTEIN_EFFECT_SET.contains(fusion.proteinEffect)) {
                    reportableEventsWithNoEffect.add(fusion.event)
                } else {
                    reportableEventsWithEffect.add(fusion.event)
                }
            }
        }

        val potentialWarnEvaluation =
            evaluatePotentialWarns(reportableEventsWithNoEffect, reportableEventsWithEffectPotentiallyWildtype, evidenceSource)

        return when {
            reportableEventsWithEffect.isNotEmpty() ->
                EvaluationFactory.fail(labels.geneIsWildTypeFail(gene, Format.concat(reportableEventsWithEffect)))

            potentialWarnEvaluation != null -> potentialWarnEvaluation

            test.hasSufficientQualityButLowPurity() ->
                EvaluationFactory.warn(
                    labels.geneIsWildTypeWarnLowPurity(gene),
                    inclusionEvents = setOf("$gene wild-type")
                )

            else -> EvaluationFactory.pass(labels.geneIsWildTypePass(gene), inclusionEvents = setOf("$gene wild-type"))
        }
    }

    private fun evaluatePotentialWarns(
        reportableEventsWithNoEffect: Set<String>,
        reportableEventsWithEffectPotentiallyWildtype: Set<String>,
        evidenceSource: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOf(
                EventsWithMessages(
                    reportableEventsWithNoEffect,
                    labels.geneIsWildTypeWarnNoEffect(Format.concat(reportableEventsWithNoEffect), gene, evidenceSource)
                ),
                EventsWithMessages(
                    reportableEventsWithEffectPotentiallyWildtype,
                    labels.geneIsWildTypeWarnPotentiallyWildtype(Format.concat(reportableEventsWithEffectPotentiallyWildtype), gene)
                ),
            )
        )
    }
}