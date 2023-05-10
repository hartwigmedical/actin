package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.molecular.datamodel.driver.*

class GeneIsInactivated internal constructor(private val gene: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val inactivationEventsThatQualify: MutableSet<String> = mutableSetOf()
        val inactivationEventsThatAreUnreportable: MutableSet<String> = mutableSetOf()
        val inactivationEventsNoTSG: MutableSet<String> = mutableSetOf()
        val inactivationEventsGainOfFunction: MutableSet<String> = mutableSetOf()
        for (homozygousDisruption in record.molecular().drivers().homozygousDisruptions()) {
            if (homozygousDisruption.gene() == gene) {
                val isGainOfFunction = (homozygousDisruption.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                        || homozygousDisruption.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
                if (!homozygousDisruption.isReportable) {
                    inactivationEventsThatAreUnreportable.add(homozygousDisruption.event())
                } else if (homozygousDisruption.geneRole() == GeneRole.ONCO) {
                    inactivationEventsNoTSG.add(homozygousDisruption.event())
                } else if (isGainOfFunction) {
                    inactivationEventsGainOfFunction.add(homozygousDisruption.event())
                } else {
                    inactivationEventsThatQualify.add(homozygousDisruption.event())
                }
            }
        }
        for (copyNumber in record.molecular().drivers().copyNumbers()) {
            if (copyNumber.type() == CopyNumberType.LOSS && copyNumber.gene() == gene) {
                val isGainOfFunction = (copyNumber.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                        || copyNumber.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
                if (!copyNumber.isReportable) {
                    inactivationEventsThatAreUnreportable.add(copyNumber.event())
                } else if (copyNumber.geneRole() == GeneRole.ONCO) {
                    inactivationEventsNoTSG.add(copyNumber.event())
                } else if (isGainOfFunction) {
                    inactivationEventsGainOfFunction.add(copyNumber.event())
                } else {
                    inactivationEventsThatQualify.add(copyNumber.event())
                }
            }
        }
        val reportableNonDriverBiallelicVariantsOther: MutableSet<String> = mutableSetOf()
        val reportableNonDriverNonBiallelicVariantsOther: MutableSet<String> = mutableSetOf()
        val inactivationHighDriverNonBiallelicVariants: MutableSet<String> = mutableSetOf()
        val inactivationSubclonalVariants: MutableSet<String> = mutableSetOf()
        val eventsThatMayBeTransPhased: MutableList<String> = mutableListOf()
        val evaluatedPhaseGroups: MutableSet<Int?> = mutableSetOf()
        val hasHighMutationalLoad = record.molecular().characteristics().hasHighTumorMutationalLoad()
        for (variant in record.molecular().drivers().variants()) {
            if (variant.gene() == gene && INACTIVATING_CODING_EFFECTS.contains(variant.canonicalImpact().codingEffect())) {
                if (!variant.isReportable) {
                    inactivationEventsThatAreUnreportable.add(variant.event())
                } else {
                    val phaseGroups: Set<Int>? = variant.phaseGroups()
                    if (phaseGroups != null) {
                        if (phaseGroups.none { evaluatedPhaseGroups.contains(it) }) {
                            eventsThatMayBeTransPhased.add(variant.event())
                        }
                        evaluatedPhaseGroups.addAll(phaseGroups)
                    } else {
                        eventsThatMayBeTransPhased.add(variant.event())
                    }

                    if (variant.driverLikelihood() == DriverLikelihood.HIGH) {
                        val isGainOfFunction = (variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                                || variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
                        if (variant.geneRole() == GeneRole.ONCO) {
                            inactivationEventsNoTSG.add(variant.event())
                        } else if (!variant.isBiallelic) {
                            inactivationHighDriverNonBiallelicVariants.add(variant.event())
                        } else if (isGainOfFunction) {
                            inactivationEventsGainOfFunction.add(variant.event())
                        } else if (variant.clonalLikelihood() < CLONAL_CUTOFF) {
                            inactivationSubclonalVariants.add(variant.event())
                        } else {
                            inactivationEventsThatQualify.add(variant.event())
                        }
                    } else if (hasHighMutationalLoad == null || !hasHighMutationalLoad) {
                        if (variant.isBiallelic) {
                            reportableNonDriverBiallelicVariantsOther.add(variant.event())
                        } else {
                            reportableNonDriverNonBiallelicVariantsOther.add(variant.event())
                        }
                    }
                }
            }
        }

        val evaluatedClusterGroups: MutableSet<Int> = mutableSetOf()
        for (disruption in record.molecular().drivers().disruptions()) {
            if (disruption.gene() == gene && disruption.isReportable) {
                if (!evaluatedClusterGroups.contains(disruption.clusterGroup())) {
                    evaluatedClusterGroups.add(disruption.clusterGroup())
                    eventsThatMayBeTransPhased.add(disruption.event())
                }
            }
        }

        if (inactivationEventsThatQualify.isNotEmpty()) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addAllInclusionMolecularEvents(inactivationEventsThatQualify)
                .addPassSpecificMessages(
                    "Inactivation event(s) detected for gene " + gene + ": " + concat(inactivationEventsThatQualify)
                )
                .addPassGeneralMessages("$gene inactivation")
                .build()
        }

        val potentialWarnEvaluation = evaluatePotentialWarns(
            inactivationEventsThatAreUnreportable,
            inactivationEventsNoTSG,
            inactivationEventsGainOfFunction,
            inactivationHighDriverNonBiallelicVariants,
            inactivationSubclonalVariants,
            reportableNonDriverBiallelicVariantsOther,
            reportableNonDriverNonBiallelicVariantsOther,
            eventsThatMayBeTransPhased
        )

        return potentialWarnEvaluation
            ?: unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No inactivation event(s) detected for gene $gene")
                .addFailGeneralMessages("No $gene inactivation")
                .build()
    }

    private fun evaluatePotentialWarns(
        inactivationEventsThatAreUnreportable: Set<String>,
        inactivationEventsNoTSG: Set<String>, inactivationEventsGainOfFunction: Set<String>,
        inactivationHighDriverNonBiallelicVariants: Set<String>, inactivationSubclonalVariants: Set<String>,
        reportableNonDriverBiallelicVariantsOther: Set<String>,
        reportableNonDriverNonBiallelicVariantsOther: Set<String>, eventsThatMayBeTransPhased: List<String>
    ): Evaluation? {
        val warnEvents: MutableSet<String> = mutableSetOf()
        val warnSpecificMessages: MutableSet<String> = mutableSetOf()
        val warnGeneralMessages: MutableSet<String> = mutableSetOf()
        if (inactivationEventsThatAreUnreportable.isNotEmpty()) {
            warnEvents.addAll(inactivationEventsThatAreUnreportable)
            warnSpecificMessages.add(
                "Inactivation events detected for gene " + gene + ": " + concat(inactivationEventsThatAreUnreportable)
                        + ", but considered non-reportable"
            )
            warnGeneralMessages.add("$gene potential inactivation but not reportable")
        }
        if (inactivationEventsNoTSG.isNotEmpty()) {
            warnEvents.addAll(inactivationEventsNoTSG)
            warnSpecificMessages.add(
                "Inactivation events detected for gene " + gene + ": " + concat(inactivationEventsNoTSG)
                        + " but gene is annotated with gene role ONCO"
            )
            warnGeneralMessages.add("$gene potential inactivation but gene role ONCO")
        }
        if (inactivationEventsGainOfFunction.isNotEmpty()) {
            warnEvents.addAll(inactivationEventsGainOfFunction)
            warnSpecificMessages.add(
                "Inactivation events detected for " + gene + ": " + concat(inactivationEventsGainOfFunction)
                        + " but no events annotated as having gain-of-function impact"
            )
            warnGeneralMessages.add("$gene potential inactivation but event annotated with gain-of-function protein impact")
        }
        if (inactivationHighDriverNonBiallelicVariants.isNotEmpty() && eventsThatMayBeTransPhased.size <= 1) {
            warnEvents.addAll(inactivationHighDriverNonBiallelicVariants)
            warnSpecificMessages.add(
                "Inactivation event(s) detected for " + gene + ": " + concat(inactivationHighDriverNonBiallelicVariants)
                        + " but event(s) are not biallelic"
            )
            warnGeneralMessages.add("$gene potential inactivation but not biallelic")
        }
        if (inactivationSubclonalVariants.isNotEmpty()) {
            warnEvents.addAll(inactivationSubclonalVariants)
            warnSpecificMessages.add(
                "Inactivation event(s) detected for " + gene + ": " + concat(inactivationSubclonalVariants)
                        + " but subclonal likelihood > " + percentage(1 - CLONAL_CUTOFF)
            )
            warnGeneralMessages.add(
                gene + " potentially inactivating event(s) " + concat(inactivationSubclonalVariants)
                        + " but subclonal likelihood > " + percentage(1 - CLONAL_CUTOFF)
            )
        }
        if (reportableNonDriverBiallelicVariantsOther.isNotEmpty()) {
            warnEvents.addAll(reportableNonDriverBiallelicVariantsOther)
            warnSpecificMessages.add(
                "Potential inactivation events detected for " + gene + ": " + concat(reportableNonDriverBiallelicVariantsOther)
                        + " but event(s) are not of high driver likelihood"
            )
            warnGeneralMessages.add("Variant(s) " + concat(reportableNonDriverBiallelicVariantsOther) + " of low driver likelihood")
        }
        if (reportableNonDriverNonBiallelicVariantsOther.isNotEmpty()) {
            warnEvents.addAll(reportableNonDriverNonBiallelicVariantsOther)
            warnSpecificMessages.add(
                "Potential inactivation events detected for $gene: " + concat(
                    reportableNonDriverNonBiallelicVariantsOther
                ) + " but event(s) are not biallelic and not of high driver likelihood"
            )
            warnGeneralMessages.add(
                "Variant(s) " + concat(reportableNonDriverNonBiallelicVariantsOther)
                        + " not biallelic and no high driver likelihood"
            )
        }
        if (eventsThatMayBeTransPhased.size > 1) {
            warnEvents.addAll(eventsThatMayBeTransPhased)
            warnSpecificMessages.add(
                "Multiple events detected for " + gene + ": " + concat(eventsThatMayBeTransPhased)
                        + " that potentially together inactivate the gene"
            )
            warnGeneralMessages.add("$gene potential inactivation if considering multiple events")
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

    companion object {
        private const val CLONAL_CUTOFF = 0.5
        val INACTIVATING_CODING_EFFECTS: MutableSet<CodingEffect?> = mutableSetOf()

        init {
            INACTIVATING_CODING_EFFECTS.add(CodingEffect.NONSENSE_OR_FRAMESHIFT)
            INACTIVATING_CODING_EFFECTS.add(CodingEffect.MISSENSE)
            INACTIVATING_CODING_EFFECTS.add(CodingEffect.SPLICE)
        }
    }
}