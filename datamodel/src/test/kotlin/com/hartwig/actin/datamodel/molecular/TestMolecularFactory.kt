package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.orange.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.orange.driver.CodingContext
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.DisruptionType
import com.hartwig.actin.datamodel.molecular.orange.driver.ExtendedVariantDetails
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.orange.driver.RegionType
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType
import com.hartwig.actin.datamodel.molecular.orange.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.orange.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.Haplotype
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.HaplotypeFunction
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoGene
import com.hartwig.serve.datamodel.trial.ImmutableHospital
import java.time.LocalDate

object TestMolecularFactory {

    private val TODAY: LocalDate = LocalDate.now()
    private const val DAYS_SINCE_MOLECULAR_ANALYSIS = 5

    fun createMinimalTestMolecularRecord(): MolecularRecord {
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
        return MolecularHistory(listOf(createExhaustiveTestMolecularRecord(), TestPanelRecordFactory.empty()))
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
            microsatelliteEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
            homologousRepairEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
            tumorMutationalBurdenEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
            tumorMutationalLoadEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence()
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
        evidence = TestClinicalEvidenceFactory.withExternalEligibleTrial(
            TestClinicalEvidenceFactory.createExternalTrial(
                title = "A Phase 1/2 Randomized Study to Evaluate the Safety and Efficacy of treatment X Plus treatment Y in "
                        + "Combination With Investigational Agents Versus treatment X Plus treatment Y, as First-Line Treatment "
                        + "for Participants With Advanced Solid Tumor (acronym)",
                countries = setOf(
                    TestClinicalEvidenceFactory.createCountry(CountryName.BELGIUM, mapOf("Brussels" to emptySet())),
                    TestClinicalEvidenceFactory.createCountry(CountryName.GERMANY, mapOf("Berlin" to emptySet()))
                ),
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
        type = VariantType.SNV,
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
        event = "EML4 - ALK fusion",
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
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

    fun createExhaustiveTestDrivers(): Drivers {
        val proper = createProperTestDrivers()
        return proper.copy(
            copyNumbers = proper.copyNumbers + CopyNumber(
                isReportable = true,
                event = "MYC amp",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestClinicalEvidenceFactory.withExternalEligibleTrial(
                    TestClinicalEvidenceFactory.createExternalTrial(
                        title = "A Phase 1 Study of XYXYXY, a T-Cell-Redirecting Agent Targeting Z, for Advanced Prostate Cancer",
                        countries = setOf(
                            TestClinicalEvidenceFactory.createCountry(
                                CountryName.NETHERLANDS,
                                mapOf(
                                    "Nijmegen" to setOf(Hospital("Radbouc UMC", false)),
                                    "Amsterdam" to setOf(Hospital("AMC", false), Hospital("VUmc", false))
                                )
                            )
                        ),
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
            ) + CopyNumber(
                isReportable = false,
                event = "MET copy number",
                driverLikelihood = null,
                evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
                gene = "MET",
                type = CopyNumberType.NONE,
                minCopies = 6,
                maxCopies = 6,
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null
            ),
            homozygousDisruptions = proper.homozygousDisruptions + HomozygousDisruption(
                isReportable = true,
                event = "PTEN hom disruption",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
                gene = "PTEN",
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null
            ),
            disruptions = proper.disruptions + Disruption(
                isReportable = true,
                event = "PTEN disruption",
                driverLikelihood = DriverLikelihood.LOW,
                evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
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
                event = "EML4 - ALK fusion",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
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
                evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
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
            evidence = ClinicalEvidence(),
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
            evidence = ClinicalEvidence(),
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
            evidence = ClinicalEvidence(),
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
            evidence = ClinicalEvidence(),
        )
    }
}