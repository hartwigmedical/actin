package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import java.time.LocalDate

class GeneIsWildType(private val gene: String, maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge) {

    override fun genes() = listOf(gene)

    override fun evaluationPrecedence() = ::evaluationPrecedenceFunction

    private fun evaluationPrecedenceFunction(groupedEvaluationsByResult: Map<EvaluationResult, List<MolecularEvaluation>>) =
        (groupedEvaluationsByResult[EvaluationResult.FAIL]
            ?: groupedEvaluationsByResult[EvaluationResult.PASS]
            ?: groupedEvaluationsByResult[EvaluationResult.WARN]
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
                if (it.proteinEffect == ProteinEffect.NO_EFFECT || it.proteinEffect == ProteinEffect.NO_EFFECT_PREDICTED) {
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
                val hasNoEffect =
                    fusion.proteinEffect == ProteinEffect.NO_EFFECT || fusion.proteinEffect == ProteinEffect.NO_EFFECT_PREDICTED
                if (hasNoEffect) {
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
                EvaluationFactory.fail(
                    "Gene $gene is not considered wild-type due to ${Format.concat(reportableEventsWithEffect)}",
                    "$gene not wild-type"
                )

            potentialWarnEvaluation != null -> potentialWarnEvaluation

            test.hasSufficientQualityButLowPurity() ->
                EvaluationFactory.warn(
                    "Gene $gene is considered wild-type although tumor purity is low",
                    "$gene is wild-type although tumor purity is low",
                    inclusionEvents = setOf("$gene wild-type")
                )

            else ->
                EvaluationFactory.pass(
                    "Gene $gene is considered wild-type", "$gene is wild-type", inclusionEvents = setOf("$gene wild-type")
                )
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