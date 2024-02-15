package com.hartwig.actin.molecular.evidence

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
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.molecular.datamodel.driver.Virus
import com.hartwig.actin.molecular.datamodel.driver.VirusType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence

object TestMolecularFactory {

    fun minimalVariant(): Variant {
        return Variant(
            chromosome = "",
            position = 0,
            ref = "",
            alt = "",
            isReportable = true,
            type = VariantType.SNV,
            gene = "",
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

    fun minimalFusion(): Fusion {
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

    fun minimalDisruption(): Disruption {
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

    fun minimalCopyNumber(): CopyNumber {
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

    fun minimalHomozygousDisruption(): HomozygousDisruption {
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

    fun minimalVirus(): Virus {
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