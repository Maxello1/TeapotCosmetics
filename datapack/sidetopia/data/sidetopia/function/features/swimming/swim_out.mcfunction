tag @s remove hs_in_water
tag @s remove hs_already_swimming
tag @s add hs_need_drying
execute as @s run effect clear @s hunger
execute as @a[tag=hs_need_drying] run attribute @s minecraft:player.block_break_speed modifier remove harderswim:block_break_debuff
execute as @s run attribute @s minecraft:player.block_break_speed modifier add harderswim:block_break_debuff -0.5 add_multiplied_total
scoreboard players add @s hs_drying 8
schedule function sidetopia:features/swimming/drying 8s