package com.hartwig.actin.molecular.evidence.actionability

import com.google.common.collect.Sets
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.gene.GeneEvent

object TestActionableEventMatcherFactory {

    fun createProper(): ActionableEventMatcher {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        val applicableDoids: MutableSet<String> = Sets.newHashSet("parent")
        val personalizedActionabilityFactory = PersonalizedActionabilityFactory(doidModel, applicableDoids)
        val actionableEvents: ActionableEvents = ImmutableActionableEvents.builder()
            .addHotspots(TestServeActionabilityFactory.hotspotBuilder().build())
            .addCodons(TestServeActionabilityFactory.rangeBuilder().build())
            .addExons(TestServeActionabilityFactory.rangeBuilder().build())
            .addGenes(TestServeActionabilityFactory.geneBuilder().event(GeneEvent.DELETION).build())
            .addGenes(TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).build())
            .addGenes(TestServeActionabilityFactory.geneBuilder().event(GeneEvent.ANY_MUTATION).build())
            .addFusions(TestServeActionabilityFactory.fusionBuilder().build())
            .addCharacteristics(create(TumorCharacteristicType.MICROSATELLITE_UNSTABLE))
            .addCharacteristics(create(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT))
            .addCharacteristics(create(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN))
            .addCharacteristics(create(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD))
            .addCharacteristics(create(TumorCharacteristicType.HPV_POSITIVE))
            .addCharacteristics(create(TumorCharacteristicType.EBV_POSITIVE))
            .build()

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

    private fun create(type: TumorCharacteristicType): ActionableCharacteristic {
        return TestServeActionabilityFactory.characteristicBuilder().type(type).build()
    }
}
