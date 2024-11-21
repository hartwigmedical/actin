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

    fun createHotspot(
        gene: String = "",
        chromosome: String = "",
        position: Int = 0,
        ref: String = "",
        alt: String = ""
    ): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addHotspots(
            ImmutableActionableHotspot.builder().from(actionableEventBuiler()).gene(gene).chromosome(chromosome).position(position).ref(ref)
                .alt(alt).build()
        ).build()
    }

    fun withHotspot(
        gene: String = "",
        chromosome: String = "",
        position: Int = 0,
        ref: String = "",
        alt: String = "",
        intervention: String = "intervention",
        source: Knowledgebase = Knowledgebase.CKB
    ): EfficacyEvidence {
        return createEfficacyEvidence(source, intervention, molecularCriterium = createHotspot(gene, chromosome, position, ref, alt))
    }

    fun createCodon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addCodons(
            ImmutableActionableRange.builder().from(actionableEventBuiler()).gene(gene).chromosome(chromosome).start(start).end(end)
                .applicableMutationType(applicableMutationType).build()
        ).build()
    }

    fun withCodon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY,
        intervention: String = "intervention"
    ): EfficacyEvidence {
        return createEfficacyEvidence(
            Knowledgebase.CKB,
            intervention,
            molecularCriterium = createCodon(gene, chromosome, start, end, applicableMutationType)
        )
    }

    fun createExon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addExons(
            ImmutableActionableRange.builder().from(actionableEventBuiler()).gene(gene).chromosome(chromosome).start(start).end(end)
                .applicableMutationType(applicableMutationType).build()
        ).build()
    }

    fun withExon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY,
        intervention: String = "intervention"
    ): EfficacyEvidence {
        return createEfficacyEvidence(
            Knowledgebase.CKB,
            intervention,
            molecularCriterium = createExon(gene, chromosome, start, end, applicableMutationType)
        )
    }

    fun createGene(gene: String = "", geneEvent: GeneEvent = GeneEvent.ANY_MUTATION): MolecularCriterium {
        return ImmutableMolecularCriterium.builder()
            .addGenes(ImmutableActionableGene.builder().from(actionableEventBuiler()).event(geneEvent).gene(gene).build()).build()
    }

    fun withGene(
        geneEvent: GeneEvent = GeneEvent.ANY_MUTATION,
        gene: String = "",
        intervention: String = "intervention"
    ): EfficacyEvidence {
        return createEfficacyEvidence(Knowledgebase.CKB, intervention, molecularCriterium = createGene(gene, geneEvent))
    }

    fun createFusion(geneUp: String = "", geneDown: String = "", minExonUp: Int? = null, maxExonUp: Int? = null): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addFusions(
            ImmutableActionableFusion.builder().from(actionableEventBuiler()).geneUp(geneUp).geneDown(geneDown).minExonUp(minExonUp)
                .maxExonUp(maxExonUp).build()
        ).build()
    }

    fun withFusion(
        geneUp: String = "",
        geneDown: String = "",
        minExonUp: Int? = null,
        maxExonUp: Int? = null,
        intervention: String = "intervention"
    ): EfficacyEvidence {
        return createEfficacyEvidence(
            Knowledgebase.CKB,
            intervention,
            molecularCriterium = createFusion(geneUp, geneDown, minExonUp, maxExonUp)
        )
    }

    fun createCharacteristic(type: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_STABLE): MolecularCriterium {
        return ImmutableMolecularCriterium.builder()
            .addCharacteristics(ImmutableActionableCharacteristic.builder().from(actionableEventBuiler()).type(type).build()).build()
    }

    fun withCharacteristic(
        type: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_STABLE,
        intervention: String = "intervention"
    ): EfficacyEvidence {
        return createEfficacyEvidence(Knowledgebase.CKB, intervention, molecularCriterium = createCharacteristic(type))
    }

    fun createHla(): MolecularCriterium {
        return ImmutableMolecularCriterium.builder()
            .addHla(ImmutableActionableHLA.builder().from(actionableEventBuiler()).hlaAllele("").build()).build()
    }

    fun withHla(intervention: String = "intervention"): EfficacyEvidence {
        return createEfficacyEvidence(Knowledgebase.CKB, intervention, molecularCriterium = createHla())
    }

    fun actionableEventBuiler(): ActionableEvent {
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
        source: Knowledgebase = Knowledgebase.CKB,
        interventionName: String = "intervention",
        direction: EvidenceDirection = EvidenceDirection.NO_BENEFIT,
        level: EvidenceLevel = EvidenceLevel.D,
        molecularCriterium: MolecularCriterium,
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
                return ImmutableTreatment.builder().name(interventionName).build()
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
        source: Knowledgebase = Knowledgebase.CKB,
        interventionName: String,
        molecularCriterium: MolecularCriterium,
        indication: Indication = ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("").doid("").build())
            .excludedSubTypes(emptySet()).build()
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
                return interventionName
            }

            override fun countries(): Set<Country> {
                return setOf(ImmutableCountry.builder().name("country").build())
            }

            override fun therapyNames(): Set<String> {
                return setOf(interventionName)
            }

            override fun genderCriterium(): GenderCriterium? {
                return null
            }

            override fun indications(): Set<Indication> {
                return setOf(indication)
            }

            override fun anyMolecularCriteria(): Set<MolecularCriterium> {
                return setOf(molecularCriterium)
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
