package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.RefGenomeVersion
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.cuppa.TestCuppaFactory
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants
import com.hartwig.hmftools.datamodel.cuppa.ImmutableCuppaData
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxRecord
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleFit
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleQCStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class OrangeInterpreterTest {

    @Test
    fun shouldNotCrashOnMinimalOrangeRecord() {
        val interpreter = createTestInterpreter()
        assertNotNull(interpreter.interpret(TestOrangeFactory.createMinimalTestOrangeRecord()))
    }

    @Test
    fun shouldInterpretProperOrangeRecord() {
        val interpreter = createTestInterpreter()
        val record = interpreter.interpret(TestOrangeFactory.createProperTestOrangeRecord())
        assertEquals(TestDataFactory.TEST_PATIENT, record.patientId())
        assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId())
        assertEquals(ExperimentType.WHOLE_GENOME, record.type())
        assertEquals(RefGenomeVersion.V37, record.refGenomeVersion())
        assertEquals(LocalDate.of(2021, 5, 6), record.date())
        assertEquals(ActionabilityConstants.EVIDENCE_SOURCE.display(), record.evidenceSource())
        assertEquals(ActionabilityConstants.EXTERNAL_TRIAL_SOURCE.display(), record.externalTrialSource())
        assertTrue(record.containsTumorCells())
        assertTrue(record.hasSufficientQualityAndPurity())
        assertTrue(record.hasSufficientQuality())
        assertNotNull(record.characteristics())

        val drivers = record.drivers()
        assertEquals(1, drivers.variants().size.toLong())
        assertEquals(2, drivers.copyNumbers().size.toLong())
        assertEquals(1, drivers.homozygousDisruptions().size.toLong())
        assertEquals(1, drivers.disruptions().size.toLong())
        assertEquals(1, drivers.fusions().size.toLong())
        assertEquals(1, drivers.viruses().size.toLong())

        val immunology = record.immunology()
        assertTrue(immunology.isReliable)
        assertEquals(1, immunology.hlaAlleles().size.toLong())
        assertEquals(1, record.pharmaco().size.toLong())
    }

    @Test
    fun shouldBeAbleToConvertSampleIdToPatientId() {
        assertEquals("ACTN01029999", OrangeInterpreter.toPatientId("ACTN01029999T"))
        assertEquals("ACTN01029999", OrangeInterpreter.toPatientId("ACTN01029999T2"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowExceptionOnInvalidSampleId() {
        OrangeInterpreter.toPatientId("no sample")
    }

    @Test
    fun shouldBeAbleToResolveAllRefGenomeVersions() {
        for (refGenomeVersion in OrangeRefGenomeVersion.values()) {
            assertNotNull(OrangeInterpreter.determineRefGenomeVersion(refGenomeVersion))
        }
    }

    @Test
    fun shouldDetermineQualityAndPurityToBeSufficientWhenOnlyPassStatusIsPresent() {
        val record = orangeRecordWithQCStatus(PurpleQCStatus.PASS)
        assertTrue(OrangeInterpreter.hasSufficientQuality(record))
        assertTrue(OrangeInterpreter.hasSufficientQualityAndPurity(record))
    }

    @Test
    fun shouldDetermineQualityButNotPurityToBeSufficientWhenOnlyLowPurityWarningIsPresent() {
        val record = orangeRecordWithQCStatus(PurpleQCStatus.WARN_LOW_PURITY)
        assertTrue(OrangeInterpreter.hasSufficientQuality(record))
        assertFalse(OrangeInterpreter.hasSufficientQualityAndPurity(record))
    }

    @Test
    fun shouldDetermineQualityAndPurityToNotBeSufficientWhenOtherWarningIsPresent() {
        val record = orangeRecordWithQCStatus(PurpleQCStatus.WARN_DELETED_GENES)
        assertFalse(OrangeInterpreter.hasSufficientQuality(record))
        assertFalse(OrangeInterpreter.hasSufficientQualityAndPurity(record))
    }

    @Test
    fun shouldDetermineQualityExcludingPurityToNotBeSufficientWhenOtherWarningIsPresentWithLowPurityWarning() {
        val record = orangeRecordWithQCStatuses(setOf(PurpleQCStatus.WARN_LOW_PURITY, PurpleQCStatus.WARN_DELETED_GENES))
        assertFalse(OrangeInterpreter.hasSufficientQuality(record))
        assertFalse(OrangeInterpreter.hasSufficientQualityAndPurity(record))
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionOnEmptyQCStates() {
        val interpreter = createTestInterpreter()
        interpreter.interpret(orangeRecordWithQCStatuses(mutableSetOf()))
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionOnMissingCuppaPredictionClassifiers() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record: OrangeRecord = ImmutableOrangeRecord.copyOf(proper)
            .withCuppa(ImmutableCuppaData.copyOf(proper.cuppa()).withPredictions(TestCuppaFactory.builder().build()))
        val interpreter = createTestInterpreter()
        interpreter.interpret(record)
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionOnGermlineDisruptionPresent() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record: OrangeRecord = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(ImmutableLinxRecord.copyOf(proper.linx())
                .withGermlineHomozygousDisruptions(TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build()))
        val interpreter = createTestInterpreter()
        interpreter.interpret(record)
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionOnGermlineBreakendPresent() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record: OrangeRecord = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(ImmutableLinxRecord.copyOf(proper.linx())
                .withAllGermlineBreakends(TestLinxFactory.breakendBuilder().gene("gene 1").build()))
        val interpreter = createTestInterpreter()
        interpreter.interpret(record)
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionOnGermlineSVPresentPresent() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record: OrangeRecord = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(ImmutableLinxRecord.copyOf(proper.linx())
                .withAllGermlineStructuralVariants(TestLinxFactory.structuralVariantBuilder().svId(1).build()))
        val interpreter = createTestInterpreter()
        interpreter.interpret(record)
    }

    companion object {
        private fun orangeRecordWithQCStatus(status: PurpleQCStatus): OrangeRecord {
            return orangeRecordWithQCStatuses(setOf(status))
        }

        private fun orangeRecordWithQCStatuses(statuses: Set<PurpleQCStatus>): OrangeRecord {
            val minimal = TestOrangeFactory.createMinimalTestOrangeRecord()
            return ImmutableOrangeRecord.copyOf(minimal)
                .withPurple(ImmutablePurpleRecord.copyOf(minimal.purple())
                    .withFit(ImmutablePurpleFit.copyOf(minimal.purple().fit())
                        .withQc(TestPurpleFactory.purpleQCBuilder().addAllStatus(statuses).build())))
        }

        private fun createTestInterpreter(): OrangeInterpreter {
            return OrangeInterpreter(TestGeneFilterFactory.createAlwaysValid(), TestEvidenceDatabaseFactory.createEmptyDatabase())
        }
    }
}