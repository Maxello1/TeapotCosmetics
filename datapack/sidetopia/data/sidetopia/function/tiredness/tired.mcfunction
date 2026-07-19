tag @s add sf_tired
tag @s remove sf_have_sleeped
tag @s remove sf_tired2
tag @s remove sf_tired3
attribute @s minecraft:generic.movement_speed modifier add sleepfatige:speed_debuff -0.2 add_multiplied_total
attribute @s minecraft:player.block_break_speed modifier add sleepfatige:block_break_debuff -0.2 add_multiplied_total
effect give @s minecraft:darkness 30 1 true