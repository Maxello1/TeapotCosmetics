execute as @s run tag @s add hs_already_swimming
execute as @s run attribute @s minecraft:generic.movement_speed modifier add harderswim:speed_debuff -0.5 add_multiplied_total
execute as @s run attribute @s minecraft:generic.attack_damage modifier add harderswim:damage_debuff -0.5 add_multiplied_total
execute as @s run attribute @s minecraft:generic.attack_speed modifier add harderswim:attack_speed_debuff -0.5 add_multiplied_total
execute as @s run attribute @s minecraft:player.block_break_speed modifier add harderswim:block_break_debuff -1 add_multiplied_total
execute as @s run effect give @s hunger infinite 2 true