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
 * make the plain patterns beside the disks vanish from the terminal. This composite shows both: every
 * slot holding anything but a disk keeps its row, empty ones included - that is where a terminal or an
 * uploader puts a pattern, so compressing them away would cost the write side its landing spots - while
 * the disks contribute their expanded rows through the disk mod's own view. Taking a plain row is an
 * ordinary take; taking a disk row charges a blank pattern and removes the recipe from its disk.</p>
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

    /**
     * The slots that are not disks, in slot order. Empty slots stay in: a terminal writes into those, and
     * that is also where an uploader looks for room.
     */
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
        return plainRowToSlot.length + diskRows.size();
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        if (isOutOfRange(slotIndex)) {
            return ItemStack.EMPTY;
        }
        return slotIndex < plainRowToSlot.length
                ? slots.getStackInSlot(plainRowToSlot[slotIndex])
                : diskRows.getStackInSlot(slotIndex - plainRowToSlot.length);
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        if (isOutOfRange(slotIndex)) {
            return;
        }
        if (slotIndex < plainRowToSlot.length) {
            slots.setItemDirect(plainRowToSlot[slotIndex], stack);
            return;
        }
        diskRows.setItemDirect(slotIndex - plainRowToSlot.length, stack);
    }

    @Override
    public ItemStack extractItem(int slotIndex, int amount, boolean simulate) {
        // The disk rows have their own take path - blank pattern charged, recipe removed from its disk -
        // so extraction cannot be left to the default implementation, which would only move the row's
        // stack around and leave the disk untouched.
        if (isOutOfRange(slotIndex)) {
            return ItemStack.EMPTY;
        }
        if (slotIndex < plainRowToSlot.length) {
            return slots.extractItem(plainRowToSlot[slotIndex], amount, simulate);
        }
        return diskRows.extractItem(slotIndex - plainRowToSlot.length, amount, simulate);
    }

    @Override
    public boolean isItemValid(int slotIndex, ItemStack stack) {
        if (isOutOfRange(slotIndex)) {
            return false;
        }
        if (slotIndex < plainRowToSlot.length) {
            return slots.isItemValid(plainRowToSlot[slotIndex], stack);
        }
        return false; // a terminal never writes onto a disk
    }

    @Override
    public ItemStack insertItem(int slotIndex, ItemStack stack, boolean simulate) {
        if (isOutOfRange(slotIndex)) {
            return stack;
        }
        if (slotIndex < plainRowToSlot.length) {
            return slots.insertItem(plainRowToSlot[slotIndex], stack, simulate);
        }
        return diskRows.insertItem(slotIndex - plainRowToSlot.length, stack, simulate);
    }

    @Override
    public int getSlotLimit(int slotIndex) {
        if (isOutOfRange(slotIndex)) {
            return 0;
        }
        if (slotIndex < plainRowToSlot.length) {
            return slots.getSlotLimit(plainRowToSlot[slotIndex]);
        }
        return 1; // a disk row holds one pattern; the disk view's default would allow a whole stack
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
        if (slotIndex < plainRowToSlot.length) {
            return slots.getSlotInv(plainRowToSlot[slotIndex]);
        }
        return diskRows.getSlotInv(slotIndex - plainRowToSlot.length);
    }

    @Override
    public void sendChangeNotification(int slotIndex) {
        if (isOutOfRange(slotIndex)) {
            return;
        }
        if (slotIndex < plainRowToSlot.length) {
            slots.sendChangeNotification(plainRowToSlot[slotIndex]);
            return;
        }
        diskRows.sendChangeNotification(slotIndex - plainRowToSlot.length);
    }

    private boolean isOutOfRange(int slotIndex) {
        return slotIndex < 0 || slotIndex >= size();
    }
}
