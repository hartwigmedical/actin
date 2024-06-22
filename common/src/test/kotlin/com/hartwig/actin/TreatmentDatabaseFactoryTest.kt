package com.hartwig.actin

import com.hartwig.actin.TreatmentDatabaseFactory.createFromPath
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import java.nio.file.NoSuchFileException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class TreatmentDatabaseFactoryTest {

    @Test
    fun `Should create database from directory`() {
        val treatmentDatabase = createFromPath(resourceOnClasspath("clinical/treatment_db"))
        assertThat(treatmentDatabase).isNotNull()
        assertThat(treatmentDatabase.findTreatmentByName("Capecitabine+Oxaliplatin")).isNotNull()
        assertThat(treatmentDatabase.findTreatmentByName("CAPECITABINE AND OXALIPLATIN")).isNotNull()
    }

    @Test
    fun `Should throw exception on create when files are missing`() {
        assertThatThrownBy { createFromPath(resourceOnClasspath("clinical/treatment_db_missing_files")) }
            .isInstanceOf(NoSuchFileException::class.java)
    }

    @Test
    fun `Should throw exception on create when invalid type provided in file`() {
        assertThatThrownBy { createFromPath(resourceOnClasspath("clinical/treatment_db_invalid")) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
