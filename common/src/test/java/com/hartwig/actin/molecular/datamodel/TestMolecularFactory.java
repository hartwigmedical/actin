package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableCuppaPrediction;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.CodingContext;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.DisruptionType;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.RegionType;
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory;
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect;
import com.hartwig.actin.molecular.datamodel.driver.VariantType;
import com.hartwig.actin.molecular.datamodel.driver.VirusType;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableMolecularImmunology;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.datamodel.immunology.TestHlaAlleleFactory;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;
import com.hartwig.actin.molecular.datamodel.pharmaco.TestPharmacoFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestMolecularFactory {

    private static final LocalDate TODAY = LocalDate.now();

    private static final int DAYS_SINCE_MOLECULAR_ANALYSIS = 5;

    private TestMolecularFactory() {
    }

    @NotNull
    public static MolecularRecord createMinimalTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .patientId(TestDataFactory.TEST_PATIENT)
                .sampleId(TestDataFactory.TEST_SAMPLE)
                .type(ExperimentType.WGS)
                .refGenomeVersion(RefGenomeVersion.V37)
                .evidenceSource(Strings.EMPTY)
                .externalTrialSource(Strings.EMPTY)
                .containsTumorCells(true)
                .hasSufficientQualityAndPurity(true)
                .hasSufficientQuality(true)
                .characteristics(ImmutableMolecularCharacteristics.builder().build())
                .drivers(ImmutableMolecularDrivers.builder().build())
                .immunology(ImmutableMolecularImmunology.builder().isReliable(false).build())
                .build();
    }

    @NotNull
    public static MolecularRecord createProperTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createMinimalTestMolecularRecord())
                .date(TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS))
                .evidenceSource("kb")
                .externalTrialSource("trial kb")
                .characteristics(createProperTestCharacteristics())
                .drivers(createProperTestDrivers())
                .immunology(createProperTestImmunology())
                .pharmaco(createProperTestPharmaco())
                .build();
    }

    @NotNull
    public static MolecularRecord createExhaustiveTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createProperTestMolecularRecord())
                .characteristics(createExhaustiveTestCharacteristics())
                .drivers(createExhaustiveTestDrivers())
                .build();
    }

    @NotNull
    private static MolecularCharacteristics createProperTestCharacteristics() {
        return ImmutableMolecularCharacteristics.builder()
                .purity(0.98)
                .ploidy(3.1)
                .predictedTumorOrigin(createProperPredictedTumorOrigin())
                .isMicrosatelliteUnstable(false)
                .isHomologousRepairDeficient(false)
                .tumorMutationalBurden(13.71)
                .hasHighTumorMutationalBurden(true)
                .tumorMutationalBurdenEvidence(TestActionableEvidenceFactory.withApprovedTreatment("Pembro"))
                .tumorMutationalLoad(185)
                .hasHighTumorMutationalLoad(true)
                .build();
    }

    @NotNull
    private static ImmutablePredictedTumorOrigin createProperPredictedTumorOrigin() {
        return ImmutablePredictedTumorOrigin.builder()
                .tumorType("Melanoma")
                .likelihood(0.996)
                .predictions(List.of(ImmutableCuppaPrediction.builder()
                                .cancerType("Melanoma")
                                .likelihood(0.996)
                                .snvPairwiseClassifier(0.979)
                                .genomicPositionClassifier(0.99)
                                .featureClassifier(0.972)
                                .build(),
                        ImmutableCuppaPrediction.builder()
                                .cancerType("Lung")
                                .likelihood(0.001)
                                .snvPairwiseClassifier(0.0009)
                                .genomicPositionClassifier(0.011)
                                .featureClassifier(0.0102)
                                .build(),
                        ImmutableCuppaPrediction.builder()
                                .cancerType("Esophagus/Stomach")
                                .likelihood(0.0016)
                                .snvPairwiseClassifier(0.0004)
                                .genomicPositionClassifier(0.006)
                                .featureClassifier(0.0002)
                                .build()))
                .build();
    }

    @NotNull
    private static MolecularCharacteristics createExhaustiveTestCharacteristics() {
        return ImmutableMolecularCharacteristics.builder()
                .from(createProperTestCharacteristics())
                .microsatelliteEvidence(TestActionableEvidenceFactory.createExhaustive())
                .homologousRepairEvidence(TestActionableEvidenceFactory.createExhaustive())
                .tumorMutationalBurdenEvidence(TestActionableEvidenceFactory.createExhaustive())
                .tumorMutationalLoadEvidence(TestActionableEvidenceFactory.createExhaustive())
                .build();
    }

    @NotNull
    private static MolecularDrivers createProperTestDrivers() {
        return ImmutableMolecularDrivers.builder()
                .addVariants(TestVariantFactory.builder()
                        .isReportable(true)
                        .event("BRAF V600E")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(TestActionableEvidenceFactory.withApprovedTreatment("Vemurafenib"))
                        .gene("BRAF")
                        .geneRole(GeneRole.ONCO)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .isAssociatedWithDrugResistance(true)
                        .type(VariantType.SNV)
                        .variantCopyNumber(4.1)
                        .totalCopyNumber(6.0)
                        .isBiallelic(false)
                        .isHotspot(true)
                        .clonalLikelihood(1.0)
                        .canonicalImpact(TestTranscriptImpactFactory.builder()
                                .transcriptId("ENST00000288602")
                                .hgvsCodingImpact("c.1799T>A")
                                .hgvsProteinImpact("p.V600E")
                                .affectedCodon(600)
                                .isSpliceRegion(false)
                                .addEffects(VariantEffect.MISSENSE)
                                .codingEffect(CodingEffect.MISSENSE)
                                .build())
                        .build())
                .addCopyNumbers(TestCopyNumberFactory.builder()
                        .isReportable(true)
                        .event("PTEN del")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(TestActionableEvidenceFactory.withExternalEligibleTrial("Trial 1"))
                        .gene("PTEN")
                        .geneRole(GeneRole.TSG)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .type(CopyNumberType.LOSS)
                        .minCopies(0)
                        .maxCopies(0)
                        .build())
                .build();
    }

    @NotNull
    private static MolecularImmunology createProperTestImmunology() {
        return ImmutableMolecularImmunology.builder()
                .isReliable(true)
                .addHlaAlleles(TestHlaAlleleFactory.builder().name("A*02:01").tumorCopyNumber(1.2).hasSomaticMutations(false).build())
                .build();
    }

    @NotNull
    private static Set<PharmacoEntry> createProperTestPharmaco() {
        Set<PharmacoEntry> pharmacoEntries = Sets.newHashSet();

        pharmacoEntries.add(ImmutablePharmacoEntry.builder()
                .gene("DPYD")
                .addHaplotypes(TestPharmacoFactory.builder().name("*1 HOM").function("Normal function").build())
                .build());

        pharmacoEntries.add(ImmutablePharmacoEntry.builder()
                .gene("UGT1A1")
                .addHaplotypes(TestPharmacoFactory.builder().name("*1 HET").function("Normal function").build())
                .addHaplotypes(TestPharmacoFactory.builder().name("*28 HET").function("Reduced function").build())
                .build());

        return pharmacoEntries;
    }

    @NotNull
    private static MolecularDrivers createExhaustiveTestDrivers() {
        return ImmutableMolecularDrivers.builder()
                .from(createProperTestDrivers())
                .addCopyNumbers(TestCopyNumberFactory.builder()
                        .isReportable(true)
                        .event("MYC amp")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .gene("MYC")
                        .type(CopyNumberType.FULL_GAIN)
                        .minCopies(38)
                        .maxCopies(38)
                        .build())
                .addHomozygousDisruptions(TestHomozygousDisruptionFactory.builder()
                        .isReportable(true)
                        .event("PTEN hom disruption")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .gene("PTEN")
                        .build())
                .addDisruptions(TestDisruptionFactory.builder()
                        .isReportable(true)
                        .event("PTEN disruption")
                        .driverLikelihood(DriverLikelihood.LOW)
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .gene("PTEN")
                        .type(DisruptionType.DEL)
                        .junctionCopyNumber(1.1)
                        .undisruptedCopyNumber(1.8)
                        .regionType(RegionType.EXONIC)
                        .codingContext(CodingContext.CODING)
                        .build())
                .addFusions(TestFusionFactory.builder()
                        .isReportable(true)
                        .event("EML4 - ALK fusion")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .geneStart("EML4")
                        .geneTranscriptStart("ENST00000318522")
                        .fusedExonUp(6)
                        .geneEnd("ALK")
                        .geneTranscriptEnd("ENST00000389048")
                        .fusedExonDown(20)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .driverType(FusionDriverType.KNOWN_PAIR)
                        .build())
                .addViruses(TestVirusFactory.builder()
                        .isReportable(true)
                        .event("HPV positive")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .name("Human papillomavirus type 16")
                        .type(VirusType.HUMAN_PAPILLOMA_VIRUS)
                        .integrations(3)
                        .isReliable(true)
                        .build())
                .build();
    }
}
