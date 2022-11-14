package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.TestAmplificationFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory;
import com.hartwig.actin.molecular.datamodel.driver.VariantType;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableHlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableMolecularImmunology;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutableHaplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;

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
                .evidenceSource(Strings.EMPTY)
                .externalTrialSource(Strings.EMPTY)
                .containsTumorCells(true)
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
                .predictedTumorOrigin(ImmutablePredictedTumorOrigin.builder().tumorType("Melanoma").likelihood(0.996).build())
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
    private static MolecularCharacteristics createExhaustiveTestCharacteristics() {
        return ImmutableMolecularCharacteristics.builder()
                .from(createProperTestCharacteristics())
                .microsatelliteEvidence(TestActionableEvidenceFactory.createExhaustive())
                .homologousRepairDeficiencyEvidence(TestActionableEvidenceFactory.createExhaustive())
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
                        .type(VariantType.SNV)
                        .variantCopyNumber(4.1)
                        .totalCopyNumber(6.0)
                        .isBiallelic(false)
                        .isHotspot(true)
                        .clonalLikelihood(1.0)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsProteinImpact("p.V600E").build())
                        .build())
                .addLosses(TestLossFactory.builder()
                        .isReportable(true)
                        .event("PTEN del")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(TestActionableEvidenceFactory.withPreClinicalTreatment("Trial"))
                        .gene("PTEN")
                        .geneRole(GeneRole.TSG)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .minCopies(0)
                        .maxCopies(0)
                        .build())
                .build();
    }

    @NotNull
    private static MolecularImmunology createProperTestImmunology() {
        return ImmutableMolecularImmunology.builder()
                .isReliable(true)
                .addHlaAlleles(ImmutableHlaAllele.builder().name("A*02:01").tumorCopyNumber(1.2).hasSomaticMutations(false).build())
                .build();
    }

    @NotNull
    private static Set<PharmacoEntry> createProperTestPharmaco() {
        return Sets.newHashSet(ImmutablePharmacoEntry.builder()
                .gene("DPYD")
                .addHaplotypes(ImmutableHaplotype.builder().name("1* HOM").function("Normal function").build())
                .build());
    }

    @NotNull
    private static MolecularDrivers createExhaustiveTestDrivers() {
        return ImmutableMolecularDrivers.builder()
                .from(createProperTestDrivers())
                .addAmplifications(TestAmplificationFactory.builder()
                        .isReportable(true)
                        .event("MYC amp")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .gene("MYC")
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
                        .type("DEL")
                        .junctionCopyNumber(1.1)
                        .undisruptedCopyNumber(1.8)
                        .range("Intron 1 downstream")
                        .build())
                .addFusions(TestFusionFactory.builder()
                        .isReportable(true)
                        .event("EML4 - ALK fusion")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .geneStart("EML4")
                        .fusedExonUp(2)
                        .geneEnd("ALK")
                        .fusedExonDown(4)
                        .driverType(FusionDriverType.KNOWN_PAIR)
                        .build())
                .addViruses(TestVirusFactory.builder()
                        .isReportable(true)
                        .event("HPV16 positive")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .name("Human papillomavirus type 16d")
                        .interpretation("HPV16")
                        .integrations(3)
                        .build())
                .build();
    }
}
