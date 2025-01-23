package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.datamodel.molecular.orange.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.driver.CodingContext
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.DisruptionType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.ExtendedVariantDetails
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.RegionType
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.pharmaco.Haplotype
import com.hartwig.actin.datamodel.molecular.pharmaco.HaplotypeFunction
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene
import java.time.LocalDate

object TestMolecularFactory {

    private val TODAY = LocalDate.now()
    private const val DAYS_SINCE_MOLECULAR_ANALYSIS = 5

    fun createMinimalTestMolecularHistory(): MolecularHistory {
        return MolecularHistory(listOf(createMinimalTestOrangeRecord(), createMinimalTestPanelRecord()))
    }

    fun createProperTestMolecularHistory(): MolecularHistory {
        return MolecularHistory(listOf(createProperTestOrangeRecord(), createProperTestPanelRecord()))
    }

    fun createExhaustiveTestMolecularHistory(): MolecularHistory {
        return MolecularHistory(listOf(createExhaustiveTestOrangeRecord(), createExhaustiveTestPanelRecord()))
    }

    fun createMinimalTestPanelRecord(): PanelRecord {
        return PanelRecord(
            testedGenes = emptySet(),
            experimentType = ExperimentType.PANEL,
            testTypeDisplay = "minimal panel",
            date = null,
            drivers = Drivers(),
            characteristics = MolecularCharacteristics(),
            evidenceSource = "",
            hasSufficientPurity = true,
            hasSufficientQuality = true
        )
    }

    fun createMinimalTestOrangeRecord(): MolecularRecord {
        return MolecularRecord(
            patientId = TestPatientFactory.TEST_PATIENT,
            sampleId = TestPatientFactory.TEST_SAMPLE,
            experimentType = ExperimentType.HARTWIG_WHOLE_GENOME,
            refGenomeVersion = RefGenomeVersion.V37,
            evidenceSource = "",
            externalTrialSource = "",
            containsTumorCells = true,
            isContaminated = false,
            hasSufficientPurity = true,
            hasSufficientQuality = true,
            drivers = Drivers(),
            characteristics = createMinimalTestCharacteristics(),
            immunology = MolecularImmunology(isReliable = false, hlaAlleles = emptySet()),
            date = null,
            pharmaco = emptySet()
        )
    }

