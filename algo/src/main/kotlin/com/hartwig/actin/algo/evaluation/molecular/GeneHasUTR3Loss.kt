package com.hartwig.actin.algo.evaluation.molecular

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.driver.RegionType
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect

class GeneHasUTR3Loss internal constructor(private val gene: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hotspotsIn3UTR: MutableSet<String> = Sets.newHashSet()
        val hotspotsIn3UTRUnreportable: MutableSet<String> = Sets.newHashSet()
        val vusIn3UTR: MutableSet<String> = Sets.newHashSet()
        for (variant in record.molecular().drivers().variants()) {
            if (variant.gene() == gene && variant.canonicalImpact().effects().contains(VariantEffect.THREE_PRIME_UTR)) {
                if (variant.isHotspot && variant.isReportable) {
                    hotspotsIn3UTR.add(variant.event())
                } else if (variant.isHotspot) {
                    hotspotsIn3UTRUnreportable.add(variant.event())
                } else {
                    vusIn3UTR.add(variant.event())
                }
            }
        }
        val disruptionsIn3UTR: MutableSet<String> = Sets.newHashSet()
        for (disruption in record.molecular().drivers().disruptions()) {
            if (disruption.gene() == gene && disruption.codingContext() == CodingContext.UTR_3P && disruption.regionType() == RegionType.EXONIC) {
                disruptionsIn3UTR.add(disruption.event())
            }
        }
        if (hotspotsIn3UTR.isNotEmpty()) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addAllInclusionMolecularEvents(hotspotsIn3UTR)
                .addPassSpecificMessages(
                    "3' UTR hotspot mutation(s) in " + gene + " should lead to 3' UTR loss: " + concat(hotspotsIn3UTR)
                )
                .addPassGeneralMessages("Present 3' UTR loss of $gene")
                .build()
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(hotspotsIn3UTRUnreportable, vusIn3UTR, disruptionsIn3UTR)
        return potentialWarnEvaluation
            ?: unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No variants detected in 3' UTR region of $gene")
                .addFailGeneralMessages("No 3' UTR loss of $gene")
                .build()
    }

    private fun evaluatePotentialWarns(
        vusIn3UTR: Set<String>, hotspotsIn3UTRUnreportable: Set<String>,
        disruptionsIn3UTR: Set<String>
    ): Evaluation? {
        val warnEvents: MutableSet<String> = Sets.newHashSet()
        val warnSpecificMessages: MutableSet<String> = Sets.newHashSet()
        val warnGeneralMessages: MutableSet<String> = Sets.newHashSet()
        if (hotspotsIn3UTRUnreportable.isNotEmpty()) {
            warnEvents.addAll(hotspotsIn3UTRUnreportable)
            warnSpecificMessages.add(
                "Hotspot mutation(s) detected in 3' UTR region of " + gene + " which may lead to 3' UTR loss: "
                        + concat(hotspotsIn3UTRUnreportable) + " but mutation is not considered reportable"
            )
            warnGeneralMessages.add("Hotspot mutation(s) in 3' UTR region of $gene may lead to 3' UTR loss but mutation is not reportable")
        }
        if (vusIn3UTR.isNotEmpty()) {
            warnEvents.addAll(vusIn3UTR)
            warnSpecificMessages.add(
                "VUS mutation(s) detected in 3' UTR region of " + gene + " which may lead to 3' UTR loss: " + concat(vusIn3UTR)
            )
            warnGeneralMessages.add("VUS mutation(s) in 3' UTR region of $gene may lead to 3' UTR loss")
        }
        if (disruptionsIn3UTR.isNotEmpty()) {
            warnEvents.addAll(disruptionsIn3UTR)
            warnSpecificMessages.add(
                "Disruption(s) detected in 3' UTR region of $gene which may lead to 3' UTR loss: " + concat(
                    disruptionsIn3UTR
                )
            )
            warnGeneralMessages.add("Disruption(s) in 3' UTR region of $gene may lead to 3' UTR loss")
        }
        return if (warnEvents.isNotEmpty() && warnSpecificMessages.isNotEmpty() && warnGeneralMessages.isNotEmpty()) {
            unrecoverable()
                .result(EvaluationResult.WARN)
                .addAllInclusionMolecularEvents(warnEvents)
                .addAllWarnSpecificMessages(warnSpecificMessages)
                .addAllWarnGeneralMessages(warnGeneralMessages)
                .build()
        } else null
    }
}