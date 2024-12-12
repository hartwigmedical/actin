package com.hartwig.actin.icd

import com.hartwig.actin.datamodel.clinical.IcdCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IcdModelTest {

    private val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("valid", "extension", "validParent", "extensionParent"))

    @Test
    fun `Should handle title validation correctly for regular and extended titles`() {
        assertThat(icdModel.isValidIcdTitle("validTitle")).isTrue()
        assertThat(icdModel.isValidIcdTitle("extensionTitle")).isTrue()
        assertThat(icdModel.isValidIcdTitle("validTitle&extensionTitle")).isTrue()
        assertThat(icdModel.isValidIcdTitle("validTitle|extensionTitle")).isFalse()
        assertThat(icdModel.isValidIcdTitle("invalidTitle")).isFalse()
        assertThat(icdModel.isValidIcdTitle("validTitle&invalidTitle")).isFalse()
    }

    @Test
    fun `Should successfully resolve codes for extended and non-extended titles`() {
        assertThat(icdModel.resolveCodeForTitle("validTitle&extensionTitle")).isEqualTo(IcdCode("validCode", "extensionCode"))
        assertThat(icdModel.resolveCodeForTitle("validTitle")).isEqualTo(IcdCode("validCode", null))
    }

    @Test
    fun `Should successfully resolve code with parents`() {
        assertThat(icdModel.returnCodeWithParents("validCode")).containsExactly("validParentCode", "validCode")
        assertThat(icdModel.returnCodeWithParents("extensionCode")).containsExactly(
            "extensionParentCode",
            "extensionCode"
        )
    }

    @Test
    fun `Should correctly resolve titles for main and extension codes`() {
        assertThat(icdModel.resolveTitleForCode(IcdCode("validCode", "extensionCode"))).isEqualTo("validTitle & extensionTitle")
        assertThat(icdModel.resolveTitleForCode(IcdCode("validCode", null))).isEqualTo("validTitle")
    }
}