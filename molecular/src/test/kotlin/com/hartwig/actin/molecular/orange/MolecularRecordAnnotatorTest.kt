package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.molecular.evidence.TestEvidenceDatabaseFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularRecordAnnotatorTest {

    private val annotator = MolecularRecordAnnotator(TestEvidenceDatabaseFactory.createProperDatabase())

    @Test
    fun `Should retain characteristics during annotation that are originally present`() {
        val annotated = annotator.annotate(TestMolecularFactory.createProperTestMolecularRecord())
        with(annotated.characteristics) {
            assertThat(microsatelliteStability!!.evidence).isNotNull()
            assertThat(homologousRecombination!!.evidence).isNotNull()
            assertThat(tumorMutationalBurden!!.evidence).isNotNull()
            assertThat(tumorMutationalLoad!!.evidence).isNotNull()
        }
    }

    @Test
    fun `Should not create characteristics during annotation that are originally missing`() {
        val annotated = annotator.annotate(TestMolecularFactory.createMinimalTestMolecularRecord())
        with(annotated.characteristics) {
            assertThat(microsatelliteStability).isNull()
            assertThat(homologousRecombination).isNull()
            assertThat(tumorMutationalBurden).isNull()
            assertThat(tumorMutationalLoad).isNull()
        }
    }
}