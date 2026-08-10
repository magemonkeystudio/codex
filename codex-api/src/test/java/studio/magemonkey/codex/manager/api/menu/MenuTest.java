package studio.magemonkey.codex.manager.api.menu;

import org.junit.jupiter.api.Test;

/**
 * Covers the page-offset math shared by {@link Menu#open(int)} (render) and
 * {@link Menu#getSlotOnCurrentPage(int)} (click resolution). Regression test for
 * https://github.com/magemonkeystudio/codex/issues/156, where render and click
 * disagreed about what a display slot meant on page 2+.
 */
class MenuTest {

    @Test
    void toVirtualSlot_firstPageIsUnshifted() {
        assert Menu.toVirtualSlot(0, 54, 0) == 0;
        assert Menu.toVirtualSlot(0, 54, 5) == 5;
        assert Menu.toVirtualSlot(0, 54, 53) == 53;
    }

    @Test
    void toVirtualSlot_laterPagesAreShiftedByPageSize() {
        assert Menu.toVirtualSlot(1, 54, 0) == 54;
        assert Menu.toVirtualSlot(1, 54, 5) == 59;
        assert Menu.toVirtualSlot(2, 54, 5) == 113;
    }

    @Test
    void toVirtualSlot_matchesAcrossRenderAndClickForSameDisplaySlot() {
        // The exact scenario from #156: a display slot on page 2 must resolve to the
        // same virtual slot whether it's being rendered (open) or clicked (MenuManager).
        int pageSize    = 9;
        int displaySlot = 3;
        int page        = 2;

        int renderVirtualSlot = Menu.toVirtualSlot(page, pageSize, displaySlot);
        int clickVirtualSlot  = Menu.toVirtualSlot(page, pageSize, displaySlot);

        assert renderVirtualSlot == clickVirtualSlot;
        assert renderVirtualSlot == 21;
    }

    @Test
    void toVirtualSlot_differentPagesDoNotCollideForSameDisplaySlot() {
        int pageSize    = 9;
        int displaySlot = 3;

        int page1Slot = Menu.toVirtualSlot(0, pageSize, displaySlot);
        int page2Slot = Menu.toVirtualSlot(1, pageSize, displaySlot);

        assert page1Slot != page2Slot;
    }
}
