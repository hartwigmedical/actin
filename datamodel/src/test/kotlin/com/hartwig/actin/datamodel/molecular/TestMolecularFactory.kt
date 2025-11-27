package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.molecular.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.characteristics.CuppaMode
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.driver.CodingContext
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.DisruptionType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.RegionType
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.Hospital
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.datamodel.molecular.pharmaco.Haplotype
import com.hartwig.actin.datamodel.molecular.pharmaco.HaplotypeFunction
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene
import java.time.LocalDate

object TestMolecularFactory {

    private val TODAY = LocalDate.now()
    private const val DAYS_SINCE_MOLECULAR_ANALYSIS = 5

    fun createMinimalMolecularTests(): List<MolecularTest> {
        return listOf(createMinimalWholeGenomeTest(), createMinimalPanelTest())
    }

    fun createProperMolecularTests(): List<MolecularTest> {
        return listOf(createProperWholeGenomeTest(), createProperPanelTest())
    }

    fun createExhaustiveMolecularTests(): List<MolecularTest> {
        return listOf(createExhaustiveWholeGenomeTest(), createExhaustivePanelTest())
    }

    fun createMinimalPanelTest(): MolecularTest {
        return MolecularTest(
            date = null,
            sampleId = TestPatientFactory.TEST_SAMPLE,
            reportHash = null,
            experimentType = ExperimentType.PANEL,
            testTypeDisplay = "minimal panel",
            targetSpecification = PanelTargetSpecification(emptyMap()),
            refGenomeVersion = RefGenomeVersion.V37,
            containsTumorCells = true,
            hasSufficientPurity = true,
            hasSufficientQuality = true,
            isContaminated = false,
            drivers = createMinimalTestDrivers(),
            characteristics = createMinimalTestCharacteristics(),
            immunology = null,
            pharmaco = emptySet(),
            evidenceSource = "",
            externalTrialSource = ""
        )
    }

    fun createMinimalWholeGenomeTest(): MolecularTest {
        return MolecularTest(
            date = null,
            sampleId = TestPatientFactory.TEST_SAMPLE,
            reportHash = null,
            experimentType = ExperimentType.HARTWIG_WHOLE_GENOME,
            testTypeDisplay = "minimal whole genome",
            targetSpecification = null,
            refGenomeVersion = RefGenomeVersion.V37,
            containsTumorCells = true,
            hasSufficientPurity = true,
            hasSufficientQuality = true,
            isContaminated = false,
            drivers = createMinimalTestDrivers(),
            characteristics = createMinimalTestCharacteristics(),
            immunology = createMinimalTestImmunology(),
            pharmaco = emptySet(),
            evidenceSource = "",
            externalTrialSource = ""
        )
    }

