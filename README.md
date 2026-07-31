<style>
    .zoom-60 {
        zoom: 0.6;
    }

    .zoom-50 {
        zoom: 0.5;
    }

    figure {
      display: inline-block; /* Keeps the container wrapped tightly around the image */
      text-align: center;   /* Centers the annotation text */
      margin: 0;
    }
    
    figcaption {
      font-size: 0.85rem;
      color: #666;
      font-style: italic;
      margin-top: 1px; /* Spacing between image and text */
    }
</style>

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

<details>
    <summary>Click here to expand</summary>    

Alongside your inventory, there is also a new block, called the slotless crate, which works much like your inventory, but
as a storage block instead:

<figure>
    <img alt="Moving items around the slotless crate" src="images/moving_into_slotless_crate.gif" class="zoom-60">
    <figcaption>You can drag and drop items from your inventory to the slotless crate and vice-versa.</figcaption>
</figure>

<figure>
    <img alt="Recipe of the slotless crate: four planks in the corners and four sticks between them." src="images/crafting_slotless_crate.gif" class="zoom-60">
    <figcaption>Four planks in the corners and four sticks in between makes a slotless crate.</figcaption>
</figure>

</details>


### Item Cluster

<details>
    <summary>Click here to expand</summary>

If a player dies, or a slotless crate gets destroyed, instead of dropping all items on the ground (which, depending on the
amount of items, might kill a server), the *item cluster* gets dropped instead, which may then be used on a slotless crate
or on yourself to retrieve the items! The Item Cluster *should* be compatible with gravestone mods (Though I have not tested them all):

<img alt="Item cluster description" src="images/item_cluster.png" class="zoom-50">
<img alt="Using item cluster on a crate" src="images/cluster_item_crate.gif" class="zoom-50">
<img alt="Using item cluster on self" src="images/item_cluster_inventory.gif" class="zoom-50">

</details>

### Reset Magnet

<details>
    <summary>Click here to expand</summary>

There is a button in the corner of the slotless inventory, which when clicked, will pull all items that are *outside* your
view of the inventory, and thus impossible to grab. If you ever lose an item by dragging it outside, just click there!

<figure>
    <img alt="Clicking the reset icon" src="images/reset_action.gif" class="zoom-60">
    <figcaption>Not holding shift only resets the position of what's outside your vision</figcaption>
</figure>
<figure>
    <img alt="Clicking the reset icon while holding shift" src="images/reset_all_action.gif" class="zoom-60">
    <figcaption>If you hold shift, you will reset the positions of <b>all</b> items in your inventory</figcaption>
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

<img alt="Moving items" src="images/move_action.gif" class="zoom-50"/>
<img alt="Putting items" src="images/put_action.gif" class="zoom-50"/>
<img alt="Shift clicking" src="images/shift_click_action.gif" class="zoom-50"/>
<img alt="Pick up all" src="images/pick_up_all_action.gif" class="zoom-50"/>
<img alt="Recipe book" src="images/recipe_book_action.gif" class="zoom-50"/>
<img alt="Pick up world" src="images/pickup_world_action.gif" class="zoom-50"/>

</details>