package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceDirection
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel
import com.hartwig.serve.datamodel.efficacy.EvidenceLevelDetails
import com.hartwig.serve.datamodel.efficacy.ImmutableEfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.ImmutableTreatment
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.hotspot.VariantAnnotation

object TestServeEvidenceFactory {

    fun createEvidenceForHotspot(vararg variants: VariantAnnotation): EfficacyEvidence {
        return create(
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
                variants = if (variants.isNotEmpty()) variants.toSet() else setOf(TestServeMolecularFactory.createVariantAnnotation())
            )
        )
    }

    fun createEvidenceForCodon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): EfficacyEvidence {
        return create(
            molecularCriterium = TestServeMolecularFactory.createCodonCriterium(
                gene = gene,
                chromosome = chromosome,
                start = start,
                end = end,
                applicableMutationType = applicableMutationType
            )
        )
    }

    fun createEvidenceForExon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): EfficacyEvidence {
        return create(
            molecularCriterium = TestServeMolecularFactory.createExonCriterium(
                gene = gene,
                chromosome = chromosome,
                start = start,
                end = end,
                applicableMutationType = applicableMutationType
            )
        )
    }

    fun createEvidenceForGene(gene: String = "", geneEvent: GeneEvent = GeneEvent.ANY_MUTATION): EfficacyEvidence {
        return create(molecularCriterium = TestServeMolecularFactory.createGeneCriterium(gene = gene, geneEvent = geneEvent))
    }

    fun createEvidenceForFusion(
        geneUp: String = "",
        geneDown: String = "",
        minExonUp: Int? = null,
        maxExonUp: Int? = null
    ): EfficacyEvidence {
        return create(
            molecularCriterium = TestServeMolecularFactory.createFusionCriterium(
                geneUp = geneUp,
                geneDown = geneDown,
                minExonUp = minExonUp,
                maxExonUp = maxExonUp
            )
        )
    }

    fun createEvidenceForCharacteristic(type: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_STABLE): EfficacyEvidence {
        return create(molecularCriterium = TestServeMolecularFactory.createCharacteristicCriterium(type = type))
    }

    fun createEvidenceForHla(): EfficacyEvidence {
        return create(molecularCriterium = TestServeMolecularFactory.createHlaCriterium())
    }

    fun create(
        source: Knowledgebase = ActionabilityConstants.EVIDENCE_SOURCE,
        treatment: String = "treatment",
        indication: Indication = TestServeFactory.createEmptyIndication(),
        molecularCriterium: MolecularCriterium = TestServeMolecularFactory.createHotspotCriterium(),
        evidenceLevel: EvidenceLevel = EvidenceLevel.D,
        evidenceLevelDetails: EvidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
        evidenceDirection: EvidenceDirection = EvidenceDirection.NO_BENEFIT
    ): EfficacyEvidence {
        return ImmutableEfficacyEvidence.builder()
            .source(source)
            .treatment(ImmutableTreatment.builder().name(treatment).build())
            .indication(indication)
            .molecularCriterium(molecularCriterium)
            .efficacyDescription("efficacy description")
            .evidenceLevel(evidenceLevel)
            .evidenceLevelDetails(evidenceLevelDetails)
            .evidenceDirection(evidenceDirection)
            .evidenceYear(2021)
            .urls(emptySet())
            .build()
    }
}
