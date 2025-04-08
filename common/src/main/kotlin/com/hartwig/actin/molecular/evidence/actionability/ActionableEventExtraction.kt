package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.EvidenceType
import com.hartwig.serve.datamodel.molecular.ActionableEvent
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.immuno.ActionableHLA
import com.hartwig.serve.datamodel.molecular.range.ActionableRange
import java.util.function.Predicate

data class EvidenceTypeAndEvent(val evidenceType: EvidenceType?, val actionableEvent: ActionableEvent)

object ActionableEventExtraction {

    fun extractEvent(molecularCriterium: MolecularCriterium): EvidenceTypeAndEvent {
        return when {
            hotspotFilter().test(molecularCriterium) -> {
                EvidenceTypeAndEvent(EvidenceType.HOTSPOT_MUTATION, extractHotspot(molecularCriterium))
            }

            codonFilter().test(molecularCriterium) -> {
                EvidenceTypeAndEvent(EvidenceType.CODON_MUTATION, extractCodon(molecularCriterium))
            }

            exonFilter().test(molecularCriterium) -> {
                EvidenceTypeAndEvent(EvidenceType.EXON_MUTATION, extractExon(molecularCriterium))
            }

            geneFilter(GeneEvent.entries.toSet()).test(molecularCriterium) -> {
                val gene = extractGene(molecularCriterium)
                EvidenceTypeAndEvent(fromActionableGene(gene), gene)
            }

            fusionFilter().test(molecularCriterium) -> {
                EvidenceTypeAndEvent(EvidenceType.FUSION_PAIR, extractFusion(molecularCriterium))
            }

            characteristicsFilter(TumorCharacteristicType.entries.toSet()).test(molecularCriterium) -> {
                val characteristic = extractCharacteristic(molecularCriterium)
                EvidenceTypeAndEvent(fromActionableCharacteristic(characteristic), characteristic)
            }

            hlaFilter().test(molecularCriterium) -> {
                EvidenceTypeAndEvent(EvidenceType.HLA, extractHla(molecularCriterium))
            }

            else -> throw IllegalStateException("Could not extract event for molecular criterium: $molecularCriterium")
        }
    }

    fun extractHotspot(molecularCriterium: MolecularCriterium): ActionableHotspot {
        return molecularCriterium.hotspots().first()
    }

    fun extractCodon(molecularCriterium: MolecularCriterium): ActionableRange {
        return molecularCriterium.codons().first()
    }

    fun extractExon(molecularCriterium: MolecularCriterium): ActionableRange {
        return molecularCriterium.exons().first()
    }

    fun extractGene(molecularCriterium: MolecularCriterium): ActionableGene {
        return molecularCriterium.genes().first()
    }

    fun extractFusion(molecularCriterium: MolecularCriterium): ActionableFusion {
        return molecularCriterium.fusions().first()
    }

    fun extractCharacteristic(molecularCriterium: MolecularCriterium): ActionableCharacteristic {
        return molecularCriterium.characteristics().first()
    }

    private fun extractHla(molecularCriterium: MolecularCriterium): ActionableHLA {
        return molecularCriterium.hla().first()
    }

    fun hotspotFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.hotspots().isNotEmpty() }
    }

    fun codonFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.codons().isNotEmpty() }
    }

    fun exonFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.exons().isNotEmpty() }
    }

    fun geneFilter(validGeneEvents: Set<GeneEvent>): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.genes().any { validGeneEvents.contains(it.event()) } }
    }

    fun fusionFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.fusions().isNotEmpty() }
    }

    fun characteristicsFilter(validTypes: Set<TumorCharacteristicType>): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.characteristics().any { validTypes.contains(it.type()) } }
    }

    private fun hlaFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.hla().isNotEmpty() }
    }

    private fun fromActionableGene(gene: ActionableGene): EvidenceType? {
        return when (gene.event()) {
            GeneEvent.AMPLIFICATION -> EvidenceType.AMPLIFICATION
            GeneEvent.OVEREXPRESSION -> EvidenceType.OVER_EXPRESSION
            GeneEvent.PRESENCE_OF_PROTEIN -> EvidenceType.PRESENCE_OF_PROTEIN
            GeneEvent.DELETION -> EvidenceType.DELETION
            GeneEvent.UNDEREXPRESSION -> EvidenceType.UNDER_EXPRESSION
            GeneEvent.ABSENCE_OF_PROTEIN -> EvidenceType.ABSENCE_OF_PROTEIN
            GeneEvent.ACTIVATION -> EvidenceType.ACTIVATION
            GeneEvent.INACTIVATION -> EvidenceType.INACTIVATION
            GeneEvent.ANY_MUTATION -> EvidenceType.ANY_MUTATION
            GeneEvent.FUSION -> EvidenceType.PROMISCUOUS_FUSION
            GeneEvent.WILD_TYPE -> EvidenceType.WILD_TYPE
            else -> {
                return null
            }
        }
    }

    private fun fromActionableCharacteristic(characteristic: ActionableCharacteristic): EvidenceType? {
        return when (characteristic.type()) {
            TumorCharacteristicType.MICROSATELLITE_UNSTABLE, TumorCharacteristicType.MICROSATELLITE_STABLE, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD, TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD, TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN, TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN, TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT -> EvidenceType.SIGNATURE
            TumorCharacteristicType.HPV_POSITIVE, TumorCharacteristicType.EBV_POSITIVE -> EvidenceType.VIRAL_PRESENCE
            else -> {
                return null
            }
        }
    }
}