    fun createProperPanelTest(): MolecularTest {
        return createMinimalPanelTest().copy(
            date = TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS.toLong()),
            testTypeDisplay = "proper panel",
            targetSpecification = PanelTargetSpecification(
                mapOf(
                    "BRAF" to listOf(MolecularTestTarget.MUTATION),
                    "PTEN" to listOf(MolecularTestTarget.MUTATION)
                )
            ),
            drivers = createProperTestDrivers(),
            characteristics = createProperTestCharacteristics(),
            evidenceSource = "kb",
        )
    }

    fun createProperWholeGenomeTest(): MolecularTest {
        return createMinimalWholeGenomeTest().copy(
            date = TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS.toLong()),
            testTypeDisplay = "proper whole genome",
            drivers = createProperTestDrivers(),
            characteristics = createProperTestCharacteristics(),
            immunology = createProperTestImmunology(),
            pharmaco = createProperTestPharmaco(),
            evidenceSource = "kb",
            externalTrialSource = "trial kb"
        )
    }

    fun createExhaustivePanelTest(): MolecularTest {
        return createProperPanelTest().copy(
            testTypeDisplay = "exhaustive panel",
            targetSpecification = PanelTargetSpecification(
                setOf(
                    "BRAF",
                    "PTEN",
                    "MYC",
                    "MET",
                    "EML4",
                    "ALK"
                ).associateWith { listOf(MolecularTestTarget.MUTATION) }),
            drivers = createExhaustiveTestDrivers(),
            characteristics = createExhaustiveTestCharacteristics()
        )
    }

    fun createExhaustiveWholeGenomeTest(): MolecularTest {
        return createProperWholeGenomeTest().copy(
            drivers = createExhaustiveTestDrivers(),
            characteristics = createExhaustiveTestCharacteristics()
        )
    }

    fun createMinimalTestCharacteristics(): MolecularCharacteristics {
        return MolecularCharacteristics(
            purity = null,
            ploidy = null,
            predictedTumorOrigin = null,
            microsatelliteStability = null,
            homologousRecombination = null,
            tumorMutationalBurden = null,
            tumorMutationalLoad = null
        )
    }

    fun createMinimalTestImmunology(): MolecularImmunology {
        return MolecularImmunology(isReliable = false, hlaAlleles = emptySet())
    }

    fun panelSpecifications(genes: Set<String>, targets: List<MolecularTestTarget> = listOf(MolecularTestTarget.MUTATION)) =
        PanelTargetSpecification(genes.associateWith { targets })

    private fun createProperTestCharacteristics(): MolecularCharacteristics {
        return MolecularCharacteristics(
            purity = 0.98,
            ploidy = 3.1,
            predictedTumorOrigin = createProperPredictedTumorOrigin(),
            microsatelliteStability = createProperMicrosatelliteStability(),
            homologousRecombination = createProperHomologousRecombination(),
            tumorMutationalBurden = createProperTumorMutationalBurden(),
            tumorMutationalLoad = createProperTumorMutationalLoad()
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
                    cuppaMode = CuppaMode.WGS
                ), CupPrediction(
                    cancerType = "Lung",
                    likelihood = 0.001,
                    snvPairwiseClassifier = 0.0009,
                    genomicPositionClassifier = 0.011,
                    featureClassifier = 0.0102,
                    cuppaMode = CuppaMode.WGS
                ), CupPrediction(
                    cancerType = "Esophagus/Stomach",
                    likelihood = 0.0016,
                    snvPairwiseClassifier = 0.0004,
                    genomicPositionClassifier = 0.006,
                    featureClassifier = 0.0002,
                    cuppaMode = CuppaMode.WGS
                )
            )
        )
    }

    private fun createProperMicrosatelliteStability(): MicrosatelliteStability {
        return MicrosatelliteStability(
            microsatelliteIndelsPerMb = 0.0,
            isUnstable = false,
            evidence = TestClinicalEvidenceFactory.createEmpty()
        )
    }

    private fun createProperHomologousRecombination(): HomologousRecombination {
        return HomologousRecombination(
            isDeficient = false,
            score = 0.45,
            type = HomologousRecombinationType.NONE,
            brca1Value = 0.4,
            brca2Value = 0.05,
            evidence = TestClinicalEvidenceFactory.createEmpty()
        )
    }

    private fun createProperTumorMutationalBurden(): TumorMutationalBurden {
        return TumorMutationalBurden(
            score = 13.71,
            isHigh = true,
            evidence = TestClinicalEvidenceFactory.withApprovedTreatment("Pembro")
        )
    }

    private fun createProperTumorMutationalLoad(): TumorMutationalLoad {
        return TumorMutationalLoad(
            score = 185,
            isHigh = true,
            evidence = TestClinicalEvidenceFactory.createEmpty()
        )
    }

    private fun createExhaustiveTestCharacteristics(): MolecularCharacteristics {
        return createProperTestCharacteristics().copy(
            microsatelliteStability = createProperMicrosatelliteStability().copy(evidence = TestClinicalEvidenceFactory.createExhaustive()),
            homologousRecombination = createProperHomologousRecombination().copy(evidence = TestClinicalEvidenceFactory.createExhaustive()),
            tumorMutationalBurden = createProperTumorMutationalBurden().copy(evidence = TestClinicalEvidenceFactory.createExhaustive()),
            tumorMutationalLoad = createProperTumorMutationalLoad().copy(evidence = TestClinicalEvidenceFactory.createExhaustive())
        )
    }

    fun createMinimalTestDrivers(): Drivers {
        return Drivers(
            variants = emptyList(),
            copyNumbers = emptyList(),
            homozygousDisruptions = emptyList(),
            disruptions = emptyList(),
            fusions = emptyList(),
            viruses = emptyList()
        )
    }

    fun createProperTestDrivers(): Drivers {
        return Drivers(
            variants = listOf(createProperVariant()),
            copyNumbers = listOf(createProperCopyNumber()),
            homozygousDisruptions = emptyList(),
            disruptions = emptyList(),
            fusions = emptyList(),
            viruses = emptyList()
        )
    }

    fun createMinimalCopyNumber() = CopyNumber(
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(),
        otherImpacts = emptySet(),
        isReportable = false,
        event = "",
        driverLikelihood = DriverLikelihood.LOW,
        evidence = TestClinicalEvidenceFactory.createEmpty(),
        gene = "",
        geneRole = GeneRole.UNKNOWN,
        proteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance = null
    )

    fun createProperCopyNumber() = CopyNumber(
        isReportable = true,
        event = "PTEN del",
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = TestClinicalEvidenceFactory.withEligibleTrials(
            setOf(
                TestExternalTrialFactory.create(
                    nctId = "NCT00000020",
                    title = "A Phase 1/2 Randomized Study to Evaluate the Safety and Efficacy of treatment X Plus treatment Y in " +
                            "Combination With Investigational Agents Versus treatment X Plus treatment Y, as First-Line Treatment " +
                            "for Participants With Advanced Solid Tumor (acronym)",
                    countries = setOf(
                        CountryDetails(Country.BELGIUM, mapOf("Brussels" to emptySet())),
                        CountryDetails(Country.GERMANY, mapOf("Berlin" to emptySet()))
                    ),
                    url = "https://clinicaltrials.gov/study/NCT00000020"
                ), TestExternalTrialFactory.create(
                    nctId = "NCT00000021",
                    title = "Another A Phase 1/2 Randomized Study to Evaluate the Safety and Efficacy of treatment X Plus treatment Y in " +
                            "Combination With Investigational Agents Versus treatment X Plus treatment Y, as First-Line Treatment " +
                            "for Participants With Advanced Solid Tumor (acronym)",
                    countries = setOf(
                        CountryDetails(
                            Country.BELGIUM,
                            mapOf(
                                "Location 1" to emptySet(),
                                "Location 2" to emptySet(),
                                "Location 3" to emptySet(),
                                "Location 4" to emptySet()
                            )
                        ),
                        CountryDetails(Country.GERMANY, mapOf("Berlin" to emptySet()))
                    ),
                    url = "https://clinicaltrials.gov/study/NCT00000021"
                )
            )
        ),
        gene = "PTEN",
        geneRole = GeneRole.TSG,
        proteinEffect = ProteinEffect.LOSS_OF_FUNCTION,
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_DEL),
        otherImpacts = emptySet(),
        isAssociatedWithDrugResistance = null
    )

    fun createMinimalVariant() = Variant(
        chromosome = "",
        position = 0,
        ref = "",
        alt = "",
        type = VariantType.UNDEFINED,
        variantAlleleFrequency = null,
        canonicalImpact = createMinimalTranscriptImpact(),
        otherImpacts = emptySet(),
        variantCopyNumber = null,
        totalCopyNumber = null,
        isBiallelic = null,
        clonalLikelihood = null,
        phaseGroups = null,
        isCancerAssociatedVariant = false,
        sourceEvent = "",
        isReportable = false,
        event = "",
        driverLikelihood = null,
        evidence = TestClinicalEvidenceFactory.createEmpty(),
        gene = "",
        geneRole = GeneRole.UNKNOWN,
        proteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance = null,
    )

    fun createMinimalTranscriptImpact() = TranscriptVariantImpact(
        transcriptId = "",
        hgvsCodingImpact = "",
        hgvsProteinImpact = "",
        affectedCodon = null,
        affectedExon = null,
        inSpliceRegion = null,
        effects = emptySet(),
        codingEffect = null,
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
            affectedExon = 15,
            inSpliceRegion = false,
            effects = setOf(VariantEffect.MISSENSE),
            codingEffect = CodingEffect.MISSENSE
        ),
        otherImpacts = emptySet(),
        variantCopyNumber = 4.1,
        totalCopyNumber = 6.0,
        isBiallelic = false,
        clonalLikelihood = 1.0,
        phaseGroups = null,
        isCancerAssociatedVariant = true,
        sourceEvent = "BRAF V600E",
        isReportable = true,
        event = "BRAF V600E",
        driverLikelihood = DriverLikelihood.HIGH,
        evidence = TestClinicalEvidenceFactory.withApprovedTreatment("Vemurafenib"),
        gene = "BRAF",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        isAssociatedWithDrugResistance = true,
    )

    fun createMinimalFusion() = Fusion(
        geneStart = "",
        geneEnd = "",
        driverType = FusionDriverType.NONE,
        proteinEffect = ProteinEffect.UNKNOWN,
        isAssociatedWithDrugResistance = null,
        geneTranscriptStart = null,
        geneTranscriptEnd = null,
        fusedExonUp = null,
        fusedExonDown = null,
        isReportable = false,
        event = "",
        driverLikelihood = null,
        evidence = TestClinicalEvidenceFactory.createEmpty(),
    )

    fun createProperFusion() = Fusion(
        isReportable = true,
        event = "EML4(exon6)::ALK(exon20) fusion",
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

    fun createMinimalVirus() = Virus(
        name = "",
        type = VirusType.OTHER,
        isReliable = false,
        integrations = null,
        isReportable = false,
        event = "",
        driverLikelihood = null,
        evidence = TestClinicalEvidenceFactory.createEmpty(),
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
            ), PharmacoEntry(
                gene = PharmacoGene.UGT1A1, haplotypes = setOf(
                    Haplotype(allele = "*1", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION),
                    Haplotype(allele = "*28", alleleCount = 1, function = HaplotypeFunction.REDUCED_FUNCTION),
                )
            )
        )
    }

    private fun createExhaustiveTestDrivers(): Drivers {
        val proper = createProperTestDrivers()
        return proper.copy(
            variants = proper.variants + listOf(
                Variant(
                    chromosome = "7",
                    position = 140453000,
                    ref = "T",
                    alt = "A",
                    type = VariantType.SNV,
                    canonicalImpact = TranscriptVariantImpact(
                        transcriptId = "ENST00000288602",
                        hgvsCodingImpact = "c.1499T>A",
                        hgvsProteinImpact = "p.V500E",
                        affectedCodon = 500,
                        affectedExon = 14,
                        inSpliceRegion = false,
                        effects = setOf(VariantEffect.MISSENSE),
                        codingEffect = CodingEffect.MISSENSE
                    ),
                    otherImpacts = setOf(
                        TranscriptVariantImpact(
                            transcriptId = "ENST00000288603",
                            hgvsCodingImpact = "c.1796T>A",
                            hgvsProteinImpact = "p.V450E",
                            affectedCodon = 450,
                            affectedExon = 15,
                            inSpliceRegion = false,
                            effects = setOf(VariantEffect.MISSENSE),
                            codingEffect = CodingEffect.MISSENSE
                        )
                    ),
                    variantCopyNumber = 4.1,
                    totalCopyNumber = 6.0,
                    isBiallelic = false,
                    clonalLikelihood = 1.0,
                    phaseGroups = null,
                    isCancerAssociatedVariant = true,
                    sourceEvent = "BRAF V450E",
                    isReportable = true,
                    event = "BRAF V500E",
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = TestClinicalEvidenceFactory.withApprovedTreatment("Vemurafenib"),
                    gene = "BRAF",
                    geneRole = GeneRole.ONCO,
                    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                    isAssociatedWithDrugResistance = true,
                ), Variant(
                    chromosome = "7",
                    position = 100000,
                    ref = "T",
                    alt = "A",
                    type = VariantType.SNV,
                    canonicalImpact = TranscriptVariantImpact(
                        transcriptId = "transcript",
                        hgvsCodingImpact = "c.1000T>A",
                        hgvsProteinImpact = "p.T788M",
                        affectedCodon = 788,
                        affectedExon = 20,
                        inSpliceRegion = false,
                        effects = setOf(VariantEffect.MISSENSE),
                        codingEffect = CodingEffect.MISSENSE
                    ),
                    otherImpacts = emptySet(),
                    variantCopyNumber = 2.0,
                    totalCopyNumber = 3.0,
                    isBiallelic = false,
                    clonalLikelihood = 1.0,
                    phaseGroups = null,
                    isCancerAssociatedVariant = false,
                    sourceEvent = "EGFR T788M",
                    isReportable = true,
                    event = "EGFR T788M",
                    driverLikelihood = DriverLikelihood.LOW,
                    evidence = TestClinicalEvidenceFactory.createEmpty(),
                    gene = "EGFR",
                    geneRole = GeneRole.ONCO,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = false,
                )
            ),
            copyNumbers = proper.copyNumbers + listOf(
                CopyNumber(
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
                                        Country.NETHERLANDS, mapOf(
                                            "Nijmegen" to setOf(Hospital("Radboud UMC", false)),
                                            "Amsterdam" to setOf(Hospital("AMC", false), Hospital("VUmc", false))
                                        )
                                    )
                                ),
                                url = "https://clinicaltrials.gov/study/NCT00000003"
                            ),
                            TestExternalTrialFactory.create(
                                nctId = "NCT00000-TO-BE-FILTERED-1",
                                title = "this national external trial should be filtered out",
                                countries = setOf(
                                    CountryDetails(
                                        Country.NETHERLANDS, mapOf(
                                            "Amsterdam" to setOf(Hospital("children", true))
                                        )
                                    )
                                ),
                                url = "https://clinicaltrials.gov/study/NCT00000011"
                            )
                        )
                    ),
                    gene = "MYC",
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.FULL_GAIN,
                        38,
                        38
                    ),
                    otherImpacts = emptySet(),
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null
                ), CopyNumber(
                    isReportable = true,
                    event = "EGFR amp",
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = TestClinicalEvidenceFactory.withEligibleTrials(
                        setOf(
                            TestExternalTrialFactory.create(
                                nctId = "NCT00000099",
                                title = "A Phase 1 Study of XYXYXY, a T-Cell-Redirecting Agent Targeting Z, for Advanced Prostate Cancer",
                                countries = setOf(
                                    CountryDetails(
                                        Country.NETHERLANDS, mapOf(
                                            "Nijmegen" to setOf(Hospital("Radboud UMC", false)),
                                            "Amsterdam" to setOf(Hospital("AMC", false), Hospital("VUmc", false))
                                        )
                                    )
                                ),
                                url = "https://clinicaltrials.gov/study/NCT00000003"
                            ),
                            TestExternalTrialFactory.create(
                                nctId = "NCT00000-TO-BE-FILTERED-2",
                                title = "this international external trial should be filtered out",
                                countries = setOf(
                                    CountryDetails(
                                        Country.BELGIUM, mapOf(
                                            "Amsterdam" to setOf(Hospital("international", false))
                                        )
                                    )
                                ),
                                url = "https://clinicaltrials.gov/study/NCT00000011"
                            )
                        )
                    ),
                    gene = "EGFR",
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.PARTIAL_GAIN,
                        20,
                        20
                    ),
                    otherImpacts = emptySet(),
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null
                ), CopyNumber(
                    isReportable = false,
                    event = "RET copy nr",
                    driverLikelihood = null,
                    evidence = TestClinicalEvidenceFactory.createExhaustive(),
                    gene = "RET",
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.NONE,
                        6,
                        6
                    ),
                    otherImpacts = setOf(
                        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                            CopyNumberType.NONE,
                            6,
                            6
                        ), TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                            CopyNumberType.NONE,
                            8,
                            8
                        )
                    ),
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null
                ), CopyNumber(
                    isReportable = true,
                    event = "DFGFRF1 del",
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = TestClinicalEvidenceFactory.createExhaustive(),
                    gene = "DFGFR1",
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.NONE,
                        6,
                        6
                    ),
                    otherImpacts = setOf(
                        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                            CopyNumberType.NONE,
                            6,
                            6
                        ), TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                            CopyNumberType.FULL_DEL,
                            0,
                            0
                        )
                    ),
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null
                ), CopyNumber(
                    isReportable = true,
                    event = "PIK3CA partial amp",
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = TestClinicalEvidenceFactory.createExhaustive(),
                    gene = "PIK3CA",
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.PARTIAL_GAIN,
                        null,
                        null
                    ),
                    otherImpacts = emptySet(),
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null
                ), CopyNumber(
                    isReportable = false,
                    event = "UNREP copy number",
                    driverLikelihood = DriverLikelihood.MEDIUM,
                    evidence = TestClinicalEvidenceFactory.createExhaustive(),
                    gene = "UNREP",
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.PARTIAL_GAIN,
                        20,
                        20
                    ),
                    otherImpacts = emptySet(),
                    geneRole = GeneRole.UNKNOWN,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    isAssociatedWithDrugResistance = null
                )
            ), homozygousDisruptions = proper.homozygousDisruptions + HomozygousDisruption(
                isReportable = true,
                event = "PTEN hom disruption",
                driverLikelihood = DriverLikelihood.HIGH,
                evidence = TestClinicalEvidenceFactory.createExhaustive(),
                gene = "PTEN",
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null
            ), disruptions = proper.disruptions + Disruption(
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
            ), fusions = proper.fusions + Fusion(
                isReportable = true,
                event = "EML4(exon6)::ALK(exon20) fusion",
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
            ), viruses = proper.viruses + listOf(
                Virus(
                    name = "Human papillomavirus type 16",
                    type = VirusType.HPV,
                    integrations = null,
                    isReliable = true,
                    isReportable = true,
                    event = "HPV positive",
                    driverLikelihood = DriverLikelihood.LOW,
                    evidence = TestClinicalEvidenceFactory.createExhaustive(),
                ), Virus(
                    name = "Epstein-Barr virus",
                    type = VirusType.EBV,
                    integrations = 4,
                    isReliable = true,
                    isReportable = true,
                    event = "EBV positive",
                    driverLikelihood = DriverLikelihood.HIGH,
                    evidence = TestClinicalEvidenceFactory.createExhaustive(),
                ), Virus(
                    name = "Other virus",
                    type = VirusType.OTHER,
                    integrations = 4,
                    isReliable = true,
                    isReportable = true,
                    event = "Other virus positive",
                    driverLikelihood = DriverLikelihood.LOW,
                    evidence = TestClinicalEvidenceFactory.createEmpty(),
                )
            )
        )
    }

    fun createMinimalDisruption(): Disruption {
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

    fun createMinimalHomozygousDisruption(): HomozygousDisruption {
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

    fun createHighConfidenceCupPrediction(): PredictedTumorOrigin {
        return PredictedTumorOrigin(
            predictions = listOf(
                CupPrediction(
                    cancerType = "Melanoma",
                    likelihood = 0.996,
                    snvPairwiseClassifier = 0.979,
                    genomicPositionClassifier = 0.99,
                    featureClassifier = 0.972,
                    cuppaMode = CuppaMode.WGS
                ), CupPrediction(
                    cancerType = "Lung",
                    likelihood = 0.001,
                    snvPairwiseClassifier = 0.0009,
                    genomicPositionClassifier = 0.011,
                    featureClassifier = 0.0102,
                    cuppaMode = CuppaMode.WGS
                ), CupPrediction(
                    cancerType = "Esophagus/Stomach",
                    likelihood = 0.0016,
                    snvPairwiseClassifier = 0.0004,
                    genomicPositionClassifier = 0.006,
                    featureClassifier = 0.0002,
                    cuppaMode = CuppaMode.WGS
                )
            )
        )
    }
}