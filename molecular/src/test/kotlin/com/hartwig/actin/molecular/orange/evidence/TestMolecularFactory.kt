package com.hartwig.actin.molecular.orange.evidence

import com.hartwig.actin.molecular.datamodel.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.DisruptionType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.RegionType
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.molecular.datamodel.driver.Virus
import com.hartwig.actin.molecular.datamodel.driver.VirusType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

object TestMolecularFactory {
    fun completeTestVariant(): Variant {

        return Variant(
            isReportable = true,
            event = "BRAF V600E",
            driverLikelihood = DriverLikelihood.HIGH,
            evidence = ActionableEvidence(approvedTreatments = setOf("Vemurafenib")),
            gene = "BRAF",
            geneRole = GeneRole.ONCO,
            proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
            isAssociatedWithDrugResistance = true,
            type = VariantType.SNV,
            variantCopyNumber = 4.1,
            totalCopyNumber = 6.0,
            isBiallelic = false,
            isHotspot = true,
            clonalLikelihood = 1.0,
            canonicalImpact = TranscriptImpact(
                transcriptId = "ENST00000288602",
                hgvsCodingImpact = "c.1799T>A",
                hgvsProteinImpact = "p.V600E",
                affectedCodon = 600,
                isSpliceRegion = false,
                effects = setOf(VariantEffect.MISSENSE),
                codingEffect = CodingEffect.MISSENSE,
                affectedExon = null
            ),
            otherImpacts = emptySet(),
            phaseGroups = null,
            chromosome = "7",
            position = 140453136,
            ref = "T",
            alt = "A"
        )
    }

    fun minimalTestVariant(): Variant {
        return Variant(
            isReportable = true,
            type = VariantType.SNV,
            gene = "",
            chromosome = "",
            position = 0,
            ref = "",
            alt = "",
            variantCopyNumber = 0.0,
            totalCopyNumber = 0.0,
            isHotspot = false,
            clonalLikelihood = 1.0,
            isBiallelic = false,
            canonicalImpact = minimalTranscriptImpact(),
            proteinEffect = ProteinEffect.UNKNOWN,
            driverLikelihood = DriverLikelihood.LOW,
            evidence = ActionableEvidence(),
            geneRole = GeneRole.UNKNOWN,
            event = "",
            isAssociatedWithDrugResistance = false,
            otherImpacts = emptySet(),
            phaseGroups = null

        )
    }

    fun minimalTranscriptImpact(): TranscriptImpact {
        return TranscriptImpact(
            transcriptId = "",
            hgvsCodingImpact = "",
            hgvsProteinImpact = "",
            affectedCodon = 0,
            isSpliceRegion = false,
            effects = emptySet(),
            codingEffect = CodingEffect.NONE,
            affectedExon = null
        )
    }

    fun minimalTestFusion(): Fusion {
        return Fusion(
            geneStart = "",
            geneTranscriptStart = "",
            fusedExonUp = 0,
            geneEnd = "",
            geneTranscriptEnd = "",
            fusedExonDown = 0,
            driverType = FusionDriverType.NONE,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = false,
            isReportable = true,
            event = "",
            driverLikelihood = DriverLikelihood.LOW,
            evidence = ActionableEvidence()
        )
    }

    fun minimalTestDisruption(): Disruption {
        return Disruption(
            type = DisruptionType.INS,
            junctionCopyNumber = 0.0,
            undisruptedCopyNumber = 0.0,
            regionType = RegionType.INTRONIC,
            codingContext = CodingContext.NON_CODING,
            clusterGroup = 0,
            isReportable = false,
            event = "",
            driverLikelihood = DriverLikelihood.LOW,
            evidence = ActionableEvidence(),
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = false
        )
    }

    fun minimalTestCopyNumber(): CopyNumber {
        return CopyNumber(
            type = CopyNumberType.NONE,
            minCopies = 0,
            maxCopies = 0,
            isReportable = false,
            isAssociatedWithDrugResistance = false,
            event = "",
            driverLikelihood = DriverLikelihood.LOW,
            evidence = ActionableEvidence(),
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN
        )
    }

    fun minimalTestHomozygousDisruption(): HomozygousDisruption {
        return HomozygousDisruption(
            isReportable = false,
            event = "",
            driverLikelihood = DriverLikelihood.LOW,
            evidence = ActionableEvidence(),
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = false
        )
    }

    fun minimalTestVirus(): Virus {
        return Virus(
            name = "",
            type = VirusType.OTHER,
            isReliable = false,
            integrations = 0,
            isReportable = false,
            event = "",
            driverLikelihood = DriverLikelihood.LOW,
            evidence = ActionableEvidence(),
        )
    }
}