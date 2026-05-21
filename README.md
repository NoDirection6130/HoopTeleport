# HoopTeleport

## The Problem

Every Minecraft server has `/sethome`. It works, sure, but it also makes the world feel smaller than it is. Teleport anywhere, anytime, for free. No cost, no risk, no effort. It removes one of the core challenges of survival: actually getting back to where you were.

**Hoop** was built as an answer to that. A teleport system that feels fair. One that gives players the convenience of a home point, but asks something in return.

---

## The Solution

Hoop is a lightweight teleport plugin for Paper 1.21.1 that replaces the classic `/sethome` with a simple, progression-based system. Players earn their teleports by spending XP levels, choose where to place them, and upgrade them over time to increase their range. It's approachable, but it's not free.

---

## How It Works

Type `/hoop` to open your personal teleport GUI. You'll see 3 empty slots (Hoppers) and a delete button (Barrier Block).

<img width="371" height="108" alt="Screenshot 2026-05-22 at 01 04 57" src="https://github.com/user-attachments/assets/8b7b0a59-3428-4564-9ae9-6d42c159f167" />

<img width="649" height="406" alt="Screenshot 2026-05-22 at 01 05 14" src="https://github.com/user-attachments/assets/4fae9936-dd2b-4fdc-b598-86f79a5f620f" />

### Buying a Teleport
Right-click an empty slot to purchase a teleport at your current location. This costs **20 XP levels** and creates an **Iron Teleport** which is tied to the exact coordinates where you were standing.

<img width="783" height="406" alt="Screenshot 2026-05-22 at 01 05 51" src="https://github.com/user-attachments/assets/eafdeef9-18b3-4a3a-9ab3-14a676eb5c68" />

### Teleport Tiers
There are 3 tiers, each with a different maximum range:

| Tier | Block | Range | Cost |
|------|-------|-------|------|
| I | Iron Block | 500 blocks | 20 XP levels |
| II | Gold Block | 1000 blocks | +20 XP levels (upgrade from Iron) |
| III | Diamond Block | 2000 blocks | +30 XP levels (upgrade from Gold) |

To upgrade, right-click an existing teleport in the GUI. A confirmation screen will appear — click **Yes (Green Wool)** to confirm or **No (Red Wool)** to cancel.

<img width="581" height="406" alt="Screenshot 2026-05-22 at 01 06 20" src="https://github.com/user-attachments/assets/1ac20ca3-afbb-40dd-be24-350fcd1326b9" />

<img width="589" height="406" alt="Screenshot 2026-05-22 at 01 06 45" src="https://github.com/user-attachments/assets/7ea410a2-36d9-4e98-bc8e-2970174eb75f" />

### Using a Teleport
Left-click a purchased teleport in the GUI to initiate teleportation. A **10-second countdown** will begin — if you move during this time, the teleport is cancelled. Upon arrival, you receive **Resistance III** for 3 seconds.

### Removing a Teleport
Click the **Barrier Block** in the GUI to enter delete mode — it will turn into a **Skeleton Skull**. Click any teleport to remove it. Note: **no XP is refunded** upon deletion.

<img width="714" height="406" alt="Screenshot 2026-05-22 at 01 06 59" src="https://github.com/user-attachments/assets/a9449f61-a2b1-4981-920c-551630efa513" />

<img width="535" height="406" alt="Screenshot 2026-05-22 at 01 07 16" src="https://github.com/user-attachments/assets/340f61ba-7f02-40d0-9de9-45676e4cfe11" />

### Dimension Support
Teleports are bound to the dimension they were created in (Overworld, Nether, The End). You cannot teleport across dimensions.

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/hoop` | Open your teleport GUI | `hoop.use` |
| `/hoop <player>` | Open another player's GUI (admin) | `hoop.admin` |

---

## Permissions (LuckPerms compatible)

| Permission | Description | Default |
|------------|-------------|---------|
| `hoop.use` | Access to `/hoop` | `true` |
| `hoop.admin` | Manage other players' hoops via `/hoop <player>` | `op` |

Example LuckPerms setup:
```
lp group member permission set hoop.use true
lp group admin permission set hoop.use true
lp group admin permission set hoop.admin true
```

---

## Additional Info

- **Language:** 100% English - all in-game messages are in English
- **No dependencies:** No external libraries required, works out of the box
- **Paper 1.21.1** - built and tested on Paper
- **Spigot compatible** - should work on Spigot as well
- **Data persistence** - all teleport data is saved to `plugins/Hoop/hoops.yml` and survives server restarts

---

## Planned for Future Updates

- Greater configuration options (costs, ranges, cooldown duration)
- More quality of life improvements

---

*Part of the nodirection6130 plugin collection.*
