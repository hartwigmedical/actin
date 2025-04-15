package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import java.time.LocalDate

class GeneIsWildType(private val gene: String, maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge) {

    override fun genes() = listOf(gene)
    override fun targets() = listOf(MolecularTestTarget.MUTATION)

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
                EvaluationFactory.fail("$gene not wild-type due to ${Format.concat(reportableEventsWithEffect)}")

            potentialWarnEvaluation != null -> potentialWarnEvaluation

            test.hasSufficientQualityButLowPurity() ->
                EvaluationFactory.warn(
                    "$gene is wild-type although tumor purity is low",
                    inclusionEvents = setOf("$gene wild-type")
                )

            else -> EvaluationFactory.pass("$gene is wild-type", inclusionEvents = setOf("$gene wild-type"))
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
                    "Reportable event(s) ${Format.concat(reportableEventsWithNoEffect)} in $gene - however these are annotated"
                            + " with protein effect 'no effect' in $evidenceSource and thus may potentially be considered wild-type"
                ),
                EventsWithMessages(
                    reportableEventsWithEffectPotentiallyWildtype,
                    "Reportable event(s) ${Format.concat(reportableEventsWithEffectPotentiallyWildtype)} in $gene"
                            + " which may potentially be considered wild-type"
                ),
            )
        )
    }
}