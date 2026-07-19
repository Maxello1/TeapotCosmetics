execute as @a[tag=!hs_in_water,tag=!inmune] at @s if block ~ ~ ~ minecraft:water run tag @s add hs_in_water
execute as @a[tag=hs_in_water, tag=!hs_already_swimming] run function sidetopia:features/swimming/swimming
execute as @a[tag=hs_in_water] at @s unless block ~ ~ ~ minecraft:water run function sidetopia:features/swimming/swim_out
schedule function sidetopia:features/swimming/swim_check 1s