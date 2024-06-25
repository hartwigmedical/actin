package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import com.hartwig.actin.molecular.datamodel.evidence.TestExternalTrialFactory
import com.hartwig.actin.molecular.datamodel.orange.characteristics.CupPrediction
import com.hartwig.actin.molecular.datamodel.orange.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.orange.driver.Disruption
import com.hartwig.actin.molecular.datamodel.orange.driver.DisruptionType
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusionDetails
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedVariantDetails
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.orange.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.orange.driver.RegionType
import com.hartwig.actin.molecular.datamodel.orange.driver.Virus
import com.hartwig.actin.molecular.datamodel.orange.driver.VirusType
import com.hartwig.actin.molecular.datamodel.orange.immunology.HlaAllele
import com.hartwig.actin.molecular.datamodel.orange.immunology.MolecularImmunology
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.Haplotype
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelType
import java.time.LocalDate

object TestMolecularFactory {

    private val TODAY: LocalDate = LocalDate.now()
    private const val DAYS_SINCE_MOLECULAR_ANALYSIS = 5

    fun createMinimalTestMolecularRecord(): MolecularRecord {
        return MolecularRecord(
            patientId = TestPatientFactory.TEST_PATIENT,
            sampleId = TestPatientFactory.TEST_SAMPLE,
            type = ExperimentType.WHOLE_GENOME,
            refGenomeVersion = RefGenomeVersion.V37,
            evidenceSource = "",
            externalTrialSource = "",
            containsTumorCells = true,
            isContaminated = false,
            hasSufficientPurity = true,
            hasSufficientQuality = true,
            characteristics = createMinimalTestCharacteristics(),
            drivers = createMinimalMolecularDrivers(),
            immunology = MolecularImmunology(isReliable = false, hlaAlleles = emptySet()),
            date = null,
            pharmaco = emptySet()
        )
    }

