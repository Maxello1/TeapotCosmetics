scoreboard objectives add food food
scoreboard objectives add rest minecraft.custom:minecraft.time_since_rest
scoreboard objectives add death minecraft.custom:minecraft.deaths
scoreboard objectives add death_timer dummy
scoreboard objectives add rng dummy
scoreboard objectives add bleed_timer dummy
scoreboard objectives add pitch dummy
scoreboard objectives add heat_amount dummy
scoreboard objectives add player_health health
scoreboard objectives add adrenaline_timer dummy
scoreboard objectives add old_age dummy
scoreboard objectives add hs_drying dummy
scoreboard objectives add pet_hunger dummy

# This is for sleep features
schedule function sidetopia:features/sleep_stuff/regen 1t
schedule function sidetopia:features/sleep_stuff/blackout/randomblackout 7s
gamerule naturalRegeneration false

# This is for random features
schedule function sidetopia:features/swimming/swim_check 1t

schedule function sidetopia:features/gem/gem_stuff/topaz/clear_effects 1t

schedule function sidetopia:seconds_timer 1t

schedule function sidetopia:features/animal_stuff/age_progressing 1200s

schedule function sidetopia:features/animal_stuff/hunger_progressing 600s