package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

object TestActionableEventMatcherFactory {

    fun createProper(): ActionableEventMatcher {
        val applicableDoids = setOf("parent", "child")
        val personalizedActionabilityFactory = PersonalizedActionabilityFactory(applicableDoids)

        val evidences: List<EfficacyEvidence> = listOf(
            TestServeActionabilityFactory.createEvidenceForHotspot(),
            TestServeActionabilityFactory.createEvidenceForCodon(),
            TestServeActionabilityFactory.createEvidenceForExon(),
            TestServeActionabilityFactory.createEvidenceForGene(geneEvent = GeneEvent.DELETION),
            TestServeActionabilityFactory.createEvidenceForGene(geneEvent = GeneEvent.AMPLIFICATION),
            TestServeActionabilityFactory.createEvidenceForGene(geneEvent = GeneEvent.ANY_MUTATION),
            TestServeActionabilityFactory.createEvidenceForFusion(),
            TestServeActionabilityFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE),
            TestServeActionabilityFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT),
            TestServeActionabilityFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN),
            TestServeActionabilityFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD),
            TestServeActionabilityFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HPV_POSITIVE),
            TestServeActionabilityFactory.createEvidenceForCharacteristic(TumorCharacteristicType.EBV_POSITIVE),
            TestServeActionabilityFactory.createEvidenceForHLA()
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
