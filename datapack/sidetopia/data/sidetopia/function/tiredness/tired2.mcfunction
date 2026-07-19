tag @s add sf_tired2
tag @s remove sf_tired
tag @s remove sf_tired3
tag @s remove sf_have_sleeped
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:speed_debuff
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:attack_speed_debuff
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:damage_debuff
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:block_break_debuff
attribute @s minecraft:generic.movement_speed modifier add sleepfatige:speed_debuff -0.4 add_multiplied_total
attribute @s minecraft:generic.attack_damage modifier add sleepfatige:damage_debuff -0.4 add_multiplied_total
attribute @s generic.attack_speed modifier add sleepfatige:attack_speed_debuff -0.5 add_multiplied_total
attribute @s minecraft:player.block_break_speed modifier add sleepfatige:block_break_debuff -0.4 add_multiplied_total
effect give @s minecraft:darkness 60 1 true