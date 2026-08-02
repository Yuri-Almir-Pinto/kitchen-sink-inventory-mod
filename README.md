# Unslotted

Unslotted is an inventory mod that removes a portion of the slots within your inventory, turning them into an area
where it's possible to store an unlimited amount of items, with one catch: items can be dragged around and overlap, turning
an inventory with way too many items into something that's difficult to sift through. So... get to organizing!

![Messy inventory](common/src/main/resources/assets/unslotted/icon.png)

**Important note**: The mod is in *beta*. Given it's reworking how the player inventory works, it is a dangerous mod
to be used in long-term worlds, and may not be fully stable yet. Specifically, the mod currently still stores much of
its data inside player NBTs directly, and doesn't break packets down, meaning that *excessively large* player inventories
or slotless crates could in theory corrupt a chunk or a player.

As long as you aren't collecting industrial amounts of different types of resources, or collecting items with a lot of NBT
information, you will probably never reach a dangerous point, but it's good to keep it in mind. These things will be fixed
over time! Once I'm confident enough in the mod, I will take it out of beta, but until then, make frequent backups of your worlds,
and update the mod with care.

## Other Content

### Slotless Crate

Alongside your inventory, there is also a new block, called the slotless crate, which works much like your inventory, but
as a storage block instead:

<details>
    <summary>Click here to expand</summary>    

<figure>
    <img alt="Moving items around the slotless crate" src="images/moving_into_slotless_crate.gif" width="50%">
    <br>
    <figcaption><i><small>You can drag and drop items from your inventory to the slotless crate and vice-versa.</small></i></figcaption>
</figure>

<br>

<figure>
    <img alt="Recipe of the slotless crate: four planks in the corners and four sticks between them." src="images/crafting_slotless_crate.gif" width="50%">
    <br>
    <figcaption><i><small>Four planks in the corners and four sticks in between makes a slotless crate.</small></i></figcaption>
</figure>

</details>


### Item Cluster

If a player dies, or a slotless crate gets destroyed, instead of dropping all items on the ground (which, depending on the
amount of items, might kill a server), the *item cluster* gets dropped instead, which may then be used on a slotless crate
or on yourself to retrieve the items! The Item Cluster *should* be compatible with gravestone mods (Though I have not tested them all):

<details>
    <summary>Click here to expand</summary>

<img alt="Item cluster description" src="images/item_cluster.png" width="50%">
<img alt="Using item cluster on a crate" src="images/cluster_item_crate.gif" width="50%">
<img alt="Using item cluster on self" src="images/item_cluster_inventory.gif" width="50%">

</details>

### Reset Magnet

There is a button in the corner of the slotless inventory, which when clicked, will pull all items that are *outside* your
view of the inventory, and thus impossible to grab. If you ever lose an item by dragging it outside, just click there!

<details>
    <summary>Click here to expand</summary>

<figure>
    <img alt="Clicking the reset icon" src="images/reset_action.gif" width="50%">
    <br>
    <figcaption><i><small>Not holding shift only resets the position of what's outside your vision</small></i></figcaption>
</figure>

<br>

<figure>
    <img alt="Clicking the reset icon while holding shift" src="images/reset_all_action.gif" width="50%">
    <br>
    <figcaption><i><small>If you hold shift, you will reset the positions of <b>all</b> items in your inventory</small></i></figcaption>
</figure>

</details>

## Demonstration

Here are some gifs showing actual usage of the mod! Keep in mind the mod is a modification of one of the core systems
of the game, so incompatibilities are bound to be found. I will try to keep them to a minimum however, and fix them
as I find them! The mod should feel as close as possible to the player's vanilla inventory in most of its interactions, and
also be compatible with modded GUIs that show the player's inventory (Although it will look weird if the modded GUI does not
follow minecraft's art style. I have a few ideas on how to "fix" it, but they are definitely experimental).

<details>
    <summary>Click here to expand</summary>

<img alt="Moving items" src="images/move_action.gif" width="50%"/>
<img alt="Putting items" src="images/put_action.gif" width="50%">
<img alt="Shift clicking" src="images/shift_click_action.gif" width="50%"/>
<img alt="Pick up all" src="images/pick_up_all_action.gif" width="50%"/>
<img alt="Recipe book" src="images/recipe_book_action.gif" width="50%"/>
<img alt="Pick up world" src="images/pickup_world_action.gif" width="50%"/>

</details>

## Roadmap

### Status Legend
* 🟢 **Done** - Already released
* 🟡 **In Progress** - Currently coding/testing
* 🔵 **Planned** - Planned, to be developed
* 🔴 **Won't do** - Will not do for whatever reason

### 📋 Feature & Update Tracker

| Status | Feature / Idea | Reason / About |
| :---: | :--- | :--- |
| 🟢 | Core Slotless Inventory Mechanic | --- |
| 🟢 | Slotless Crate Block | Content | Initial Release | --- |
| 🟡 | Fix Storing Slotless Storage Data in Nbt | --- |
| 🔵 | Disable Changes on Player Inventory | Suggestion by @unilock ([#1](https://github.com/Yuri-Almir-Pinto/unslotted-mod/issues/1)) |
| 🔵 | Friendly Config Toggles | --- |
| 🔵 | Add Toggleable "Culling" on Large Item Piles | --- |

---

> *Got a feature request or bug? Feel free to open an issue on [GitHub](https://github.com/Yuri-Almir-Pinto/unslotted-mod) or discuss it in our `#suggestions` channel in [Discord](https://discord.gg/8YbVjm7ztV). Discord is also open for any questions.*
