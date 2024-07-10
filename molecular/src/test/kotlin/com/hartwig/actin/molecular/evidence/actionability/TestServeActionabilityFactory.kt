package com.hartwig.actin.molecular.evidence.actionability

import com.google.common.collect.Sets
import com.hartwig.actin.molecular.evidence.TestServeFactory
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.CancerType
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.ImmutableCancerType
import com.hartwig.serve.datamodel.ImmutableTreatment
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.Treatment
import com.hartwig.serve.datamodel.characteristic.ImmutableActionableCharacteristic
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.fusion.ImmutableActionableFusion
import com.hartwig.serve.datamodel.gene.ImmutableActionableGene
import com.hartwig.serve.datamodel.hotspot.ImmutableActionableHotspot
import com.hartwig.serve.datamodel.immuno.ImmutableActionableHLA
import com.hartwig.serve.datamodel.range.ImmutableActionableRange
import org.apache.logging.log4j.util.Strings

object TestServeActionabilityFactory {

    fun hotspotBuilder(): ImmutableActionableHotspot.Builder {
        return ImmutableActionableHotspot.builder().from(createEmptyActionableEvent()).from(TestServeFactory.createEmptyHotspot())
    }

    fun rangeBuilder(): ImmutableActionableRange.Builder {
        return ImmutableActionableRange.builder().from(createEmptyActionableEvent()).from(TestServeFactory.createEmptyRangeAnnotation())
    }

    fun geneBuilder(): ImmutableActionableGene.Builder {
        return ImmutableActionableGene.builder().from(createEmptyActionableEvent()).from(TestServeFactory.createEmptyGeneAnnotation())
    }

    fun fusionBuilder(): ImmutableActionableFusion.Builder {
        return ImmutableActionableFusion.builder().from(createEmptyActionableEvent()).from(TestServeFactory.createEmptyFusionPair())
    }

    fun characteristicBuilder(): ImmutableActionableCharacteristic.Builder {
        return ImmutableActionableCharacteristic.builder()
            .from(createEmptyActionableEvent())
            .type(TumorCharacteristicType.MICROSATELLITE_STABLE)
    }

    fun hlaBuilder(): ImmutableActionableHLA.Builder {
        return ImmutableActionableHLA.builder().from(createEmptyActionableEvent()).hlaAllele(Strings.EMPTY)
    }

    fun treatmentBuilder(): ImmutableTreatment.Builder {
        return ImmutableTreatment.builder().name(Strings.EMPTY)
    }

    fun cancerTypeBuilder(): ImmutableCancerType.Builder {
        return ImmutableCancerType.builder().name(Strings.EMPTY).doid(Strings.EMPTY)
    }

    private fun createEmptyActionableEvent(): ActionableEvent {
        return createActionableEvent(Knowledgebase.UNKNOWN, Strings.EMPTY)
    }

    fun createActionableEvent(source: Knowledgebase, treatment: String): ActionableEvent {
        return object : ActionableEvent {
            override fun source(): Knowledgebase {
                return source
            }

            override fun sourceEvent(): String {
                return Strings.EMPTY
            }

            override fun sourceUrls(): Set<String> {
                return Sets.newHashSet("https://clinicaltrials.gov/study/NCT00000001")
            }

            override fun treatment(): Treatment {
                return treatmentBuilder().name(treatment).build()
            }

            override fun applicableCancerType(): CancerType {
                return cancerTypeBuilder().build()
            }

            override fun blacklistCancerTypes(): MutableSet<CancerType?> {
                return Sets.newHashSet()
            }

            override fun level(): EvidenceLevel {
                return EvidenceLevel.D
            }

            override fun direction(): EvidenceDirection {
                return EvidenceDirection.NO_BENEFIT
            }

            override fun evidenceUrls(): MutableSet<String?> {
                // evidenceUrls() contains a set of countries
                return Sets.newHashSet("country")
            }
        }
    }
}
