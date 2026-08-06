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
        assertThat(PadCatalog.pads[0].frequencyHz).isWithin(0.01f).of(261.63f)
        assertThat(PadCatalog.pads[5].frequencyHz).isWithin(0.01f).of(440.00f)
        assertThat(PadCatalog.pads[7].frequencyHz).isWithin(0.01f).of(523.25f)
    }

    @Test
    fun envelopeTiming_matchesRequirements() {
        // Idle is 10% transparent (alpha 0.9) per product UX update.
        assertThat(PadCatalog.IDLE_INTENSITY).isEqualTo(0.9f)
        assertThat(PadCatalog.ATTACK_MS).isEqualTo(250)
        assertThat(PadCatalog.HOLD_MS).isEqualTo(1000)
        assertThat(PadCatalog.DECAY_MS).isEqualTo(500)
    }
}
