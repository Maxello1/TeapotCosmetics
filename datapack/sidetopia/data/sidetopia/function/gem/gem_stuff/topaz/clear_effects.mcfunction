execute as @a[scores={adrenaline_timer=0}] run attribute @s[scores={adrenaline_timer=0}] minecraft:generic.attack_speed modifier remove sidetopia:adrenaline
execute as @a[scores={adrenaline_timer=0}] run attribute @s[scores={adrenaline_timer=0}] minecraft:generic.armor modifier remove sidetopia:adrenaline
execute as @a[scores={adrenaline_timer=0}] run attribute @s[scores={adrenaline_timer=0}] minecraft:generic.attack_damage modifier remove sidetopia:adrenaline
execute as @a[scores={adrenaline_timer=0}] run attribute @s[scores={adrenaline_timer=0}] minecraft:generic.step_height modifier remove sidetopia:adrenaline
execute as @a[scores={adrenaline_timer=0}] run attribute @s[scores={adrenaline_timer=0}] minecraft:generic.movement_speed modifier remove sidetopia:adrenaline
execute as @a[scores={adrenaline_timer=0}] run attribute @s[scores={adrenaline_timer=0}] minecraft:generic.movement_speed modifier remove sidetopia:adrenaline_1
schedule function sidetopia:topaz/clear_effects 1s