    fun createProperTestMolecularRecord(): MolecularRecord {
        return createMinimalTestMolecularRecord().copy(
            date = TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS.toLong()),
            evidenceSource = "kb",
            externalTrialSource = "trial kb",
            characteristics = createProperTestCharacteristics(),
            drivers = createProperTestDrivers(),
            immunology = createProperTestImmunology(),
            pharmaco = createProperTestPharmaco()
        )
    }

    fun createExhaustiveTestMolecularRecord(): MolecularRecord {
        return createProperTestMolecularRecord().copy(
            characteristics = createExhaustiveTestCharacteristics(),
            drivers = createExhaustiveTestDrivers()
        )
    }

    fun createMinimalTestMolecularHistory(): MolecularHistory {
        return MolecularHistory(listOf(createMinimalTestMolecularRecord()))
    }

    fun createProperTestMolecularHistory(): MolecularHistory {
        return MolecularHistory(listOf(createProperTestMolecularRecord()))
    }

    fun createExhaustiveTestMolecularHistory(): MolecularHistory {
        return MolecularHistory(listOf(createExhaustiveTestMolecularRecord()))
    }

    private fun createMinimalTestCharacteristics(): MolecularCharacteristics {
        return MolecularCharacteristics(null, null, null, null, null, null, null, null, null, null, null, null, null, null)
    }

    private fun createProperTestCharacteristics(): MolecularCharacteristics {
        return MolecularCharacteristics(
            purity = 0.98,
            ploidy = 3.1,
            predictedTumorOrigin = createProperPredictedTumorOrigin(),
            isMicrosatelliteUnstable = false,
            homologousRepairScore = 0.45,
            isHomologousRepairDeficient = false,
            tumorMutationalBurden = 13.71,
            hasHighTumorMutationalBurden = true,
            tumorMutationalBurdenEvidence = TestActionableEvidenceFactory.withApprovedTreatment("Pembro"),
            tumorMutationalLoad = 185,
            hasHighTumorMutationalLoad = true,
            microsatelliteEvidence = null,
            tumorMutationalLoadEvidence = null,
            homologousRepairEvidence = null
        )
    }

    private fun createProperPredictedTumorOrigin(): PredictedTumorOrigin {
        return PredictedTumorOrigin(
            predictions = listOf(
                CupPrediction(
                    cancerType = "Melanoma",
                    likelihood = 0.996,
                    snvPairwiseClassifier = 0.979,
                    genomicPositionClassifier = 0.99,
                    featureClassifier = 0.972,
                ),
                CupPrediction(
                    cancerType = "Lung",
                    likelihood = 0.001,
                    snvPairwiseClassifier = 0.0009,
                    genomicPositionClassifier = 0.011,
                    featureClassifier = 0.0102
                ),
                CupPrediction(
                    cancerType = "Esophagus/Stomach",
                    likelihood = 0.0016,
                    snvPairwiseClassifier = 0.0004,
                    genomicPositionClassifier = 0.006,
                    featureClassifier = 0.0002
                )
            )
        )
    }

    private fun createExhaustiveTestCharacteristics(): MolecularCharacteristics {
        return createProperTestCharacteristics().copy(
            microsatelliteEvidence = TestActionableEvidenceFactory.createExhaustive(),
            homologousRepairEvidence = TestActionableEvidenceFactory.createExhaustive(),
            tumorMutationalBurdenEvidence = TestActionableEvidenceFactory.createExhaustive(),
            tumorMutationalLoadEvidence = TestActionableEvidenceFactory.createExhaustive()
        )
    }

    private fun createMinimalMolecularDrivers() =
        Drivers(emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet())

    fun createProperTestDrivers(): Drivers {
        return createMinimalMolecularDrivers().copy(
            variants = setOf(
                createProperVariant()
            ),
            copyNumbers = setOf(
                createProperCopyNumber()
            )
        )
    }

    fun createProperCopyNumber() = CopyNumber(
        isReportable = true,
        event = "PTEN del",
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = TestActionableEvidenceFactory.withExternalEligibleTrial(
            TestExternalTrialFactory.create(
                title = "A Phase 1/2 Randomized Study to Evaluate the Safety and Efficacy of treatment X Plus treatment Y in "
                        + "Combination With Investigational Agents Versus treatment X Plus treatment Y, as First-Line Treatment "
                        + "for Participants With Advanced Solid Tumor (acronym)",
                countries = setOf(Country.BELGIUM, Country.GERMANY),
                url = "https://clinicaltrials.gov/study/NCT00000002",
                nctId = "NCT00000020"
            )
        ),
        gene = "PTEN",
        geneRole = GeneRole.TSG,
        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
        type = CopyNumberType.LOSS,
        minCopies = 0,
        maxCopies = 0,
        isAssociatedWithDrugResistance = null
    )

    fun createProperVariant() = Variant(
        chromosome = "7",
        position = 140453136,
        ref = "T",
        alt = "A",
        isReportable = true,
        event = "BRAF V600E",
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = TestActionableEvidenceFactory.withApprovedTreatment("Vemurafenib"),
        gene = "BRAF",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        isAssociatedWithDrugResistance = true,
        type = VariantType.SNV,
        extendedVariantDetails = ExtendedVariantDetails(
            variantCopyNumber = 4.1,
            totalCopyNumber = 6.0,
            isBiallelic = false,
            otherImpacts = emptySet(),
            phaseGroups = null,
            clonalLikelihood = 1.0
        ),
        isHotspot = true,
        canonicalImpact = TranscriptImpact(
            transcriptId = "ENST00000288602",
            hgvsCodingImpact = "c.1799T>A",
            hgvsProteinImpact = "p.V600E",
            affectedCodon = 600,
            isSpliceRegion = false,
            effects = setOf(VariantEffect.MISSENSE),
            codingEffect = CodingEffect.MISSENSE,
            affectedExon = null
        )
    )

    private fun createProperTestImmunology(): MolecularImmunology {
        return MolecularImmunology(
            isReliable = true,
            hlaAlleles = setOf(HlaAllele(name = "A*02:01", tumorCopyNumber = 1.2, hasSomaticMutations = false)),
        )
    }

    private fun createProperTestPharmaco(): Set<PharmacoEntry> {
        return setOf(
            PharmacoEntry(
                gene = "DPYD",
                haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = "Normal function")),
            ),
            PharmacoEntry(
                gene = "UGT1A1",
                haplotypes = setOf(
                    Haplotype(allele = "*1", alleleCount = 1, function = "Normal function"),
                    Haplotype(allele = "*28", alleleCount = 1, function = "Reduced function"),
                )
            )
        )
    }

    private fun createExhaustiveTestDrivers(): Drivers {
        val proper = createProperTestDrivers()
        return proper.copy(
            copyNumbers = proper.copyNumbers + CopyNumber(
                isReportable = true,
                event = "MYC amp",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestActionableEvidenceFactory.withExternalEligibleTrial(
                    TestExternalTrialFactory.create(
                        title = "A Phase 1 Study of XYXYXY, a T-Cell-Redirecting Agent Targeting Z, for Advanced Prostate Cancer",
                        countries = setOf(Country.NETHERLANDS),
                        url = "https://clinicaltrials.gov/study/NCT00000003",
                        nctId = "NCT00000003",
                    )
                ),
                gene = "MYC",
                type = CopyNumberType.FULL_GAIN,
                minCopies = 38,
                maxCopies = 38,
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null
            ),
            homozygousDisruptions = proper.homozygousDisruptions + HomozygousDisruption(
                isReportable = true,
                event = "PTEN hom disruption",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestActionableEvidenceFactory.createExhaustive(),
                gene = "PTEN",
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null
            ),
            disruptions = proper.disruptions + Disruption(
                isReportable = true,
                event = "PTEN disruption",
                driverLikelihood = DriverLikelihood.LOW,
                evidence = TestActionableEvidenceFactory.createExhaustive(),
                gene = "PTEN",
                type = DisruptionType.DEL,
                junctionCopyNumber = 1.1,
                undisruptedCopyNumber = 1.8,
                regionType = RegionType.EXONIC,
                codingContext = CodingContext.CODING,
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null,
                clusterGroup = 0
            ),
            fusions = proper.fusions + Fusion(
                isReportable = true,
                event = "EML4 - ALK fusion",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestActionableEvidenceFactory.createExhaustive(),
                geneStart = "EML4",
                geneTranscriptStart = "ENST00000318522",
                geneEnd = "ALK",
                geneTranscriptEnd = "ENST00000389048",
                proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                driverType = FusionDriverType.KNOWN_PAIR,
                extendedFusionDetails = ExtendedFusionDetails(
                    fusedExonUp = 6,
                    fusedExonDown = 20,
                    isAssociatedWithDrugResistance = null
                )
            ),
            viruses = proper.viruses + Virus(
                isReportable = true,
                event = "HPV positive",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestActionableEvidenceFactory.createExhaustive(),
                name = "Human papillomavirus type 16",
                type = VirusType.HUMAN_PAPILLOMA_VIRUS,
                integrations = 3,
                isReliable = true,
            )
        )
    }

    fun freeTextPriorMolecularFusionRecord(geneStart: String, geneEnd: String) = TestPanelRecordFactory.empty().copy(
        panelExtraction =
        GenericPanelExtraction(
            fusions = listOf(GenericFusionExtraction(geneStart, geneEnd)),
            panelType = GenericPanelType.FREE_TEXT
        )
    )
}

