package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.molecular.util.GeneConstants
import java.time.LocalDate

class GeneIsInactivated(override val gene: String, maxTestAge: LocalDate? = null, private val onlyDeletions: Boolean) :
    MolecularEvaluationFunction(
        targetCoveragePredicate = if (onlyDeletions) {
            specific(
                MolecularTestTarget.DELETION,
                messagePrefix = "Deletion of"
            )
        } else {
            or(MolecularTestTarget.MUTATION, MolecularTestTarget.DELETION, messagePrefix = "Inactivation of")
        }, maxTestAge = maxTestAge
    ) {

    override fun evaluate(test: MolecularTest, ihcTests: List<IhcTest>): Evaluation {
        val inactivationEventsThatQualify: MutableSet<String> = mutableSetOf()
        val ihcLossEventsThatAreIndeterminate: MutableSet<String> = mutableSetOf()

        val ihcTestEvaluation =
            if (gene in GeneConstants.IHC_LOSS_EVALUABLE_GENES) IhcTestEvaluation.create(gene, ihcTests) else null
        if (ihcTestEvaluation?.hasCertainLossResultsForItem() == true) {
            inactivationEventsThatQualify.add("$gene loss by IHC")
        } else if (ihcTestEvaluation?.hasPossibleLossResultsForItem() == true) {
            ihcLossEventsThatAreIndeterminate.add("$gene potential loss by IHC")
        }

        val inactivationEventsThatAreUnreportable: MutableSet<String> = mutableSetOf()
        val inactivationEventsNoTSG: MutableSet<String> = mutableSetOf()
        val inactivationEventsGainOfFunction: MutableSet<String> = mutableSetOf()
        val inactivationEventsNoEffect: MutableSet<String> = mutableSetOf()
        val inactivationEventsOnNonCanonicalTranscript: MutableSet<String> = mutableSetOf()
        val evidenceSource = test.evidenceSource

        sequenceOf(
            test.drivers.copyNumbers.asSequence().filter { it.otherImpacts.any { impact -> impact.type.isDeletion } }
        ).flatten()
            .filter { it.gene == gene }
            .forEach { geneAlterationDriver -> inactivationEventsOnNonCanonicalTranscript.add(geneAlterationDriver.event) }

        val drivers = test.drivers
        sequenceOf(
            drivers.homozygousDisruptions.asSequence(),
            drivers.copyNumbers.asSequence().filter { it.canonicalImpact.type.isDeletion }
        ).flatten()
            .filter { it.gene == gene }
            .forEach { geneAlterationDriver ->
                val isGainOfFunction = (geneAlterationDriver.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION
                        || geneAlterationDriver.proteinEffect == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
                val isNoEffect = (geneAlterationDriver.proteinEffect == ProteinEffect.NO_EFFECT
                        || geneAlterationDriver.proteinEffect == ProteinEffect.NO_EFFECT_PREDICTED)
                if (!geneAlterationDriver.isReportable) {
                    inactivationEventsThatAreUnreportable.add(geneAlterationDriver.event)
                } else if (geneAlterationDriver.geneRole == GeneRole.ONCO) {
                    inactivationEventsNoTSG.add(geneAlterationDriver.event)
                } else if (isGainOfFunction) {
                    inactivationEventsGainOfFunction.add(geneAlterationDriver.event)
                } else if (isNoEffect) {
                    inactivationEventsNoEffect.add(geneAlterationDriver.event)
                } else {
                    inactivationEventsThatQualify.add(geneAlterationDriver.event)
                }
            }

        val reportableNonDriverBiallelicVariantsOther: MutableSet<String> = mutableSetOf()
        val reportableNonDriverNonBiallelicVariantsOther: MutableSet<String> = mutableSetOf()
        val inactivationHighDriverNonBiallelicVariants: MutableSet<String> = mutableSetOf()
        val inactivationHighDriverUnknownBiallelicVariants: MutableSet<String> = mutableSetOf()
        val eventsThatMayBeTransPhased: MutableList<String> = mutableListOf()
        val evaluatedPhaseGroups: MutableSet<Int?> = mutableSetOf()

        if (!onlyDeletions) {
            val hasHighMutationalLoad = test.characteristics.tumorMutationalLoad?.isHigh
            for (variant in drivers.variants) {
                val variantIsClonal = variant.clonalLikelihood?.let { it >= CLONAL_CUTOFF } ?: true
                if (variant.gene == gene && variantIsClonal && INACTIVATING_CODING_EFFECTS.contains(variant.canonicalImpact.codingEffect)) {
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
                            val isNoEffect = (variant.proteinEffect == ProteinEffect.NO_EFFECT
                                    || variant.proteinEffect == ProteinEffect.NO_EFFECT_PREDICTED)
                            if (variant.geneRole == GeneRole.ONCO) {
                                inactivationEventsNoTSG.add(variant.event)
                            } else if (variant.isBiallelic == false) {
                                inactivationHighDriverNonBiallelicVariants.add(variant.event)
                            } else if (variant.isBiallelic == null) {
                                inactivationHighDriverUnknownBiallelicVariants.add(variant.event)
                            } else if (isGainOfFunction) {
                                inactivationEventsGainOfFunction.add(variant.event)
                            } else if (isNoEffect) {
                                inactivationEventsNoEffect.add(variant.event)
                            } else {
                                inactivationEventsThatQualify.add(variant.event)
                            }
                        } else if ((hasHighMutationalLoad == null || !hasHighMutationalLoad) && variant.isBiallelic == true) {
                            reportableNonDriverBiallelicVariantsOther.add(variant.event)
                        } else if (
                            (variant.gene in GeneConstants.HR_GENES && test.characteristics.homologousRecombination?.isDeficient == true)
                            || (variant.gene in GeneConstants.MMR_GENES && test.characteristics.microsatelliteStability?.isUnstable == true)
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
        }

        val messageSubject = if (onlyDeletions) "deletion" else "inactivation"

        if (inactivationEventsThatQualify.isNotEmpty()) {
            return EvaluationFactory.pass(
                "$gene $messageSubject (${concat(inactivationEventsThatQualify)})",
                inclusionEvents = inactivationEventsThatQualify
            )
        }

        val potentialWarnEvaluation = evaluatePotentialWarns(
            ihcLossEventsThatAreIndeterminate,
            inactivationEventsThatAreUnreportable,
            inactivationEventsNoTSG,
            inactivationEventsGainOfFunction,
            inactivationEventsNoEffect,
            inactivationHighDriverNonBiallelicVariants,
            inactivationHighDriverUnknownBiallelicVariants,
            inactivationEventsOnNonCanonicalTranscript,
            reportableNonDriverBiallelicVariantsOther,
            reportableNonDriverNonBiallelicVariantsOther,
            eventsThatMayBeTransPhased,
            evidenceSource,
            messageSubject
        )

        return potentialWarnEvaluation ?: EvaluationFactory.fail("No $gene $messageSubject")
    }

    private fun evaluatePotentialWarns(
        ihcLossEventsThatAreIndeterminate: Set<String>,
        inactivationEventsThatAreUnreportable: Set<String>,
        inactivationEventsNoTSG: Set<String>,
        inactivationEventsGainOfFunction: Set<String>,
        inactivationEventsNoEffect: Set<String>,
        inactivationHighDriverNonBiallelicVariants: Set<String>,
        inactivationHighDriverUnknownBiallelicVariants: Set<String>,
        inactivationEventsOnNonCanonicalTranscript: Set<String>,
        reportableNonDriverBiallelicVariantsOther: Set<String>,
        reportableNonDriverNonBiallelicVariantsOther: Set<String>,
        eventsThatMayBeTransPhased: List<String>, evidenceSource: String,
        messageSubject: String
    ): Evaluation? {
        val messageSubjectCapitalized = messageSubject.replaceFirstChar { it.titlecase() }

        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOfNotNull(
                EventsWithMessages(
                    ihcLossEventsThatAreIndeterminate,
                    "$messageSubjectCapitalized event(s) ${concat(ihcLossEventsThatAreIndeterminate)} for $gene but unclear how to interpret IHC result"
                ),
                EventsWithMessages(
                    inactivationEventsThatAreUnreportable,
                    "$messageSubjectCapitalized event(s) ${concat(inactivationEventsThatAreUnreportable)} for $gene but event(s) not reportable"
                ),
                EventsWithMessages(
                    inactivationEventsNoTSG,
                    "$messageSubjectCapitalized event(s) ${concat(inactivationEventsNoTSG)} for $gene"
                            + " however gene is oncogene in $evidenceSource"
                ),
                EventsWithMessages(
                    inactivationEventsGainOfFunction,
                    "$messageSubjectCapitalized event(s) ${concat(inactivationEventsGainOfFunction)} for $gene"
                            + " however event(s) annotated with gain-of-function protein impact in $evidenceSource"
                ),
                EventsWithMessages(
                    inactivationEventsNoEffect,
                    "$messageSubjectCapitalized event(s) ${concat(inactivationEventsNoEffect)} for $gene"
                            + " however event(s) annotated with no protein effect in $evidenceSource"
                ),
                if (inactivationHighDriverNonBiallelicVariants.isNotEmpty() && eventsThatMayBeTransPhased.size <= 1) {
                    EventsWithMessages(
                        inactivationHighDriverNonBiallelicVariants,
                        "$messageSubjectCapitalized event(s) ${concat(inactivationHighDriverNonBiallelicVariants)} for $gene but event(s) are not biallelic"
                    )
                } else null,
                if (inactivationHighDriverUnknownBiallelicVariants.isNotEmpty() && eventsThatMayBeTransPhased.size <= 1) {
                    EventsWithMessages(
                        inactivationHighDriverUnknownBiallelicVariants,
                        "$messageSubjectCapitalized event(s) ${concat(inactivationHighDriverUnknownBiallelicVariants)} for $gene but unknown if event(s) are biallelic"
                    )
                } else null,
                EventsWithMessages(
                    inactivationEventsOnNonCanonicalTranscript,
                    "$messageSubjectCapitalized event(s) ${concat(inactivationEventsOnNonCanonicalTranscript)} for $gene but only on non-canonical transcript"
                ),
                EventsWithMessages(
                    reportableNonDriverBiallelicVariantsOther,
                    "Potential $messageSubject event(s) ${concat(reportableNonDriverBiallelicVariantsOther)} for $gene"
                            + " but event(s) are not of high driver likelihood"
                ),
                EventsWithMessages(
                    reportableNonDriverNonBiallelicVariantsOther,
                    "Potential $messageSubject event(s) ${concat(reportableNonDriverNonBiallelicVariantsOther)} for $gene"
                            + " but event(s) are not biallelic and not of high driver likelihood"
                ),
                if (eventsThatMayBeTransPhased.size > 1) {
                    EventsWithMessages(
                        eventsThatMayBeTransPhased.toSet(),
                        "Multiple events for $gene (${concat(eventsThatMayBeTransPhased)}) that potentially together cause $messageSubject of the gene"
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