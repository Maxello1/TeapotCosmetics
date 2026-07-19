execute as @a[tag=hs_need_drying,scores={hs_drying=0}] run attribute @s minecraft:generic.movement_speed modifier remove harderswim:speed_debuff
execute as @a[tag=hs_need_drying,scores={hs_drying=0}] run attribute @s minecraft:player.block_break_speed modifier remove harderswim:block_break_debuff
execute as @a[tag=hs_need_drying,scores={hs_drying=0}] run attribute @s minecraft:generic.attack_damage modifier remove harderswim:damage_debuff
execute as @a[tag=hs_need_drying,scores={hs_drying=0}] run attribute @s minecraft:generic.attack_speed modifier remove harderswim:attack_speed_debuff
tag @a[tag=hs_need_drying] remove hs_need_drying