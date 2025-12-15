package com.hartwig.actin

import com.hartwig.actin.TreatmentDatabaseFactory.createFromPath
import com.hartwig.actin.TreatmentDatabaseFactory.writeToPath
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
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

    @Test
    fun `Should save database to directory`() {
        val drugTreatment = TreatmentTestFactory.drugTreatment("Test name", TreatmentCategory.RADIOTHERAPY)
        val drugsByName = drugTreatment.drugs.associateBy { it.name }
        val db = TreatmentDatabase(drugsByName, listOf(drugTreatment).associateBy { it.name },)
        writeToPath("/Users/deniskumar/hmf/repos/actin/common/src/test/resources/clinical/saved_treatment_db", db)

        val treatmentDatabase = createFromPath("/Users/deniskumar/hmf/repos/actin/common/src/test/resources/clinical/saved_treatment_db")
        assertThat(treatmentDatabase).isNotNull()
        assertThat(treatmentDatabase.findTreatmentByName("Test name")).isNotNull()
        assertThat(treatmentDatabase.findTreatmentByName("Test name")).isNotNull()
    }
}
