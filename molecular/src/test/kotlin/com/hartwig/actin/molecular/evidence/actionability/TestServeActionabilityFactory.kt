package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeFactory
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.CancerType
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.ImmutableCancerType
import com.hartwig.serve.datamodel.ImmutableClinicalTrial
import com.hartwig.serve.datamodel.ImmutableTreatment
import com.hartwig.serve.datamodel.Intervention
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.characteristic.ImmutableActionableCharacteristic
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.fusion.ImmutableActionableFusion
import com.hartwig.serve.datamodel.gene.ImmutableActionableGene
import com.hartwig.serve.datamodel.hotspot.ImmutableActionableHotspot
import com.hartwig.serve.datamodel.immuno.ImmutableActionableHLA
import com.hartwig.serve.datamodel.range.ImmutableActionableRange

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
        return ImmutableActionableHLA.builder().from(createEmptyActionableEvent()).hlaAllele("")
    }

    fun treatmentBuilder(): ImmutableTreatment.Builder {
        return ImmutableTreatment.builder().name("")
    }

    fun cancerTypeBuilder(): ImmutableCancerType.Builder {
        return ImmutableCancerType.builder().name("").doid("")
    }

    private fun createEmptyActionableEvent(): ActionableEvent {
        return createActionableEvent(Knowledgebase.CKB_EVIDENCE, "intervention")
    }

    fun createActionableEvent(source: Knowledgebase, interventionName: String): ActionableEvent {
        val nctId = "NCT00000001"
        val isTrial = source == Knowledgebase.CKB_TRIAL
        return object : ActionableEvent {
            override fun source(): Knowledgebase {
                return source
            }

            override fun sourceEvent(): String {
                return ""
            }

            override fun sourceUrls(): Set<String> {
                return setOf("https://ckbhome.jax.org/profileResponse/advancedEvidenceFind?molecularProfileId=29716")
            }

            override fun intervention(): Intervention {
                return if (isTrial) {
                    ImmutableClinicalTrial.builder()
                        .studyAcronym(interventionName)
                        .studyNctId(nctId)
                        .studyTitle("")
                        .countriesOfStudy(setOf("country"))
                        .build()
                } else {
                    ImmutableTreatment.builder().name(interventionName).build()
                }
            }

            override fun applicableCancerType(): CancerType {
                return cancerTypeBuilder().build()
            }

            override fun blacklistCancerTypes(): Set<CancerType> {
                return emptySet()
            }

            override fun level(): EvidenceLevel {
                return EvidenceLevel.D
            }

            override fun direction(): EvidenceDirection {
                return EvidenceDirection.NO_BENEFIT
            }

            override fun evidenceUrls(): Set<String> {
                return if (isTrial) setOf("https://clinicaltrials.gov/study/$nctId") else emptySet()
            }
        }
    }
}
