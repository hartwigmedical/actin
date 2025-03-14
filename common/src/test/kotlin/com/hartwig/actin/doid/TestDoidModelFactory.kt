package com.hartwig.actin.doid

import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory.createMinimalTestDoidManualConfig
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory.createWithOneMainCancerDoid

object TestDoidModelFactory {

    fun createMinimalTestDoidModel(): DoidModel {
        return create()
    }

    fun createWithOneParentChild(parent: String, child: String): DoidModel {
        return create(mapOf(child to listOf(parent)))
    }

    fun createWithParentChildAndTermPerDoidMaps(childToParentMap: Map<String, String>, doidToTermMap: Map<String, String>): DoidModel {
        return create(
            childToParentsMap = childToParentMap.mapValues { listOf(it.value) },
            termPerDoidMap = doidToTermMap,
            doidPerLowerCaseTermMap = doidToTermMap.entries.associate { (doid, term) -> term.lowercase() to doid }
        )
    }

    fun createWithChildToParentMap(childToParentMap: Map<String, String>): DoidModel {
        return create(childToParentMap.mapValues { listOf(it.value) })
    }

    fun createWithMainCancerTypeAndChildToParentsMap(mainCancerDoid: String, childToParentsMap: Map<String, List<String>>): DoidModel {
        return create(childToParentsMap).copy(doidManualConfig = createWithOneMainCancerDoid(mainCancerDoid))
    }

    fun createWithMainCancerTypeAndChildToParentMap(mainCancerDoid: String, childToParentMap: Map<String, String>): DoidModel {
        return createWithChildToParentMap(childToParentMap).copy(doidManualConfig = createWithOneMainCancerDoid(mainCancerDoid))
    }

    fun createWithOneDoidAndTerm(doid: String, term: String): DoidModel {
        return create(termPerDoidMap = mapOf(doid to term), doidPerLowerCaseTermMap = mapOf(term.lowercase() to doid))
    }

    fun createWithTwoDoidsAndTerms(doids: List<String>, terms: List<String>): DoidModel {
        return create(
            termPerDoidMap = mapOf(doids[0] to terms[0], doids[1] to terms[1]),
            doidPerLowerCaseTermMap = mapOf(terms[0].lowercase() to doids[0], terms[1].lowercase() to doids[1])
        )
    }

    fun createWithDoidManualConfig(config: DoidManualConfig): DoidModel {
        return create(config = config)
    }

    private fun create(
        childToParentsMap: Map<String, List<String>> = emptyMap(),
        termPerDoidMap: Map<String, String> = emptyMap(),
        doidPerLowerCaseTermMap: Map<String, String> = emptyMap(),
        config: DoidManualConfig = createMinimalTestDoidManualConfig()
    ): DoidModel {
        return DoidModel(childToParentsMap, termPerDoidMap, doidPerLowerCaseTermMap, config)
    }
}
