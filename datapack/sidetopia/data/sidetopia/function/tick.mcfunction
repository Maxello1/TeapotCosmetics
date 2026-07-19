# This is for sleep features
execute as @a[tag=!sf_init] run function sidetopia:init
execute as @a[scores={rest=0}, tag=!sf_sleeping] run function sidetopia:features/sleep_stuff/sleep
execute as @a[scores={rest=1..},tag=sf_sleeping] run tag @s remove sf_sleeping
execute as @a[scores={food=0..15,rest=1..100}] run function sidetopia:features/sleep_stuff/goodrest
execute as @a[scores={rest=24000..47999},tag=!sf_tired,tag=!inmune] run function sidetopia:features/sleep_stuff/tiredness/tired
execute as @a[scores={rest=48000..71999},tag=!sf_tired2,tag=!inmune] run function sidetopia:features/sleep_stuff/tiredness/tired2
execute as @a[scores={rest=72000..},tag=!sf_tired3,tag=!inmune] run function sidetopia:features/sleep_stuff/tiredness/tired3
execute as @a[scores={death=1..}] run function sidetopia:death
execute as @a[scores={death_timer=1}] run function sidetopia:features/sleep_stuff/inminentdeath
execute as @a[scores={bleed_timer=0}] run tag @s remove quartz_bleed

# Undead mobs will not drop items when killed
execute as @e[type=#minecraft:undead, tag=!NoDrops] run data merge entity @s {HandDropChances:[0.0f,0.0f],ArmorDropChances:[0.0f,0.0f,0.0f,0.0f],Tags:["NoDrops"]}

# Animal system
execute as @e[type=#sidetopia:old_age_able, tag=!old_aged] run function sidetopia:features/animal_stuff/old_age_innit
execute as @e[scores={old_age=30..}] run kill @s
execute as @e[type=#sidetopia:can_hunger, tag=!owned_by_someone] if data entity @s Owner run function sidetopia:features/animal_stuff/animal_hunger_innit
