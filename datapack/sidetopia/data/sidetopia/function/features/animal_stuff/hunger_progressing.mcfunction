execute as @e[scores={pet_hunger=1..}] run scoreboard players remove @s pet_hunger 1
execute as @e[scores={pet_hunger=0}] run effect give @s minecraft:poison 2 1 true
execute at @e[scores={pet_hunger=0}] run particle angry_villager ~ ~0.3 ~ 0.3 0.3 0.3 0.4 5
schedule function sidetopia:features/animal_stuff/hunger_progressing 600s