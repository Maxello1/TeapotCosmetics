tag @s add sf_have_sleeped
tag @s add sf_sleeping
effect give @s minecraft:slowness 15 255 true
effect give @s minecraft:blindness 15 0 true
execute if score @s food matches 20 run function sidetopia:features/sleep_stuff/full_sleep
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:speed_debuff
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:attack_speed_debuff
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:damage_debuff
attribute @s minecraft:generic.movement_speed modifier remove sleepfatige:block_break_debuff
tag @s remove sf_tired
 tag @s remove sf_tired2
 tag @s remove sf_tired3
scoreboard players set @s death_timer 0