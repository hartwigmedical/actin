package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.RefGenomeVersion;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRefGenomeVersion;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleFit;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleQCStatus;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeInterpreterTest {

    @Test
    public void shouldNotCrashOnMinimalOrangeRecord() {
        OrangeInterpreter interpreter = createTestInterpreter();
        assertNotNull(interpreter.interpret(TestOrangeFactory.createMinimalTestOrangeRecord()));
    }

    @Test
    public void shouldInterpretProperOrangeRecord() {
        OrangeInterpreter interpreter = createTestInterpreter();
        MolecularRecord record = interpreter.interpret(TestOrangeFactory.createProperTestOrangeRecord());

        assertEquals(TestDataFactory.TEST_PATIENT, record.patientId());
        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId());
        assertEquals(ExperimentType.WGS, record.type());
        assertEquals(RefGenomeVersion.V37, record.refGenomeVersion());
        assertEquals(LocalDate.of(2021, 5, 6), record.date());
        assertEquals(ActionabilityConstants.EVIDENCE_SOURCE.display(), record.evidenceSource());
        assertEquals(ActionabilityConstants.EXTERNAL_TRIAL_SOURCE.display(), record.externalTrialSource());
        assertTrue(record.containsTumorCells());
        assertTrue(record.hasSufficientQualityAndPurity());
        assertTrue(record.hasSufficientQuality());

        assertNotNull(record.characteristics());

        MolecularDrivers drivers = record.drivers();
        assertEquals(1, drivers.variants().size());
        assertEquals(2, drivers.copyNumbers().size());
        assertEquals(1, drivers.homozygousDisruptions().size());
        assertEquals(1, drivers.disruptions().size());
        assertEquals(1, drivers.fusions().size());
        assertEquals(1, drivers.viruses().size());

        MolecularImmunology immunology = record.immunology();
        assertTrue(immunology.isReliable());
        assertEquals(1, immunology.hlaAlleles().size());

        assertEquals(1, record.pharmaco().size());
    }

    @Test
    public void shouldBeAbleToConvertSampleIdToPatientId() {
        assertEquals("ACTN01029999", OrangeInterpreter.toPatientId("ACTN01029999T"));
        assertEquals("ACTN01029999", OrangeInterpreter.toPatientId("ACTN01029999T2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnInvalidSampleId() {
        OrangeInterpreter.toPatientId("no sample");
    }

    @Test
    public void shouldBeAbleToResolveAllRefGenomeVersions() {
        for (OrangeRefGenomeVersion refGenomeVersion : OrangeRefGenomeVersion.values()) {
            assertNotNull(OrangeInterpreter.determineRefGenomeVersion(refGenomeVersion));
        }
    }

    @Test
    public void shouldDetermineQualityAndPurityToBeSufficientWhenOnlyPassStatusIsPresent() {
        OrangeRecord record = orangeRecordWithQCStatus(PurpleQCStatus.PASS);
        assertTrue(OrangeInterpreter.hasSufficientQuality(record));
        assertTrue(OrangeInterpreter.hasSufficientQualityAndPurity(record));
    }

    @Test
    public void shouldDetermineQualityButNotPurityToBeSufficientWhenOnlyLowPurityWarningIsPresent() {
        OrangeRecord record = orangeRecordWithQCStatus(PurpleQCStatus.WARN_LOW_PURITY);
        assertTrue(OrangeInterpreter.hasSufficientQuality(record));
        assertFalse(OrangeInterpreter.hasSufficientQualityAndPurity(record));
    }

    @Test
    public void shouldDetermineQualityAndPurityToNotBeSufficientWhenOtherWarningIsPresent() {
        OrangeRecord record = orangeRecordWithQCStatus(PurpleQCStatus.WARN_DELETED_GENES);
        assertFalse(OrangeInterpreter.hasSufficientQuality(record));
        assertFalse(OrangeInterpreter.hasSufficientQualityAndPurity(record));
    }

    @Test
    public void shouldDetermineQualityExcludingPurityToNotBeSufficientWhenOtherWarningIsPresentWithLowPurityWarning() {
        OrangeRecord record = orangeRecordWithQCStatuses(Set.of(PurpleQCStatus.WARN_LOW_PURITY, PurpleQCStatus.WARN_DELETED_GENES));
        assertFalse(OrangeInterpreter.hasSufficientQuality(record));
        assertFalse(OrangeInterpreter.hasSufficientQualityAndPurity(record));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnEmptyQCStates() {
        OrangeInterpreter interpreter = createTestInterpreter();
        interpreter.interpret(orangeRecordWithQCStatuses(Set.of()));
    }

    @NotNull
    private static OrangeRecord orangeRecordWithQCStatus(PurpleQCStatus status) {
        return orangeRecordWithQCStatuses(Set.of(status));
    }

    @NotNull
    private static OrangeRecord orangeRecordWithQCStatuses(Set<PurpleQCStatus> statuses) {
        OrangeRecord minimal = TestOrangeFactory.createMinimalTestOrangeRecord();
        return ImmutableOrangeRecord.copyOf(minimal)
                .withPurple(ImmutablePurpleRecord.copyOf(minimal.purple())
                        .withFit(ImmutablePurpleFit.copyOf(minimal.purple().fit()).withQcStatuses(statuses)));
    }

    @NotNull
    private static OrangeInterpreter createTestInterpreter() {
        return new OrangeInterpreter(TestGeneFilterFactory.createAlwaysValid(), TestEvidenceDatabaseFactory.createEmptyDatabase());
    }
}