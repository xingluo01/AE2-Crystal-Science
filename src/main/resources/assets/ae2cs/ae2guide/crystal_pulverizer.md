---
navigation:
  parent: index.md
  title: Crystal Pulverizer
  icon: ae2cs:crystal_pulverizer
  position: 70
item_ids:
  - ae2cs:crystal_pulverizer
  - ae2cs:resonating_pulverizer_factory
---

# Crystal Pulverizer

<Row gap="16">
  <BlockImage id="ae2cs:crystal_pulverizer" scale="2" />
  <BlockImage id="ae2cs:resonating_pulverizer_factory" scale="2" />
</Row>

The **Crystal Pulverizer** is a mid-game crystal processing machine provided by AECS.
It is designed to efficiently convert ores or crystal-based raw materials into their corresponding dusts.
Functionally, it inherits the processing system of the Quartz Grindstone,
while significantly improving operating efficiency and automation capabilities.

---

## Functional Role

The Crystal Pulverizer is positioned as an upgraded replacement for the Quartz Grindstone:

- Executes the same recipe types as the Quartz Grindstone
- Designed for mid-game and later automated production environments

Under identical recipe conditions, the Crystal Pulverizer completes pulverizing operations at a much higher processing speed.

---

## Power and Efficiency

The Crystal Pulverizer supports **electric power only** and does not allow manual operation.

- Installing **Speed Cards** can greatly increase processing throughput per unit time

With multiple Speed Cards installed, the Crystal Pulverizer is capable of meeting the demands of large-scale dust production.

---

## Machine Characteristics

The Crystal Pulverizer has the following general machine properties:

- Supports the **[Side Configuration](side_config.md)** system
- Can automatically draw power from the connected **ME network**
- Can also receive power via external energy cables
- **Participates in channel transmission**

---

## Upgraded Variant: Resonating Pulverizer Factory

The **Resonating Pulverizer Factory** is the upgraded variant of this machine and runs the **same recipes**.
Its power, upgrade and automation behavior is otherwise identical, so only the slots and throughput differ:
it replaces the 1 input / 4 output slots with two 3x3 grids and processes up to **32** items at a time.

The gain does not come from a faster operation - finishing a whole batch takes about as long as this machine takes
for one item, so throughput scales with the batch size.
The Speed Cards covered above keep their effect and additionally raise the batch size (**+4** each),
as does the **Meteorite Overclock Card** (**+16**), up to the cap of **32**.
