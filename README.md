# WGItemFrameBreakFlags
Worldguard plugin for protecting against Item Frame breaking.
Requires Worldguard 7 and a 1.16+ server. Other configurations are untested, other plugins may interfere with this one.

This plugin was specifically made because if the following behavior in spigot https://hub.spigotmc.org/jira/browse/SPIGOT-3999
which means Worldguard does not catch Physics events causes by Boats, as well as other Removal Causes ( https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/hanging/HangingBreakEvent.RemoveCause.html )

This plugin serves to cover these specific cases by adding flags for each but cannot and will not implement any more exhaustive checks on the source of the event.


Adds:

* `default-item-frame-destroy` for unknown causes
* `explosion-item-frame-destroy` for explosion-caused item frame destruction
* `obstruction-item-frame-destroy` for when block placement would cause the item frame to break
* `physics-item-frame-destroy` for when physics or bounding boxes (like those of a Boat) cause the item frame to break.
