package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory
import com.hartwig.actin.molecular.orange.datamodel.cuppa.TestCuppaFactory
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.cuppa.ImmutableCuppaData
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxRecord
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleFit
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleQCStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class OrangeExtractorTest {

    private val interpreter = OrangeExtractor(TestGeneFilterFactory.createAlwaysValid())

    @Test
    fun `Should not crash on minimal orange record`() {
        assertThat(interpreter.interpret(TestOrangeFactory.createMinimalTestOrangeRecord())).isNotNull()
    }

    @Test
    fun `Should interpret proper orange record`() {
        val record = interpreter.interpret(TestOrangeFactory.createProperTestOrangeRecord())
        assertThat(record.sampleId).isEqualTo(TestPatientFactory.TEST_SAMPLE)
        assertThat(record.experimentType).isEqualTo(ExperimentType.HARTWIG_WHOLE_GENOME)
        assertThat(record.refGenomeVersion).isEqualTo(RefGenomeVersion.V37)
        assertThat(record.date).isEqualTo(LocalDate.of(2021, 5, 6))
        assertThat(record.evidenceSource).isEqualTo(ActionabilityConstants.EVIDENCE_SOURCE.display())
        assertThat(record.externalTrialSource).isEqualTo(ActionabilityConstants.EXTERNAL_TRIAL_SOURCE.display())
        assertThat(record.containsTumorCells).isTrue
        assertThat(record.isContaminated).isFalse
        assertThat(record.hasSufficientPurity).isTrue
        assertThat(record.hasSufficientQuality).isTrue
        assertThat(record.characteristics).isNotNull()

        val drivers = record.drivers
        assertThat(drivers.variants).hasSize(1)
        assertThat(drivers.copyNumbers).hasSize(3)
        assertThat(drivers.homozygousDisruptions).hasSize(1)
        assertThat(drivers.disruptions).hasSize(1)
        assertThat(drivers.fusions).hasSize(1)
        assertThat(drivers.viruses).hasSize(1)

        val immunology = record.immunology
        assertThat(immunology.isReliable).isTrue
        assertThat(immunology.hlaAlleles).hasSize(1)
        assertThat(record.pharmaco).hasSize(1)
    }

    @Test
    fun `Should be able to resolve all ref genome versions`() {
        for (refGenomeVersion in OrangeRefGenomeVersion.values()) {
            assertThat(interpreter.determineRefGenomeVersion(refGenomeVersion)).isNotNull()
        }
    }

    @Test
    fun `Should determine quality and purity to be sufficient when only pass status is present`() {
        val record = orangeRecordWithQCStatus(PurpleQCStatus.PASS)
        assertThat(interpreter.hasSufficientQuality(record)).isTrue
        assertThat(interpreter.hasSufficientPurity(record)).isTrue
    }

    @Test
    fun `Should determine quality but not purity to be sufficient when only low purity warning is present`() {
        val record = orangeRecordWithQCStatus(PurpleQCStatus.WARN_LOW_PURITY)
        assertThat(interpreter.hasSufficientQuality(record)).isTrue
        assertThat(interpreter.hasSufficientPurity(record)).isFalse
    }

    @Test
    fun `Should determine quality and purity to be sufficient when other warning is present`() {
        val record = orangeRecordWithQCStatus(PurpleQCStatus.WARN_DELETED_GENES)
        assertThat(interpreter.hasSufficientQuality(record)).isTrue
        assertThat(interpreter.hasSufficientPurity(record)).isTrue
    }

    @Test
    fun `Should determine quality excluding purity to be sufficient when other warning is present with low purity warning`() {
        val record = orangeRecordWithQCStatuses(setOf(PurpleQCStatus.WARN_LOW_PURITY, PurpleQCStatus.WARN_DELETED_GENES))
        assertThat(interpreter.hasSufficientQuality(record)).isTrue
        assertThat(interpreter.hasSufficientPurity(record)).isFalse
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception on empty QC States`() {
        interpreter.interpret(orangeRecordWithQCStatuses(mutableSetOf()))
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception on missing cuppa prediction classifiers`() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record = ImmutableOrangeRecord.copyOf(proper)
            .withCuppa(ImmutableCuppaData.copyOf(proper.cuppa()).withPredictions(TestCuppaFactory.builder().build()))
        interpreter.interpret(record)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception on germline variant present`() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record = ImmutableOrangeRecord.copyOf(proper)
            .withPurple(
                ImmutablePurpleRecord.copyOf(proper.purple())
                    .withAllGermlineVariants(TestPurpleFactory.variantBuilder().gene("gene 1").build())
            )
        interpreter.interpret(record)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception on germline disruption present`() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(
                ImmutableLinxRecord.copyOf(proper.linx())
                    .withGermlineHomozygousDisruptions(TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build())
            )
        interpreter.interpret(record)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception on germline breakend present`() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(
                ImmutableLinxRecord.copyOf(proper.linx())
                    .withAllGermlineBreakends(TestLinxFactory.breakendBuilder().gene("gene 1").build())
            )
        interpreter.interpret(record)
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception on germline SV present`() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(
                ImmutableLinxRecord.copyOf(proper.linx())
                    .withAllGermlineStructuralVariants(TestLinxFactory.structuralVariantBuilder().svId(1).build())
            )
        interpreter.interpret(record)
    }

    @Test
    fun `Should accept empty list as scrubbed for germline variant`() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record = ImmutableOrangeRecord.copyOf(proper)
            .withPurple(ImmutablePurpleRecord.copyOf(proper.purple()).withAllGermlineVariants(emptyList()))
        interpreter.interpret(record)
    }

    @Test
    fun `Should accept empty list as scrubbed for germline disruption`() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(ImmutableLinxRecord.copyOf(proper.linx()).withGermlineHomozygousDisruptions(emptyList()))
        interpreter.interpret(record)
    }

    @Test
    fun `Should accept empty list as scrubbed for germline breakends`() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(ImmutableLinxRecord.copyOf(proper.linx()).withAllGermlineBreakends(emptyList()))
        interpreter.interpret(record)
    }

    @Test
    fun `Should accept empty list as for scrubbed germline SV`() {
        val proper = TestOrangeFactory.createProperTestOrangeRecord()
        val record = ImmutableOrangeRecord.copyOf(proper)
            .withLinx(ImmutableLinxRecord.copyOf(proper.linx()).withAllGermlineStructuralVariants(emptyList()))
        interpreter.interpret(record)
    }

    private fun orangeRecordWithQCStatus(status: PurpleQCStatus): OrangeRecord {
        return orangeRecordWithQCStatuses(setOf(status))
    }

    private fun orangeRecordWithQCStatuses(statuses: Set<PurpleQCStatus>): OrangeRecord {
        val minimal = TestOrangeFactory.createMinimalTestOrangeRecord()
        return ImmutableOrangeRecord.copyOf(minimal)
            .withPurple(
                ImmutablePurpleRecord.copyOf(minimal.purple())
                    .withFit(
                        ImmutablePurpleFit.copyOf(minimal.purple().fit())
                            .withQc(TestPurpleFactory.purpleQCBuilder().addAllStatus(statuses).build())
                    )
            )
    }
}