package com.hartwig.actin.molecular.evidence

import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.common.ImmutableCancerType
import com.hartwig.serve.datamodel.common.ImmutableIndication
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceDirection
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel
import com.hartwig.serve.datamodel.efficacy.EvidenceLevelDetails
import com.hartwig.serve.datamodel.efficacy.ImmutableTreatment
import com.hartwig.serve.datamodel.efficacy.Treatment
import com.hartwig.serve.datamodel.molecular.ActionableEvent
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.characteristic.ImmutableActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ImmutableActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.gene.ImmutableActionableGene
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableActionableHotspot
import com.hartwig.serve.datamodel.molecular.immuno.ImmutableActionableHLA
import com.hartwig.serve.datamodel.molecular.range.ImmutableActionableRange
import com.hartwig.serve.datamodel.trial.ActionableTrial
import com.hartwig.serve.datamodel.trial.Country
import com.hartwig.serve.datamodel.trial.GenderCriterium
import com.hartwig.serve.datamodel.trial.ImmutableCountry
import java.time.LocalDate

object TestServeActionabilityFactory {

    fun createEfficacyEvidenceWithHotspot(
        gene: String = "",
        chromosome: String = "",
        position: Int = 0,
        ref: String = "",
        alt: String = ""
    ): EfficacyEvidence {
        return createEfficacyEvidence(createHotspot(gene, chromosome, position, ref, alt))
    }

    fun createEfficacyEvidenceWithCodon(gene: String = ""): EfficacyEvidence {
        return createEfficacyEvidence(createCodon(gene))
    }

    fun createEfficacyEvidenceWithExon(): EfficacyEvidence {
        return createEfficacyEvidence(createExon())
    }

    fun createEfficacyEvidenceWithGene(geneEvent: GeneEvent = GeneEvent.ANY_MUTATION, gene: String = ""): EfficacyEvidence {
        return createEfficacyEvidence(createGene(gene, geneEvent))
    }

    fun createEfficacyEvidenceWithFusion(): EfficacyEvidence {
        return createEfficacyEvidence(createFusion())
    }

    fun createEfficacyEvidenceWithCharacteristic(type: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_STABLE): EfficacyEvidence {
        return createEfficacyEvidence(createCharacteristic(type))
    }

    fun createEfficacyEvidenceWithHla(): EfficacyEvidence {
        return createEfficacyEvidence(createHla())
    }

    fun createHotspot(
        gene: String = "",
        chromosome: String = "",
        position: Int = 0,
        ref: String = "",
        alt: String = ""
    ): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addHotspots(
            ImmutableActionableHotspot.builder().from(createActionableEvent()).gene(gene).chromosome(chromosome).position(position).ref(ref)
                .alt(alt).build()
        ).build()
    }

    fun createCodon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addCodons(
            ImmutableActionableRange.builder().from(createActionableEvent()).gene(gene).chromosome(chromosome).start(start).end(end)
                .applicableMutationType(applicableMutationType).build()
        ).build()
    }

    fun createExon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addExons(
            ImmutableActionableRange.builder().from(createActionableEvent()).gene(gene).chromosome(chromosome).start(start).end(end)
                .applicableMutationType(applicableMutationType).build()
        ).build()
    }

    fun createGene(gene: String = "", geneEvent: GeneEvent = GeneEvent.ANY_MUTATION, sourceEvent: String = ""): MolecularCriterium {
        return ImmutableMolecularCriterium.builder()
            .addGenes(
                ImmutableActionableGene.builder().from(createActionableEvent()).event(geneEvent).gene(gene).sourceEvent(sourceEvent).build()
            ).build()
    }

    fun createFusion(geneUp: String = "", geneDown: String = "", minExonUp: Int? = null, maxExonUp: Int? = null): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addFusions(
            ImmutableActionableFusion.builder().from(createActionableEvent()).geneUp(geneUp).geneDown(geneDown).minExonUp(minExonUp)
                .maxExonUp(maxExonUp).build()
        ).build()
    }

    fun createCharacteristic(type: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_STABLE): MolecularCriterium {
        return ImmutableMolecularCriterium.builder()
            .addCharacteristics(ImmutableActionableCharacteristic.builder().from(createActionableEvent()).type(type).build()).build()
    }

    fun createHla(): MolecularCriterium {
        return ImmutableMolecularCriterium.builder()
            .addHla(ImmutableActionableHLA.builder().from(createActionableEvent()).hlaAllele("").build()).build()
    }

    fun createActionableEvent(): ActionableEvent {
        return object : ActionableEvent {
            override fun sourceDate(): LocalDate {
                return LocalDate.of(2021, 2, 3)
            }

            override fun sourceEvent(): String {
                return ""
            }

            override fun sourceUrls(): Set<String> {
                return setOf("https://ckbhome.jax.org/profileResponse/advancedEvidenceFind?molecularProfileId=29716")
            }
        }
    }

    fun createEfficacyEvidence(
        molecularCriterium: MolecularCriterium,
        source: Knowledgebase = Knowledgebase.CKB,
        treatment: String = "treatment",
        direction: EvidenceDirection = EvidenceDirection.NO_BENEFIT,
        level: EvidenceLevel = EvidenceLevel.D,
        indication: Indication = ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("").doid("").build())
            .excludedSubTypes(emptySet()).build()
    ): EfficacyEvidence {
        return object : EfficacyEvidence() {
            override fun source(): Knowledgebase {
                return source
            }

            override fun molecularCriterium(): MolecularCriterium {
                return molecularCriterium
            }

            override fun treatment(): Treatment {
                return ImmutableTreatment.builder().name(treatment).build()
            }

            override fun indication(): Indication {
                return indication
            }

            override fun efficacyDescription(): String {
                return "efficacy evidence"
            }

            override fun evidenceYear(): Int {
                return 2021
            }

            override fun evidenceLevel(): EvidenceLevel {
                return level
            }

            override fun evidenceLevelDetails(): EvidenceLevelDetails {
                return EvidenceLevelDetails.GUIDELINE
            }

            override fun evidenceDirection(): EvidenceDirection {
                return direction
            }

            override fun urls(): Set<String> {
                return emptySet()
            }
        }
    }

    fun createActionableTrial(
        molecularCriteria: Set<MolecularCriterium>,
        source: Knowledgebase = Knowledgebase.CKB,
        treatment: String = "",
        indications: Set<Indication> = setOf(
            ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("").doid("").build())
                .excludedSubTypes(emptySet()).build()
        )
    ): ActionableTrial {
        return object : ActionableTrial() {
            override fun source(): Knowledgebase {
                return source
            }

            override fun nctId(): String {
                return "NCT00000001"
            }

            override fun title(): String {
                return ""
            }

            override fun acronym(): String {
                return ""
            }

            override fun countries(): Set<Country> {
                return setOf(ImmutableCountry.builder().name("country").build())
            }

            override fun therapyNames(): Set<String> {
                return setOf(treatment)
            }

            override fun genderCriterium(): GenderCriterium? {
                return null
            }

            override fun indications(): Set<Indication> {
                return indications
            }

            override fun anyMolecularCriteria(): Set<MolecularCriterium> {
                return molecularCriteria
            }

            override fun urls(): Set<String> {
                return setOf("https://clinicaltrials.gov/study/NCT00000001")
            }

            override fun evidenceYear(): Int {
                return 1960
            }
        }
    }
}
