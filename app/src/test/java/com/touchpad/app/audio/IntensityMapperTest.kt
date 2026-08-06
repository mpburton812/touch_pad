package com.touchpad.app.audio

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IntensityMapperTest {
    @Test
    fun idleVisual_mapsToSilentAudio() {
        assertThat(IntensityMapper.visualToAudio(0.3f)).isEqualTo(0f)
    }

    @Test
    fun peakVisual_mapsToFullAudio() {
        assertThat(IntensityMapper.visualToAudio(1.0f)).isEqualTo(1f)
    }

    @Test
    fun midVisual_mapsProportionally() {
        // Halfway between idle 0.3 and peak 1.0 → 0.65.
        assertThat(IntensityMapper.visualToAudio(0.65f)).isWithin(0.001f).of(0.5f)
    }
}
