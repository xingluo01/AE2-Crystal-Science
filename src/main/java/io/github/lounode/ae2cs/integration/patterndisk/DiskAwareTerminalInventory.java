package io.github.lounode.ae2cs.integration.patterndisk;

import java.util.ArrayList;

import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;

import io.github.lounode.ae2pattern.api.PatternDiskApi;

/**
 * The terminal view of a provider whose pattern slots serve two purposes: they hold encoded patterns and,
 * for the disks, whole disks full of them.
 *
 * <p>AE2's pattern access terminal can read exactly one inventory, so handing it the disk view alone would
 * make the plain patterns beside the disks vanish from the terminal. This composite shows both: the disk
 * rows come first, then every slot that is not a disk keeps its row, empty ones included - that is where a
 * terminal writes. The disks lead because the upload path walks the rows from index 0 and stops at the
 * first one that takes the pattern; with the empty slots ahead of them an upload would land in a plain
 * slot instead of on a disk. Taking a plain row is an ordinary take; taking a disk row charges a blank
 * pattern and removes the recipe from its disk.</p>
 *
 * <p>Rows are laid out once, on construction - a terminal keeps the slot count it opened with, so a view
 * is rebuilt rather than rearranged when the disks change.</p>
 */
final class DiskAwareTerminalInventory implements InternalInventory {

    private final InternalInventory slots;
    private final InternalInventory diskRows;
    private final int[] plainRowToSlot;

    DiskAwareTerminalInventory(InternalInventory slots, InternalInventory diskRows) {
        this.slots = slots;
        this.diskRows = diskRows;
        this.plainRowToSlot = plainRows(slots);
    }

    /** The slots that are not disks, in slot order. Empty slots stay in - a terminal writes into those. */
    private static int[] plainRows(InternalInventory slots) {
        var rows = new ArrayList<Integer>();
        for (int slot = 0; slot < slots.size(); slot++) {
            if (!PatternDiskApi.isPatternDisk(slots.getStackInSlot(slot))) {
                rows.add(slot);
            }
        }
        var result = new int[rows.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = rows.get(i);
        }
        return result;
    }

    @Override
    public int size() {
        return diskRows.size() + plainRowToSlot.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        if (isOutOfRange(slotIndex)) {
            return ItemStack.EMPTY;
        }
        return isDiskRow(slotIndex)
                ? diskRows.getStackInSlot(slotIndex)
                : slots.getStackInSlot(plainRowToSlot[slotIndex - diskRows.size()]);
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        if (isOutOfRange(slotIndex)) {
            return;
        }
        if (isDiskRow(slotIndex)) {
            diskRows.setItemDirect(slotIndex, stack);
            return;
        }
        slots.setItemDirect(plainRowToSlot[slotIndex - diskRows.size()], stack);
    }

    @Override
    public ItemStack extractItem(int slotIndex, int amount, boolean simulate) {
        // The disk rows have their own take path - blank pattern charged, recipe removed from its disk -
        // so extraction cannot be left to the default implementation, which would only move the row's
        // stack around and leave the disk untouched.
        if (isOutOfRange(slotIndex)) {
            return ItemStack.EMPTY;
        }
        if (isDiskRow(slotIndex)) {
            return diskRows.extractItem(slotIndex, amount, simulate);
        }
        return slots.extractItem(plainRowToSlot[slotIndex - diskRows.size()], amount, simulate);
    }

    @Override
    public boolean isItemValid(int slotIndex, ItemStack stack) {
        if (isOutOfRange(slotIndex) || isDiskRow(slotIndex)) {
            // A disk row says no, exactly as the disk view does: every path that writes onto a disk goes
            // through insertItem. An uploader that probed isItemValid first would therefore skip the disk
            // rows and settle for a plain slot.
            return false;
        }
        return slots.isItemValid(plainRowToSlot[slotIndex - diskRows.size()], stack);
    }

    @Override
    public ItemStack insertItem(int slotIndex, ItemStack stack, boolean simulate) {
        if (isOutOfRange(slotIndex)) {
            return stack;
        }
        if (isDiskRow(slotIndex)) {
            return diskRows.insertItem(slotIndex, stack, simulate);
        }
        return slots.insertItem(plainRowToSlot[slotIndex - diskRows.size()], stack, simulate);
    }

    @Override
    public int getSlotLimit(int slotIndex) {
        if (isOutOfRange(slotIndex)) {
            return 0;
        }
        if (isDiskRow(slotIndex)) {
            return 1; // a disk row holds one pattern; the disk view's default would allow a whole stack
        }
        return slots.getSlotLimit(plainRowToSlot[slotIndex - diskRows.size()]);
    }

    /**
     * Routed rather than left to the default, which would wrap <em>this</em> composite in a single-slot
     * proxy and so bypass the disk view's own guard. That guard is what refuses a take while the network
     * holds no blank pattern to charge for it; without it a terminal could hand out a copy of a recipe
     * whose removal from its disk then fails.
     */
    @Override
    public InternalInventory getSlotInv(int slotIndex) {
        if (isOutOfRange(slotIndex)) {
            return InternalInventory.empty();
        }
        if (isDiskRow(slotIndex)) {
            return diskRows.getSlotInv(slotIndex);
        }
        return slots.getSlotInv(plainRowToSlot[slotIndex - diskRows.size()]);
    }

    @Override
    public void sendChangeNotification(int slotIndex) {
        if (isOutOfRange(slotIndex)) {
            return;
        }
        if (isDiskRow(slotIndex)) {
            diskRows.sendChangeNotification(slotIndex);
            return;
        }
        slots.sendChangeNotification(plainRowToSlot[slotIndex - diskRows.size()]);
    }

    /**
     * The disks sit at the front on purpose: the upload path walks rows from index 0 and stops at the first
     * one that accepts the pattern, so leading with them is what makes an upload land on a disk rather than
     * in one of the provider's own empty slots.
     */
    private boolean isDiskRow(int slotIndex) {
        return slotIndex >= 0 && slotIndex < diskRows.size();
    }

    private boolean isOutOfRange(int slotIndex) {
        return slotIndex < 0 || slotIndex >= size();
    }
}
