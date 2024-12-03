package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

object TestActionableEventMatcherFactory {

    fun createProper(): ActionableEventMatcher {
        val applicableDoids = setOf("parent", "child")
        val personalizedActionabilityFactory = PersonalizedActionabilityFactory(applicableDoids)

        val evidences: List<EfficacyEvidence> = listOf(
            TestServeEvidenceFactory.createEvidenceForHotspot(),
            TestServeEvidenceFactory.createEvidenceForCodon(),
            TestServeEvidenceFactory.createEvidenceForExon(),
            TestServeEvidenceFactory.createEvidenceForGene(geneEvent = GeneEvent.DELETION),
            TestServeEvidenceFactory.createEvidenceForGene(geneEvent = GeneEvent.AMPLIFICATION),
            TestServeEvidenceFactory.createEvidenceForGene(geneEvent = GeneEvent.ANY_MUTATION),
            TestServeEvidenceFactory.createEvidenceForFusion(),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HPV_POSITIVE),
            TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.EBV_POSITIVE),
            TestServeEvidenceFactory.createEvidenceForHLA()
        )
        val actionableEvents = ActionableEvents(evidences, emptyList())

        return ActionableEventMatcher(
            personalizedActionabilityFactory,
            SignatureEvidence.create(actionableEvents),
            VariantEvidence.create(actionableEvents),
            CopyNumberEvidence.create(actionableEvents),
            HomozygousDisruptionEvidence.create(actionableEvents),
            BreakendEvidence.create(actionableEvents),
            FusionEvidence.create(actionableEvents),
            VirusEvidence.create(actionableEvents)
        )
    }
}
