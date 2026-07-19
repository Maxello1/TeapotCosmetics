scoreboard players remove @a[scores={death_timer=2..}] death_timer 1
scoreboard players remove @a[scores={bleed_timer=1..}] bleed_timer 1
scoreboard players remove @a[scores={heat_amount=1..}] heat_amount 1
scoreboard players remove @a[scores={adrenaline_timer=1..}] adrenaline_timer 1
scoreboard players remove @a[scores={hs_drying=1..}] hs_drying 1

execute as @a if items entity @s container.* minecraft:lava_bucket run function sidetopia:features/bucket/has_lava_bucket
execute as @a if items entity @s container.* minecraft:water_bucket run function sidetopia:features/bucket/has_water_bucket
execute as @a[tag=celestite_clarity] unless items entity @s armor.head *[minecraft:enchantments={levels:{"sidetopia:celestite_clarity":1}}] run tag @s remove celestite_clarity

execute as @a[scores={heat_amount=3..}] run function sidetopia:features/bucket/heat_damage
execute at @a[tag=quartz_bleed] run particle minecraft:dust{color:[0.8,0.0,0.0],scale:1.5} ~ ~0.5 ~ 0.20 0.3 0.20 0.1 10

schedule function sidetopia:seconds_timer 1s