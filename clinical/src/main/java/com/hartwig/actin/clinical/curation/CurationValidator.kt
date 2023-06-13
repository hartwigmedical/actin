package com.hartwig.actin.clinical.curation

import com.hartwig.actin.doid.DoidModel

class CurationValidator(private val doidModel: DoidModel) {
    fun isValidCancerDoidSet(doids: Set<String?>): Boolean {
        return hasValidDoids(doids, doidModel, DISEASE_OF_CELLULAR_PROLIFERATION_DOID)
    }

    fun isValidDiseaseDoidSet(doids: Set<String?>): Boolean {
        return hasValidDoids(doids, doidModel, DISEASE_DOID)
    }

    companion object {
        const val DISEASE_DOID = "4"
        const val DISEASE_OF_CELLULAR_PROLIFERATION_DOID = "14566"
        private fun hasValidDoids(doids: Set<String?>, doidModel: DoidModel, expectedParentDoid: String): Boolean {
            return if (doids.isEmpty()) {
                false
            } else doids.stream().allMatch { doid: String? ->
                doidModel.doidWithParents(
                    doid!!
                ).contains(expectedParentDoid)
            }
        }
    }
}