tag @s add sf_tired
tag @s remove sf_have_sleeped
tag @s remove sf_tired2
tag @s remove sf_tired3
execute as @s[tag=!celestite_clarity] run function sidetopia:features/sleep_stuff/tiredness/tired_normal
execute as @s[tag=celestite_clarity] run function sidetopia:features/sleep_stuff/tiredness/tired_celestite
effect give @s minecraft:darkness 30 1 true