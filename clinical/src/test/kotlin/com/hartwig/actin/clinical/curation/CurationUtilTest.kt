package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.CurationUtil.capitalizeFirstLetterOnly
import com.hartwig.actin.clinical.curation.CurationUtil.fullTrim
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurationUtilTest {

    @Test
    fun `Should capitalize first letter only`() {
        assertThat(capitalizeFirstLetterOnly("hi")).isEqualTo("Hi")
        assertThat(capitalizeFirstLetterOnly("Hi")).isEqualTo("Hi")
        assertThat(capitalizeFirstLetterOnly("hI")).isEqualTo("Hi")
        assertThat(capitalizeFirstLetterOnly("h")).isEqualTo("H")
        assertThat(capitalizeFirstLetterOnly("H")).isEqualTo("H")
        assertThat(capitalizeFirstLetterOnly("")).isEqualTo("")
    }

    @Test
    fun `Should trim extra spaces between words`() {
        assertThat(fullTrim(Strings.EMPTY)).isEqualTo("")
        assertThat(fullTrim("hi")).isEqualTo("hi")
        assertThat(fullTrim("this is a normal sentence")).isEqualTo("this is a normal sentence")
        assertThat(fullTrim(" this     is  a weird   sentence  ")).isEqualTo("this is a weird sentence")
    }

    @Test
    fun `Should convert DOIDs to set`() {
        assertConversionToSet("123", "456", CurationUtil::toDOIDs)
    }

    @Test
    fun `Should convert categories to set`() {
        assertConversionToSet("category1", "category2", CurationUtil::toCategories)
    }

    @Test
    fun `Should convert to set`() {
        assertConversionToSet("treatment1", "treatment2", CurationUtil::toSet)
    }

    private fun assertConversionToSet(element1: String, element2: String, convert: (String) -> Set<String>) {
        assertThat(convert("")).isEmpty()
        assertThat(convert(element1)).isEqualTo(setOf(element1))

        val multiple = convert("$element1;$element2")
        assertThat(multiple).containsExactly(element1, element2)
    }
}