package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import java.time.LocalDate

class GeneIsInactivated(override val gene: String, maxTestAge: LocalDate? = null) :
    MolecularEvaluationFunction(
        targetCoveragePredicate = or(MolecularTestTarget.MUTATION, MolecularTestTarget.DELETION, messagePrefix = "Inactivation of"),
        maxTestAge = maxTestAge
    ) {

    override fun evaluate(test: MolecularTest): Evaluation {
        val inactivationEventsThatQualify: MutableSet<String> = mutableSetOf()
        val inactivationEventsThatAreUnreportable: MutableSet<String> = mutableSetOf()
        val inactivationEventsNoTSG: MutableSet<String> = mutableSetOf()
        val inactivationEventsGainOfFunction: MutableSet<String> = mutableSetOf()
        val inactivationEventsOnNonCanonicalTranscript: MutableSet<String> = mutableSetOf()
        val evidenceSource = test.evidenceSource

        sequenceOf(
            test.drivers.copyNumbers.asSequence().filter { it.otherImpacts.any { impact -> impact.type == CopyNumberType.DEL } }
        ).flatten()
            .filter { it.gene == gene }
            .forEach { geneAlterationDriver -> inactivationEventsOnNonCanonicalTranscript.add(geneAlterationDriver.event) }

        val drivers = test.drivers
        sequenceOf(
            drivers.homozygousDisruptions.asSequence(),
            drivers.copyNumbers.asSequence().filter { it.canonicalImpact.type == CopyNumberType.DEL }
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
        val eventsThatMayBeTransPhased: MutableList<String> = mutableListOf()
        val evaluatedPhaseGroups: MutableSet<Int?> = mutableSetOf()
        val hasHighMutationalLoad = test.characteristics.tumorMutationalLoad?.isHigh
        for (variant in drivers.variants) {
            val variantIsClonal = variant.extendedVariantDetails?.clonalLikelihood?.let { it >= CLONAL_CUTOFF } ?: true
            if (variant.gene == gene && variantIsClonal && INACTIVATING_CODING_EFFECTS.contains(variant.canonicalImpact.codingEffect)) {
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
                        } else {
                            inactivationEventsThatQualify.add(variant.event)
                        }
                    } else if ((hasHighMutationalLoad == null || !hasHighMutationalLoad) && extendedVariant?.isBiallelic == true) {
                        reportableNonDriverBiallelicVariantsOther.add(variant.event)
                    } else if (
                        (variant.gene in MolecularConstants.HRD_GENES && test.characteristics.homologousRecombination?.isDeficient == true)
                        || (variant.gene in MolecularConstants.MSI_GENES && test.characteristics.microsatelliteStability?.isUnstable == true)
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
                "$gene inactivation (${concat(inactivationEventsThatQualify)})",
                inclusionEvents = inactivationEventsThatQualify
            )
        }

        val potentialWarnEvaluation = evaluatePotentialWarns(
            inactivationEventsThatAreUnreportable,
            inactivationEventsNoTSG,
            inactivationEventsGainOfFunction,
            inactivationHighDriverNonBiallelicVariants,
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
        inactivationEventsOnNonCanonicalTranscript: Set<String>,
        reportableNonDriverBiallelicVariantsOther: Set<String>,
        reportableNonDriverNonBiallelicVariantsOther: Set<String>,
        eventsThatMayBeTransPhased: List<String>, evidenceSource: String
    ): Evaluation? {
        return MolecularEventUtil.evaluatePotentialWarnsForEventGroups(
            listOfNotNull(
                EventsWithMessages(
                    inactivationEventsThatAreUnreportable,
                    "Inactivation event(s) ${concat(inactivationEventsThatAreUnreportable)} for $gene but event(s) not reportable"
                ),
                EventsWithMessages(
                    inactivationEventsNoTSG,
                    "Inactivation event(s) ${concat(inactivationEventsNoTSG)} for $gene"
                            + " however gene is oncogene in $evidenceSource"
                ),
                EventsWithMessages(
                    inactivationEventsGainOfFunction,
                    "Inactivation event(s) ${concat(inactivationEventsGainOfFunction)} for $gene"
                            + " however event(s) annotated with gain-of-function protein impact in $evidenceSource"
                ),
                if (inactivationHighDriverNonBiallelicVariants.isNotEmpty() && eventsThatMayBeTransPhased.size <= 1) {
                    EventsWithMessages(
                        inactivationHighDriverNonBiallelicVariants,
                        "Inactivation event(s) ${concat(inactivationHighDriverNonBiallelicVariants)} for $gene but event(s) are not biallelic"
                    )
                } else null,
                EventsWithMessages(
                    inactivationEventsOnNonCanonicalTranscript,
                    "Inactivation event(s) ${concat(inactivationEventsOnNonCanonicalTranscript)} for $gene but only on non-canonical transcript"
                ),
                EventsWithMessages(
                    reportableNonDriverBiallelicVariantsOther,
                    "Potential inactivation event(s) ${concat(reportableNonDriverBiallelicVariantsOther)} for $gene"
                            + " but event(s) are not of high driver likelihood"
                ),
                EventsWithMessages(
                    reportableNonDriverNonBiallelicVariantsOther,
                    "Potential inactivation event(s) ${concat(reportableNonDriverNonBiallelicVariantsOther)} for $gene"
                            + " but event(s) are not biallelic and not of high driver likelihood"
                ),
                if (eventsThatMayBeTransPhased.size > 1) {
                    EventsWithMessages(
                        eventsThatMayBeTransPhased.toSet(),
                        "Multiple events for $gene (${concat(eventsThatMayBeTransPhased)}) that potentially together inactivate the gene"
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