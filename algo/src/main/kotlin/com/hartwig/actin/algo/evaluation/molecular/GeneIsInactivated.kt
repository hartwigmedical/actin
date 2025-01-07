package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concatWithCommaAndAnd
import com.hartwig.actin.algo.evaluation.util.Format.percentage
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.GeneRole
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import java.time.LocalDate

class GeneIsInactivated(private val gene: String, maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge) {

    override fun genes() = listOf(gene)

    override fun evaluate(test: MolecularTest): Evaluation {
        val inactivationEventsThatQualify: MutableSet<String> = mutableSetOf()
        val inactivationEventsThatAreUnreportable: MutableSet<String> = mutableSetOf()
        val inactivationEventsNoTSG: MutableSet<String> = mutableSetOf()
        val inactivationEventsGainOfFunction: MutableSet<String> = mutableSetOf()
        val inactivationEventsOnNonCanonicalTranscript: MutableSet<String> = mutableSetOf()
        val evidenceSource = test.evidenceSource

        sequenceOf(
            test.drivers.copyNumbers.asSequence().filter { it.otherImpacts.any { impact -> impact.type == CopyNumberType.LOSS } }
        ).flatten()
            .filter { it.gene == gene }
            .forEach { geneAlterationDriver -> inactivationEventsOnNonCanonicalTranscript.add(geneAlterationDriver.event) }

        val drivers = test.drivers
        sequenceOf(
            drivers.homozygousDisruptions.asSequence(),
            drivers.copyNumbers.asSequence().filter { it.canonicalImpact.type == CopyNumberType.LOSS }
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
        val hasHighMutationalLoad = test.characteristics.hasHighTumorMutationalLoad
        for (variant in drivers.variants) {
            if (variant.gene == gene && INACTIVATING_CODING_EFFECTS.contains(variant.canonicalImpact.codingEffect)) {
                if (!variant.isReportable) {
                    inactivationEventsThatAreUnreportable.add(variant.event)
                } else {
                    val extendedVariant = variant.extendedVariantDetails
                    val phaseGroups: Set<Int>? = extendedVariant?.phaseGroups
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
                        } else if (extendedVariant?.isBiallelic == false) {
                            inactivationHighDriverNonBiallelicVariants.add(variant.event)
                        } else if (isGainOfFunction) {
                            inactivationEventsGainOfFunction.add(variant.event)
                        } else if (extendedVariant?.clonalLikelihood?.let { it < CLONAL_CUTOFF } == true) {
                            inactivationSubclonalVariants.add(variant.event)
                        } else {
                            inactivationEventsThatQualify.add(variant.event)
                        }
                    } else if ((hasHighMutationalLoad == null || !hasHighMutationalLoad) && extendedVariant?.isBiallelic == true) {
                        reportableNonDriverBiallelicVariantsOther.add(variant.event)
                    } else if (
                        (variant.gene in MolecularConstants.HRD_GENES && test.characteristics.isHomologousRepairDeficient == true)
                        || (variant.gene in MolecularConstants.MSI_GENES && test.characteristics.isMicrosatelliteUnstable == true)
                    ) {
                        reportableNonDriverNonBiallelicVariantsOther.add(variant.event)
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
                "$gene inactivation (${concatWithCommaAndAnd(inactivationEventsThatQualify)})",
                inclusionEvents = inactivationEventsThatQualify
            )
        }

        val potentialWarnEvaluation = evaluatePotentialWarns(
            inactivationEventsThatAreUnreportable,
            inactivationEventsNoTSG,
            inactivationEventsGainOfFunction,
            inactivationHighDriverNonBiallelicVariants,
            inactivationSubclonalVariants,
            inactivationEventsOnNonCanonicalTranscript,
            reportableNonDriverBiallelicVariantsOther,
            reportableNonDriverNonBiallelicVariantsOther,
            eventsThatMayBeTransPhased,
            evidenceSource
        )

        return potentialWarnEvaluation ?: EvaluationFactory.fail("No $gene inactivation")
    }

    private fun evaluatePotentialWarns(
        inactivationEventsThatAreUnreportable: Set<String>,
        inactivationEventsNoTSG: Set<String>,
        inactivationEventsGainOfFunction: Set<String>,
        inactivationHighDriverNonBiallelicVariants: Set<String>,
        inactivationSubclonalVariants: Set<String>,
        inactivationEventsOnNonCanonicalTranscript: Set<String>,
        reportableNonDriverBiallelicVariantsOther: Set<String>,
        reportableNonDriverNonBiallelicVariantsOther: Set<String>,
        eventsThatMayBeTransPhased: List<String>, evidenceSource: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOfNotNull(
                EventsWithMessages(
                    inactivationEventsThatAreUnreportable,
                    "Inactivation event(s) for $gene (${concatWithCommaAndAnd(inactivationEventsThatAreUnreportable)}) but event(s) not reportable"
                ),
                EventsWithMessages(
                    inactivationEventsNoTSG,
                    "Inactivation event(s) for $gene (${concatWithCommaAndAnd(inactivationEventsNoTSG)})"
                            + " however gene is oncogene in $evidenceSource"
                ),
                EventsWithMessages(
                    inactivationEventsGainOfFunction,
                    "Inactivation event(s) for $gene (${concatWithCommaAndAnd(inactivationEventsGainOfFunction)})"
                            + " but event(s) annotated with gain-of-function protein impact evidence in $evidenceSource"
                ),
                if (inactivationHighDriverNonBiallelicVariants.isNotEmpty() && eventsThatMayBeTransPhased.size <= 1) {
                    EventsWithMessages(
                        inactivationHighDriverNonBiallelicVariants,
                        "Inactivation event(s) for $gene (${concatWithCommaAndAnd(inactivationHighDriverNonBiallelicVariants)}) but event(s) are not biallelic"
                    )
                } else null,
                EventsWithMessages(
                    inactivationSubclonalVariants,
                    "Inactivation event(s) for $gene (${concatWithCommaAndAnd(inactivationSubclonalVariants)}) but subclonal likelihood > "
                            + percentage(1 - CLONAL_CUTOFF)
                ),
                EventsWithMessages(
                    inactivationEventsOnNonCanonicalTranscript,
                    "Inactivation event(s) for $gene (${concatWithCommaAndAnd(inactivationSubclonalVariants)}) but only on non-canonical transcript"
                ),
                EventsWithMessages(
                    reportableNonDriverBiallelicVariantsOther,
                    "Potential inactivation event(s) for $gene (${concatWithCommaAndAnd(reportableNonDriverBiallelicVariantsOther)})"
                            + " but event(s) are not of high driver likelihood"
                ),
                EventsWithMessages(
                    reportableNonDriverNonBiallelicVariantsOther,
                    "Potential inactivation event(s) for $gene (${concatWithCommaAndAnd(reportableNonDriverNonBiallelicVariantsOther)})"
                            + " but event(s) are not biallelic and not of high driver likelihood"
                ),
                if (eventsThatMayBeTransPhased.size > 1) {
                    EventsWithMessages(
                        eventsThatMayBeTransPhased.toSet(),
                        "Multiple events for $gene (${concatWithCommaAndAnd(eventsThatMayBeTransPhased)}) that potentially together inactivate the gene"
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