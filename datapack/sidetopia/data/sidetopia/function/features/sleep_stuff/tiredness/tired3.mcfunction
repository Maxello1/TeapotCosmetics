tag @s add sf_tired3
tag @s remove sf_tired
tag @s remove sf_tired2
tag @s remove sf_have_sleeped
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:speed_debuff
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:attack_speed_debuff
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:damage_debuff
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:block_break_debuff
execute as @s[tag=!celestite_clarity] run function sidetopia:features/sleep_stuff/tiredness/tired_normal_3
execute as @s[tag=celestite_clarity] run function sidetopia:features/sleep_stuff/tiredness/tired_celestite_3
effect give @s minecraft:darkness 90 1 true
title @s times 10 80 20
title @s title {"text":"WARNING","color":"red","bold":true}
title @s subtitle {"text":"You will die in 20 minutes if you don't sleep","color":"white"}
scoreboard players set @s death_timer 1201