    fun createProperTestPanelRecord(): PanelRecord {
        return createMinimalTestPanelRecord().copy(
            testedGenes = setOf("BRAF", "PTEN"),
            testTypeDisplay = "proper panel",
            date = TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS.toLong()),
            drivers = createProperTestDrivers(),
            characteristics = createProperTestCharacteristics(),
            evidenceSource = "kb",
        )
    }

    fun createProperTestOrangeRecord(): MolecularRecord {
        return createMinimalTestOrangeRecord().copy(
            date = TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS.toLong()),
            evidenceSource = "kb",
            externalTrialSource = "trial kb",
            drivers = createProperTestDrivers(),
            characteristics = createProperTestCharacteristics(),
            immunology = createProperTestImmunology(),
            pharmaco = createProperTestPharmaco()
        )
    }

    fun createExhaustiveTestPanelRecord(): PanelRecord {
        return createProperTestPanelRecord().copy(
            testedGenes = setOf("BRAF", "PTEN", "MYC", "MET", "EML4", "ALK"),
            testTypeDisplay = "exhaustive panel",
            drivers = createExhaustiveTestDrivers(),
            characteristics = createExhaustiveTestCharacteristics()
        )
    }

    fun createExhaustiveTestOrangeRecord(): MolecularRecord {
        return createProperTestOrangeRecord().copy(
            drivers = createExhaustiveTestDrivers(),
            characteristics = createExhaustiveTestCharacteristics()
        )
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
            tumorMutationalBurdenEvidence = TestClinicalEvidenceFactory.withApprovedTreatment("Pembro"),
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
            microsatelliteEvidence = TestClinicalEvidenceFactory.createExhaustive(),
            homologousRepairEvidence = TestClinicalEvidenceFactory.createExhaustive(),
            tumorMutationalBurdenEvidence = TestClinicalEvidenceFactory.createExhaustive(),
            tumorMutationalLoadEvidence = TestClinicalEvidenceFactory.createExhaustive()
        )
    }

    fun createProperTestDrivers(): Drivers {
        return Drivers(
            variants = listOf(createProperVariant()),
            copyNumbers = listOf(createProperCopyNumber())
        )
    }

    fun createProperCopyNumber() = CopyNumber(
        isReportable = true,
        event = "PTEN del",
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = TestClinicalEvidenceFactory.withEligibleTrial(
            TestExternalTrialFactory.create(
                nctId = "NCT00000020",
                title = "A Phase 1/2 Randomized Study to Evaluate the Safety and Efficacy of treatment X Plus treatment Y in "
                        + "Combination With Investigational Agents Versus treatment X Plus treatment Y, as First-Line Treatment "
                        + "for Participants With Advanced Solid Tumor (acronym)",
                countries = setOf(
                    CountryDetails(Country.BELGIUM, mapOf("Brussels" to emptySet())),
                    CountryDetails(Country.GERMANY, mapOf("Berlin" to emptySet()))
                ),
                url = "https://clinicaltrials.gov/study/NCT00000020"
            )
        ),
        gene = "PTEN",
        geneRole = GeneRole.TSG,
        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS),
        otherImpacts = emptySet(),
        isAssociatedWithDrugResistance = null
    )

    fun createProperVariant() = Variant(
        chromosome = "7",
        position = 140453136,
        ref = "T",
        alt = "A",
        type = VariantType.SNV,
        canonicalImpact = TranscriptVariantImpact(
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
        extendedVariantDetails = ExtendedVariantDetails(
            variantCopyNumber = 4.1,
            totalCopyNumber = 6.0,
            isBiallelic = false,
            phaseGroups = null,
            clonalLikelihood = 1.0
        ),
        isHotspot = true,
        isReportable = true,
        event = "BRAF V600E",
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = TestClinicalEvidenceFactory.withApprovedTreatment("Vemurafenib"),
        gene = "BRAF",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        isAssociatedWithDrugResistance = true,
    )

    fun createProperFusion() = Fusion(
        isReportable = true,
        event = "EML4::ALK fusion",
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = TestClinicalEvidenceFactory.createExhaustive(),
        geneStart = "EML4",
        geneEnd = "ALK",
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        driverType = FusionDriverType.KNOWN_PAIR,
        isAssociatedWithDrugResistance = null,
        geneTranscriptStart = "ENST00000318522",
        geneTranscriptEnd = "ENST00000389048",
        fusedExonUp = 6,
        fusedExonDown = 20,
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
                gene = PharmacoGene.DPYD,
                haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = HaplotypeFunction.NORMAL_FUNCTION)),
            ),
            PharmacoEntry(
                gene = PharmacoGene.UGT1A1,
                haplotypes = setOf(
                    Haplotype(allele = "*1", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION),
                    Haplotype(allele = "*28", alleleCount = 1, function = HaplotypeFunction.REDUCED_FUNCTION),
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
                evidence = TestClinicalEvidenceFactory.withEligibleTrials(
                    setOf(
                        TestExternalTrialFactory.create(
                            nctId = "NCT00000003",
                            title = "A Phase 1 Study of XYXYXY, a T-Cell-Redirecting Agent Targeting Z, for Advanced Prostate Cancer",
                            countries = setOf(
                                CountryDetails(
                                    Country.NETHERLANDS,
                                    mapOf(
                                        "Nijmegen" to setOf(Hospital("Radboud UMC", false)),
                                        "Amsterdam" to setOf(Hospital("AMC", false), Hospital("VUmc", false))
                                    )
                                )
                            ),
                            url = "https://clinicaltrials.gov/study/NCT00000003"
                        ),
                        TestExternalTrialFactory.create(
                            nctId = "NCT00000011",
                            title = "this trial should be filtered out",
                            countries = setOf(
                                CountryDetails(
                                    Country.BELGIUM,
                                    mapOf(
                                        "Leuven" to setOf(Hospital("hospital", null))
                                    )
                                )
                            ),
                            url = "https://clinicaltrials.gov/study/NCT00000011"
                        )
                    )
                ),
                gene = "MYC",
                canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, 38, 38),
                otherImpacts = emptySet(),
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null
            ) + CopyNumber(
                isReportable = false,
                event = "MET copy number",
                driverLikelihood = null,
                evidence = TestClinicalEvidenceFactory.createExhaustive(),
                gene = "MET",
                canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(minCopies = 6, maxCopies = 6),
                otherImpacts = emptySet(),
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null
            ),
            homozygousDisruptions = proper.homozygousDisruptions + HomozygousDisruption(
                isReportable = true,
                event = "PTEN hom disruption",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestClinicalEvidenceFactory.createExhaustive(),
                gene = "PTEN",
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null
            ),
            disruptions = proper.disruptions + Disruption(
                isReportable = true,
                event = "PTEN disruption",
                driverLikelihood = DriverLikelihood.LOW,
                evidence = TestClinicalEvidenceFactory.createExhaustive(),
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
                isReportable = false,
                event = "EML4::ALK fusion",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestClinicalEvidenceFactory.createExhaustive(),
                geneStart = "EML4",
                geneEnd = "ALK",
                proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                driverType = FusionDriverType.KNOWN_PAIR,
                isAssociatedWithDrugResistance = null,
                geneTranscriptStart = "ENST00000318522",
                geneTranscriptEnd = "ENST00000389048",
                fusedExonUp = 6,
                fusedExonDown = 20,
            ),
            viruses = proper.viruses + Virus(
                isReportable = true,
                event = "HPV positive",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestClinicalEvidenceFactory.createExhaustive(),
                name = "Human papillomavirus type 16",
                type = VirusType.HUMAN_PAPILLOMA_VIRUS,
                integrations = 3,
                isReliable = true,
            )
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
            evidence = TestClinicalEvidenceFactory.createEmpty(),
            gene = "",
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = false
        )
    }

    fun minimalCopyNumber(): CopyNumber {
        return CopyNumber(
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(),
            otherImpacts = emptySet(),
            isReportable = false,
            isAssociatedWithDrugResistance = false,
            event = "",
            driverLikelihood = DriverLikelihood.LOW,
            evidence = TestClinicalEvidenceFactory.createEmpty(),
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
            evidence = TestClinicalEvidenceFactory.createEmpty(),
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
            evidence = TestClinicalEvidenceFactory.createEmpty()
        )
    }
}