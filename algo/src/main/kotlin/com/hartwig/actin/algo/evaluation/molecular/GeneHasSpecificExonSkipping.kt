package com.hartwig.actin.algo.evaluation.molecular

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect

class GeneHasSpecificExonSkipping internal constructor(private val gene: String, private val exonToSkip: Int) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val fusionSkippingEvents: MutableSet<String> = Sets.newHashSet()
        for (fusion in record.molecular().drivers().fusions()) {
            if (fusion.isReportable && fusion.geneStart() == gene && fusion.geneEnd() == gene && fusion.fusedExonUp() == exonToSkip - 1 && fusion.fusedExonDown() == exonToSkip + 1) {
                fusionSkippingEvents.add(fusion.event())
            }
        }
        val exonSplicingVariants: MutableSet<String> = Sets.newHashSet()
        for (variant in record.molecular().drivers().variants()) {
            val isCanonicalSplice =
                variant.canonicalImpact().codingEffect() == CodingEffect.SPLICE || variant.canonicalImpact().isSpliceRegion
            val canonicalExonAffected = variant.canonicalImpact().affectedExon()
            val isCanonicalExonAffected = canonicalExonAffected != null && canonicalExonAffected == exonToSkip
            if (variant.isReportable && variant.gene() == gene && isCanonicalExonAffected && isCanonicalSplice) {
                exonSplicingVariants.add(variant.event())
            }
        }
        if (!fusionSkippingEvents.isEmpty()) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addAllInclusionMolecularEvents(fusionSkippingEvents)
                .addPassSpecificMessages(
                    "Exon " + exonToSkip + " skipped in gene " + gene + " due to " + concat(fusionSkippingEvents)
                )
                .addPassGeneralMessages("Present $gene exon $exonToSkip skipping")
                .build()
        }
        return if (!exonSplicingVariants.isEmpty()) {
            unrecoverable()
                .result(EvaluationResult.WARN)
                .addAllInclusionMolecularEvents(exonSplicingVariants)
                .addWarnSpecificMessages(
                    "Exon " + exonToSkip + " may be skipped in gene " + gene + " due to " + concat(exonSplicingVariants)
                )
                .addWarnGeneralMessages("Potential $gene exon $exonToSkip skipping")
                .build()
        } else unrecoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("No $gene exon $exonToSkip skipping")
            .addFailGeneralMessages("No $gene exon $exonToSkip skipping")
            .build()
    }
}