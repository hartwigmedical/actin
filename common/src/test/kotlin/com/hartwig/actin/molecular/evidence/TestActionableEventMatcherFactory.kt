package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventMatcher
import com.hartwig.actin.molecular.evidence.actionability.ActionableEvents
import com.hartwig.actin.molecular.evidence.actionability.BreakendEvidence
import com.hartwig.actin.molecular.evidence.actionability.CopyNumberEvidence
import com.hartwig.actin.molecular.evidence.actionability.FusionEvidence
import com.hartwig.actin.molecular.evidence.actionability.HomozygousDisruptionEvidence
import com.hartwig.actin.molecular.evidence.actionability.PersonalizedActionabilityFactory
import com.hartwig.actin.molecular.evidence.actionability.SignatureEvidence
import com.hartwig.actin.molecular.evidence.actionability.VariantEvidence
import com.hartwig.actin.molecular.evidence.actionability.VirusEvidence
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

object TestActionableEventMatcherFactory {

    fun createProper(): ActionableEventMatcher {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        val applicableDoids = setOf("parent")
        val personalizedActionabilityFactory = PersonalizedActionabilityFactory(doidModel, applicableDoids)
        val evidences: List<EfficacyEvidence> = listOf(
            TestServeActionabilityFactory.withHotspot(),
            TestServeActionabilityFactory.withCodon(),
            TestServeActionabilityFactory.withExon(),
            TestServeActionabilityFactory.withGene(GeneEvent.DELETION),
            TestServeActionabilityFactory.withGene(GeneEvent.AMPLIFICATION),
            TestServeActionabilityFactory.withGene(GeneEvent.ANY_MUTATION),
            TestServeActionabilityFactory.withFusion(),
            TestServeActionabilityFactory.withCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE),
            TestServeActionabilityFactory.withCharacteristic(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT),
            TestServeActionabilityFactory.withCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN),
            TestServeActionabilityFactory.withCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD),
            TestServeActionabilityFactory.withCharacteristic(TumorCharacteristicType.HPV_POSITIVE),
            TestServeActionabilityFactory.withCharacteristic(TumorCharacteristicType.EBV_POSITIVE),
            TestServeActionabilityFactory.withHla()
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
