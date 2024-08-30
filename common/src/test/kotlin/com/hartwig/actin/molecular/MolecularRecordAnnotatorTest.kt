package com.hartwig.actin.molecular

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.molecular.evidence.TestEvidenceDatabaseFactory
import com.hartwig.actin.molecular.evidence.orange.MolecularRecordAnnotator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularRecordAnnotatorTest {
    private val annotator = MolecularRecordAnnotator(TestEvidenceDatabaseFactory.createProperDatabase())

    @Test
    fun `Should annotate molecular record with evidence for characteristics when they are provided`() {
        val annotated = annotator.annotate(TestMolecularFactory.createProperTestMolecularRecord())
        with(annotated.characteristics) {
            assertThat(microsatelliteEvidence).isNotNull
            assertThat(homologousRepairEvidence).isNotNull
            assertThat(tumorMutationalBurdenEvidence).isNotNull
            assertThat(tumorMutationalLoadEvidence).isNotNull
        }
    }

    @Test
    fun `Should not annotate molecular record with evidence for null characteristics`() {
        val annotated = annotator.annotate(TestMolecularFactory.createMinimalTestMolecularRecord())
        with(annotated.characteristics) {
            assertThat(microsatelliteEvidence).isNull()
            assertThat(homologousRepairEvidence).isNull()
            assertThat(tumorMutationalBurdenEvidence).isNull()
            assertThat(tumorMutationalLoadEvidence).isNull()
        }
    }
}