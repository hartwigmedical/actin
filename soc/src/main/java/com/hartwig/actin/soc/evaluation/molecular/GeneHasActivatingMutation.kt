package com.hartwig.actin.soc.evaluation.molecular

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.Variant

class GeneHasActivatingMutation internal constructor(private val gene: String) : EvaluationFunction {
    fun evaluate(record: PatientRecord): Evaluation {
        val activatingVariants: MutableSet<String> = Sets.newHashSet()
        val activatingVariantsAssociatedWithResistance: MutableSet<String> = Sets.newHashSet()
        val activatingVariantsNoHotspotAndNoGainOfFunction: MutableSet<String> = Sets.newHashSet()
        val activatingVariantsInNonOncogene: MutableSet<String> = Sets.newHashSet()
        val activatingSubclonalVariants: MutableSet<String> = Sets.newHashSet()
        val nonHighDriverGainOfFunctionVariants: MutableSet<String> = Sets.newHashSet()
        val nonHighDriverVariants: MutableSet<String> = Sets.newHashSet()
        val otherMissenseOrHotspotVariants: MutableSet<String> = Sets.newHashSet()
        val hasHighMutationalLoad = record.molecular().characteristics().hasHighTumorMutationalLoad()
        for (variant in record.molecular().drivers().variants()) {
            if (variant.gene() == gene) {
                val isGainOfFunction = (variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                        || variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)
                val isNoOncogene = variant.geneRole() == GeneRole.TSG
                if (variant.isReportable) {
                    if (variant.driverLikelihood() == DriverLikelihood.HIGH) {
                        if (isAssociatedWithDrugResistance(variant)) {
                            activatingVariantsAssociatedWithResistance.add(variant.event())
                        } else if (!variant.isHotspot && !isGainOfFunction) {
                            activatingVariantsNoHotspotAndNoGainOfFunction.add(variant.event())
                        } else if (isNoOncogene) {
                            activatingVariantsInNonOncogene.add(variant.event())
                        } else if (variant.clonalLikelihood() < CLONAL_CUTOFF) {
                            activatingSubclonalVariants.add(variant.event())
                        } else {
                            activatingVariants.add(variant.event())
                        }
                    } else {
                        if (isGainOfFunction) {
                            nonHighDriverGainOfFunctionVariants.add(variant.event())
                        } else if (hasHighMutationalLoad == null || !hasHighMutationalLoad) {
                            nonHighDriverVariants.add(variant.event())
                        }
                    }
                } else if (isMissenseOrHotspot(variant)) {
                    otherMissenseOrHotspotVariants.add(variant.event())
                }
            }
        }
        if (!activatingVariants.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(activatingVariants)
                    .addPassSpecificMessages("Activating mutation(s) detected in gene + " + gene + ": " + Format.concat(activatingVariants))
                    .addPassGeneralMessages("$gene activating mutation(s)")
                    .build()
        }
        val potentialWarnEvaluation = evaluatePotentialWarns(activatingVariantsAssociatedWithResistance,
                activatingVariantsInNonOncogene,
                activatingVariantsNoHotspotAndNoGainOfFunction,
                activatingSubclonalVariants,
                nonHighDriverGainOfFunctionVariants,
                nonHighDriverVariants,
                otherMissenseOrHotspotVariants)
        return potentialWarnEvaluation
                ?: EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.FAIL)
                        .addFailSpecificMessages("No activating mutation(s) detected in gene $gene")
                        .addFailGeneralMessages("No $gene activating mutation(s)")
                        .build()
    }

    private fun evaluatePotentialWarns(activatingVariantsAssociatedWithResistance: Set<String>,
                                       activatingVariantsInNonOncogene: Set<String>, activatingVariantsNoHotspotAndNoGainOfFunction: Set<String>,
                                       activatingSubclonalVariants: Set<String>, nonHighDriverGainOfFunctionVariants: Set<String>,
                                       nonHighDriverVariants: Set<String>, otherMissenseOrHotspotVariants: Set<String>): Evaluation? {
        val warnEvents: MutableSet<String> = Sets.newHashSet()
        val warnSpecificMessages: MutableSet<String> = Sets.newHashSet()
        val warnGeneralMessages: MutableSet<String> = Sets.newHashSet()
        if (!activatingVariantsAssociatedWithResistance.isEmpty()) {
            warnEvents.addAll(activatingVariantsAssociatedWithResistance)
            warnSpecificMessages.add(
                    "Gene " + gene + " should have activating mutation(s): " + Format.concat(activatingVariantsAssociatedWithResistance) + ", however, these are (also) associated with drug resistance")
            warnGeneralMessages.add("$gene activating mutation(s) detected but associated with drug resistance")
        }
        if (!activatingVariantsInNonOncogene.isEmpty()) {
            warnEvents.addAll(activatingVariantsInNonOncogene)
            warnSpecificMessages.add("Gene " + gene + " has activating mutation(s) " + Format.concat(activatingVariantsInNonOncogene) + " but gene known as TSG")
            warnGeneralMessages.add("$gene activating mutation(s) detected but $gene known as TSG")
        }
        if (!activatingVariantsNoHotspotAndNoGainOfFunction.isEmpty()) {
            warnEvents.addAll(activatingVariantsNoHotspotAndNoGainOfFunction)
            warnSpecificMessages.add("Gene $gene has potentially activating mutation(s) " + Format.concat(
                    activatingVariantsNoHotspotAndNoGainOfFunction) + " that have high driver likelihood, but is not a hotspot and not associated with gain of function protein effect")
            warnGeneralMessages.add(gene
                    + " potentially activating mutation(s) detected but is not a hotspot and not associated with having gain-of-function protein effect")
        }
        if (!activatingSubclonalVariants.isEmpty()) {
            warnEvents.addAll(activatingSubclonalVariants)
            warnSpecificMessages.add("Gene " + gene + " potentially activating mutation(s) " + Format.concat(activatingSubclonalVariants) + " have subclonal likelihood of > " + Format.percentage(1 - CLONAL_CUTOFF))
            warnGeneralMessages.add(gene + " potentially activating mutation(s) " + Format.concat(activatingSubclonalVariants) + " but subclonal likelihood > " + Format.percentage(1 - CLONAL_CUTOFF))
        }
        if (!nonHighDriverGainOfFunctionVariants.isEmpty()) {
            warnEvents.addAll(nonHighDriverGainOfFunctionVariants)
            warnSpecificMessages.add(
                    "Gene " + gene + " has potentially activating mutation(s) " + Format.concat(nonHighDriverGainOfFunctionVariants) + " that do not have high driver likelihood, but are associated with gain-of-function protein effect")
            warnGeneralMessages.add(
                    "$gene potentially activating mutation(s) detected based on protein effect but no high driver likelihood")
        }
        if (!nonHighDriverVariants.isEmpty()) {
            warnEvents.addAll(nonHighDriverVariants)
            warnSpecificMessages.add("Gene " + gene + " has potentially activating mutation(s) " + Format.concat(nonHighDriverVariants) + " but no high driver likelihood")
            warnGeneralMessages.add("$gene potentially activating mutation(s) detected but no high driver likelihood")
        }
        if (!otherMissenseOrHotspotVariants.isEmpty()) {
            warnEvents.addAll(otherMissenseOrHotspotVariants)
            warnSpecificMessages.add(
                    "Gene " + gene + " has potentially activating mutation(s) " + Format.concat(otherMissenseOrHotspotVariants) + " that are missense or have hotspot status, but are not considered reportable")
            warnGeneralMessages.add("$gene potentially activating mutation(s) detected but is unreportable")
        }
        return if (!warnEvents.isEmpty() && !warnSpecificMessages.isEmpty() && !warnGeneralMessages.isEmpty()) {
            EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(warnEvents)
                    .addAllWarnSpecificMessages(warnSpecificMessages)
                    .addAllWarnGeneralMessages(warnGeneralMessages)
                    .build()
        } else null
    }

    companion object {
        private const val CLONAL_CUTOFF = 0.5
        private fun isAssociatedWithDrugResistance(variant: Variant): Boolean {
            val isAssociatedWithDrugResistance = variant.isAssociatedWithDrugResistance
            return isAssociatedWithDrugResistance != null && isAssociatedWithDrugResistance
        }

        private fun isMissenseOrHotspot(variant: Variant): Boolean {
            return variant.canonicalImpact().codingEffect() == CodingEffect.MISSENSE || variant.isHotspot
        }
    }
}