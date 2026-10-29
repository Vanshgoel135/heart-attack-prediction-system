package com.haps.android

import com.haps.android.model.ClinicalFormState
import org.junit.Assert.*
import org.junit.Test

class ClinicalFormStateTest {

    @Test
    fun testDefaultFormStateConversion() {
        val form = ClinicalFormState()
        val featureMap = form.toFeatureMap()

        assertEquals(55, featureMap["age"])
        assertEquals(1, featureMap["sex"])
        assertEquals(2, featureMap["cp"])
        assertEquals(135, featureMap["trestbps"])
        assertEquals(240, featureMap["chol"])
        assertEquals(0, featureMap["fbs"])
        assertEquals(1, featureMap["restecg"])
        assertEquals(145, featureMap["thalach"])
        assertEquals(0, featureMap["exang"])
        assertEquals(1.4, featureMap["oldpeak"])
        assertEquals(1, featureMap["slope"])
        assertEquals(1, featureMap["ca"])
        assertEquals(2, featureMap["thal"])
    }

    @Test
    fun testSampleHighRiskPreset() {
        val highRisk = ClinicalFormState.sampleHighRisk()
        val map = highRisk.toFeatureMap()

        assertEquals(63, map["age"])
        assertEquals(1, map["sex"])
        assertEquals(3, map["cp"])
        assertEquals(165, map["trestbps"])
        assertEquals(295, map["chol"])
        assertEquals(3.2, map["oldpeak"])
        assertEquals(3, map["ca"])
        assertEquals(1, map["smoking"])
        assertEquals(1, map["diabetes"])
        assertEquals(1, map["hypertension"])
    }

    @Test
    fun testSampleLowRiskPreset() {
        val lowRisk = ClinicalFormState.sampleLowRisk()
        val map = lowRisk.toFeatureMap()

        assertEquals(34, map["age"])
        assertEquals(0, map["sex"])
        assertEquals(0, map["cp"])
        assertEquals(116, map["trestbps"])
        assertEquals(182, map["chol"])
        assertEquals(0.2, map["oldpeak"])
        assertEquals(0, map["smoking"])
    }
}
