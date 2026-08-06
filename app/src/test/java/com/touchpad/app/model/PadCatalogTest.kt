package com.touchpad.app.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PadCatalogTest {
    @Test
    fun pads_matchRequirementsTable() {
        assertThat(PadCatalog.pads).hasSize(8)
        assertThat(PadCatalog.pads.map { it.noteName }).containsExactly(
            "C4", "D4", "E4", "F4", "G4", "A4", "B4", "C5",
        ).inOrder()
    }

    @Test
    fun idleAndAttack_matchProductDefaults() {
        assertThat(PadCatalog.IDLE_INTENSITY).isEqualTo(0.3f)
        assertThat(PadCatalog.ATTACK_MS).isEqualTo(250)
    }
}

class DecayTimingTest {
    @Test
    fun sliderMapsToReleaseWindow() {
        assertThat(DecayTiming.sliderToMs(0f)).isEqualTo(DecayTiming.MIN_MS)
        assertThat(DecayTiming.sliderToMs(1f)).isEqualTo(DecayTiming.MAX_MS)
        assertThat(DecayTiming.sliderToMs(0.5f)).isEqualTo(
            (DecayTiming.MIN_MS + DecayTiming.MAX_MS) / 2,
        )
    }
}
