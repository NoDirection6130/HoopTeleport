package pl.nodirection6130.hoop;

import java.util.UUID;

public class PlayerHoop {

    private final UUID ownerUUID;
    private final HoopSlot[] slots = new HoopSlot[3];

    public PlayerHoop(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        for (int i = 0; i < 3; i++) slots[i] = new HoopSlot();
    }

    public UUID getOwnerUUID() { return ownerUUID; }

    public HoopSlot getSlot(int index) { return slots[index]; }

    public void setSlot(int index, HoopSlot slot) { slots[index] = slot; }

    public HoopSlot[] getSlots() { return slots; }
}
