package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.orange.driver.Disruption
import com.hartwig.actin.molecular.datamodel.orange.driver.HomozygousDisruption

class GeneIsWildType internal constructor(private val gene: String) : MolecularEvaluationFunction {

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
        val evaluation = if (reportableEventsWithEffect.isNotEmpty()) {
            EvaluationFactory.fail(
                "Gene $gene is not considered wild-type due to ${Format.concat(reportableEventsWithEffect)}",
                "$gene not wild-type"
            )
        } else {
            evaluatePotentialWarns(reportableEventsWithNoEffect, reportableEventsWithEffectPotentiallyWildtype, evidenceSource)
                ?: EvaluationFactory.pass(
                    "Gene $gene is considered wild-type", "$gene is wild-type", inclusionEvents = setOf("$gene wild-type")
                )
        }
        return evaluation
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