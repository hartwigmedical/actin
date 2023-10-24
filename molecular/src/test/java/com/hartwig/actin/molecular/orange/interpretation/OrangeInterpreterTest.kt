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
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.util.Set

class OrangeInterpreterTest {
    @Test
    fun shouldNotCrashOnMinimalOrangeRecord() {
        val interpreter = createTestInterpreter()
        Assert.assertNotNull(interpreter.interpret(TestOrangeFactory.createMinimalTestOrangeRecord()))
    }

    @Test
    fun shouldInterpretProperOrangeRecord() {
        val interpreter = createTestInterpreter()
        val record = interpreter.interpret(TestOrangeFactory.createProperTestOrangeRecord())
        Assert.assertEquals(TestDataFactory.TEST_PATIENT, record.patientId())
        Assert.assertEquals(TestDataFactory.TEST_SAMPLE, record.sampleId())
        Assert.assertEquals(ExperimentType.WHOLE_GENOME, record.type())
        Assert.assertEquals(RefGenomeVersion.V37, record.refGenomeVersion())
        Assert.assertEquals(LocalDate.of(2021, 5, 6), record.date())
        Assert.assertEquals(ActionabilityConstants.EVIDENCE_SOURCE.display(), record.evidenceSource())
        Assert.assertEquals(ActionabilityConstants.EXTERNAL_TRIAL_SOURCE.display(), record.externalTrialSource())
        Assert.assertTrue(record.containsTumorCells())
        Assert.assertTrue(record.hasSufficientQualityAndPurity())
        Assert.assertTrue(record.hasSufficientQuality())
        Assert.assertNotNull(record.characteristics())
        val drivers = record.drivers()
        Assert.assertEquals(1, drivers.variants().size.toLong())
        Assert.assertEquals(2, drivers.copyNumbers().size.toLong())
        Assert.assertEquals(1, drivers.homozygousDisruptions().size.toLong())
        Assert.assertEquals(1, drivers.disruptions().size.toLong())
        Assert.assertEquals(1, drivers.fusions().size.toLong())
        Assert.assertEquals(1, drivers.viruses().size.toLong())
        val immunology = record.immunology()
        Assert.assertTrue(immunology.isReliable())
        Assert.assertEquals(1, immunology.hlaAlleles().size.toLong())
        Assert.assertEquals(1, record.pharmaco().size.toLong())
    }

    @Test
    fun shouldBeAbleToConvertSampleIdToPatientId() {
        Assert.assertEquals("ACTN01029999", OrangeInterpreter.Companion.toPatientId("ACTN01029999T"))
        Assert.assertEquals("ACTN01029999", OrangeInterpreter.Companion.toPatientId("ACTN01029999T2"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowExceptionOnInvalidSampleId() {
        OrangeInterpreter.Companion.toPatientId("no sample")
    }

    @Test
    fun shouldBeAbleToResolveAllRefGenomeVersions() {
        for (refGenomeVersion in OrangeRefGenomeVersion.values()) {
            Assert.assertNotNull(OrangeInterpreter.Companion.determineRefGenomeVersion(refGenomeVersion))
        }
    }

    @Test
    fun shouldDetermineQualityAndPurityToBeSufficientWhenOnlyPassStatusIsPresent() {
        val record = orangeRecordWithQCStatus(PurpleQCStatus.PASS)
        Assert.assertTrue(OrangeInterpreter.Companion.hasSufficientQuality(record))
        Assert.assertTrue(OrangeInterpreter.Companion.hasSufficientQualityAndPurity(record))
    }

    @Test
    fun shouldDetermineQualityButNotPurityToBeSufficientWhenOnlyLowPurityWarningIsPresent() {
        val record = orangeRecordWithQCStatus(PurpleQCStatus.WARN_LOW_PURITY)
        Assert.assertTrue(OrangeInterpreter.Companion.hasSufficientQuality(record))
        Assert.assertFalse(OrangeInterpreter.Companion.hasSufficientQualityAndPurity(record))
    }

    @Test
    fun shouldDetermineQualityAndPurityToNotBeSufficientWhenOtherWarningIsPresent() {
        val record = orangeRecordWithQCStatus(PurpleQCStatus.WARN_DELETED_GENES)
        Assert.assertFalse(OrangeInterpreter.Companion.hasSufficientQuality(record))
        Assert.assertFalse(OrangeInterpreter.Companion.hasSufficientQualityAndPurity(record))
    }

    @Test
    fun shouldDetermineQualityExcludingPurityToNotBeSufficientWhenOtherWarningIsPresentWithLowPurityWarning() {
        val record = orangeRecordWithQCStatuses(Set.of(PurpleQCStatus.WARN_LOW_PURITY, PurpleQCStatus.WARN_DELETED_GENES))
        Assert.assertFalse(OrangeInterpreter.Companion.hasSufficientQuality(record))
        Assert.assertFalse(OrangeInterpreter.Companion.hasSufficientQualityAndPurity(record))
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionOnEmptyQCStates() {
        val interpreter = createTestInterpreter()
        interpreter.interpret(orangeRecordWithQCStatuses(setOf<PurpleQCStatus?>()))
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionOnMissingCuppaPredictionClassifiers() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record: OrangeRecord? = ImmutableOrangeRecord.copyOf(proper)
            .withCuppa(ImmutableCuppaData.copyOf(proper.cuppa()).withPredictions(TestCuppaFactory.builder().build()))
        val interpreter = createTestInterpreter()
        interpreter.interpret(record)
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionOnGermlineDisruptionPresent() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record: OrangeRecord? = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(ImmutableLinxRecord.copyOf(proper.linx())
                .withGermlineHomozygousDisruptions(TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build()))
        val interpreter = createTestInterpreter()
        interpreter.interpret(record)
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionOnGermlineBreakendPresent() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record: OrangeRecord? = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(ImmutableLinxRecord.copyOf(proper.linx())
                .withAllGermlineBreakends(TestLinxFactory.breakendBuilder().gene("gene 1").build()))
        val interpreter = createTestInterpreter()
        interpreter.interpret(record)
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionOnGermlineSVPresentPresent() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record: OrangeRecord? = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(ImmutableLinxRecord.copyOf(proper.linx())
                .withAllGermlineStructuralVariants(TestLinxFactory.structuralVariantBuilder().svId(1).build()))
        val interpreter = createTestInterpreter()
        interpreter.interpret(record)
    }

    companion object {
        private fun orangeRecordWithQCStatus(status: PurpleQCStatus?): OrangeRecord {
            return orangeRecordWithQCStatuses(Set.of(status))
        }

        private fun orangeRecordWithQCStatuses(statuses: MutableSet<PurpleQCStatus?>?): OrangeRecord {
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