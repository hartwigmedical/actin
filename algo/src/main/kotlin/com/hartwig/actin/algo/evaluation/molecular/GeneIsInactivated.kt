package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.wgs.driver.CopyNumberType

class GeneIsInactivated(private val gene: String) : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val inactivationEventsThatQualify: MutableSet<String> = mutableSetOf()
        val inactivationEventsThatAreUnreportable: MutableSet<String> = mutableSetOf()
        val inactivationEventsNoTSG: MutableSet<String> = mutableSetOf()
        val inactivationEventsGainOfFunction: MutableSet<String> = mutableSetOf()
        val evidenceSource = molecular.evidenceSource

        val drivers = molecular.drivers
        sequenceOf(
            drivers.homozygousDisruptions.asSequence(),
            drivers.copyNumbers.asSequence().filter { it.type == CopyNumberType.LOSS }
        ).flatten()
            .filter { it.gene == gene }
            .forEach { geneAlterationDriver ->
                val isGainOfFunction = (geneAlterationDriver.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION
                        || geneAlterationDriver.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
                if (!geneAlterationDriver.isReportable) {
                    inactivationEventsThatAreUnreportable.add(geneAlterationDriver.event)
                } else if (geneAlterationDriver.geneRole == GeneRole.ONCO) {
                    inactivationEventsNoTSG.add(geneAlterationDriver.event)
                } else if (isGainOfFunction) {
                    inactivationEventsGainOfFunction.add(geneAlterationDriver.event)
                } else {
                    inactivationEventsThatQualify.add(geneAlterationDriver.event)
                }
            }

        val reportableNonDriverBiallelicVariantsOther: MutableSet<String> = mutableSetOf()
        val reportableNonDriverNonBiallelicVariantsOther: MutableSet<String> = mutableSetOf()
        val inactivationHighDriverNonBiallelicVariants: MutableSet<String> = mutableSetOf()
        val inactivationSubclonalVariants: MutableSet<String> = mutableSetOf()
        val eventsThatMayBeTransPhased: MutableList<String> = mutableListOf()
        val evaluatedPhaseGroups: MutableSet<Int?> = mutableSetOf()
        val hasHighMutationalLoad = molecular.characteristics.hasHighTumorMutationalLoad
        for (variant in drivers.variants) {
            if (variant.gene == gene && INACTIVATING_CODING_EFFECTS.contains(variant.canonicalImpact.codingEffect)) {
                if (!variant.isReportable) {
                    inactivationEventsThatAreUnreportable.add(variant.event)
                } else {
                    val phaseGroups: Set<Int>? = variant.phaseGroups
                    if (phaseGroups != null) {
                        if (phaseGroups.none { evaluatedPhaseGroups.contains(it) }) {
                            eventsThatMayBeTransPhased.add(variant.event)
                        }
                        evaluatedPhaseGroups.addAll(phaseGroups)
                    } else {
                        eventsThatMayBeTransPhased.add(variant.event)
                    }

                    if (variant.driverLikelihood == DriverLikelihood.HIGH) {
                        val isGainOfFunction = (variant.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION
                                || variant.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
                        if (variant.geneRole == GeneRole.ONCO) {
                            inactivationEventsNoTSG.add(variant.event)
                        } else if (!variant.isBiallelic) {
                            inactivationHighDriverNonBiallelicVariants.add(variant.event)
                        } else if (isGainOfFunction) {
                            inactivationEventsGainOfFunction.add(variant.event)
                        } else if (variant.clonalLikelihood < CLONAL_CUTOFF) {
                            inactivationSubclonalVariants.add(variant.event)
                        } else {
                            inactivationEventsThatQualify.add(variant.event)
                        }
                    } else if (hasHighMutationalLoad == null || !hasHighMutationalLoad) {
                        if (variant.isBiallelic) {
                            reportableNonDriverBiallelicVariantsOther.add(variant.event)
                        } else {
                            reportableNonDriverNonBiallelicVariantsOther.add(variant.event)
                        }
                    }
                }
            }
        }

        val evaluatedClusterGroups: MutableSet<Int> = mutableSetOf()
        for (disruption in drivers.disruptions) {
            if (disruption.gene == gene && disruption.isReportable) {
                if (!evaluatedClusterGroups.contains(disruption.clusterGroup)) {
                    evaluatedClusterGroups.add(disruption.clusterGroup)
                    eventsThatMayBeTransPhased.add(disruption.event)
                }
            }
        }

        if (inactivationEventsThatQualify.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Inactivation event(s) detected for gene " + gene + ": " + concat(inactivationEventsThatQualify),
                "$gene inactivation",
                inclusionEvents = inactivationEventsThatQualify
            )
        }

        val potentialWarnEvaluation = evaluatePotentialWarns(
            inactivationEventsThatAreUnreportable,
            inactivationEventsNoTSG,
            inactivationEventsGainOfFunction,
            inactivationHighDriverNonBiallelicVariants,
            inactivationSubclonalVariants,
            reportableNonDriverBiallelicVariantsOther,
            reportableNonDriverNonBiallelicVariantsOther,
            eventsThatMayBeTransPhased,
            evidenceSource
        )

        return potentialWarnEvaluation
            ?: EvaluationFactory.fail("No inactivation event(s) detected for gene $gene", "No $gene inactivation")
    }

    private fun evaluatePotentialWarns(
        inactivationEventsThatAreUnreportable: Set<String>,
        inactivationEventsNoTSG: Set<String>, inactivationEventsGainOfFunction: Set<String>,
        inactivationHighDriverNonBiallelicVariants: Set<String>, inactivationSubclonalVariants: Set<String>,
        reportableNonDriverBiallelicVariantsOther: Set<String>,
        reportableNonDriverNonBiallelicVariantsOther: Set<String>, eventsThatMayBeTransPhased: List<String>, evidenceSource: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOfNotNull(
                EventsWithMessages(
                    inactivationEventsThatAreUnreportable,
                    "Inactivation event(s) detected for gene $gene: ${concat(inactivationEventsThatAreUnreportable)}, but considered non-reportable",
                    "Inactivation event(s) for $gene but event(s) not reportable"
                ),
                EventsWithMessages(
                    inactivationEventsNoTSG,
                    "Inactivation event(s) detected for gene $gene: ${concat(inactivationEventsNoTSG)}"
                            + " but gene is annotated with gene role oncogene in $evidenceSource",
                    "Inactivation event(s) for $gene but gene is oncogene in $evidenceSource"
                ),
                EventsWithMessages(
                    inactivationEventsGainOfFunction,
                    "Inactivation event(s) detected for $gene: ${concat(inactivationEventsGainOfFunction)}"
                            + " but no events annotated as having gain-of-function impact in $evidenceSource",
                    "Inactivation event(s) for $gene but event(s) annotated with gain-of-function protein impact evidence in $evidenceSource"
                ),
                if (inactivationHighDriverNonBiallelicVariants.isNotEmpty() && eventsThatMayBeTransPhased.size <= 1) {
                    EventsWithMessages(
                        inactivationHighDriverNonBiallelicVariants,
                        "Inactivation event(s) detected for $gene: ${concat(inactivationHighDriverNonBiallelicVariants)} but event(s) are not biallelic",
                        "Inactivation event(s) for $gene but event(s) are not biallelic"
                    )
                } else null,
                EventsWithMessages(
                    inactivationSubclonalVariants,
                    "Inactivation event(s) detected for $gene: ${concat(inactivationSubclonalVariants)} but subclonal likelihood > "
                            + percentage(1 - CLONAL_CUTOFF),
                    "Inactivation event(s) detected for $gene: ${concat(inactivationSubclonalVariants)} but subclonal likelihood > "
                            + percentage(1 - CLONAL_CUTOFF)
                ),
                EventsWithMessages(
                    reportableNonDriverBiallelicVariantsOther,
                    "Potential inactivation event(s) detected for $gene: ${concat(reportableNonDriverBiallelicVariantsOther)}"
                            + " but event(s) are not of high driver likelihood",
                    "Potential inactivation event(s) " + concat(reportableNonDriverBiallelicVariantsOther) + " but no high driver likelihood"
                ),
                EventsWithMessages(
                    reportableNonDriverNonBiallelicVariantsOther,
                    "Potential inactivation event(s) detected for $gene: ${concat(reportableNonDriverNonBiallelicVariantsOther)}"
                            + " but event(s) are not biallelic and not of high driver likelihood",
                    "Potential inactivation event(s) " + concat(reportableNonDriverNonBiallelicVariantsOther)
                            + " but not biallelic and no high driver likelihood"
                ),
                if (eventsThatMayBeTransPhased.size > 1) {
                    EventsWithMessages(
                        eventsThatMayBeTransPhased.toSet(),
                        "Multiple events detected for $gene: ${concat(eventsThatMayBeTransPhased)} that potentially together inactivate the gene",
                        "$gene potential inactivation if considering multiple events"
                    )
                } else null
            )
        )
    }

    companion object {
        private const val CLONAL_CUTOFF = 0.5
        val INACTIVATING_CODING_EFFECTS = setOf(CodingEffect.NONSENSE_OR_FRAMESHIFT, CodingEffect.MISSENSE, CodingEffect.SPLICE)
    }
}