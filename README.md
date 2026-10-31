# Chaos Die (Fabric 1.20.1)

A d20 item. Right-click to roll and get a random effect: low rolls hurt, high rolls help, a natural 20 is a jackpot.
Sneak while rolling for advantage (best of two rolls). 3 second cooldown.

Recipe: gold ingots in the corners, lapis on the edges, ender pearl in the middle.

## Rolls
1 Smitten (cosmetic lightning, slow, blind, weak) · 2 Wobbly Knees · 3 Empty Stomach · 4 Lights Out ·
5 Hold My Drink (levitation, then you fall) · 6 Chicken Rain · 7 Black Cat (bad luck) · 8 Wet Noodle Arms ·
9 Nothing · 10 Bread · 11 Zoomies · 12 Springs In Your Shoes · 13 Cat Eyes · 14 Second Wind ·
15 Treasure Roll (random loot) · 16 XP Shower · 17 Diver's Blessing · 18 Battle Fury · 19 Fortune's Favor ·
20 NATURAL 20 (totem, diamonds, regen, absorption)

## Build
- On a PC: `gradle wrapper --gradle-version 8.6` once, then `./gradlew build`. Jar: `build/libs/chaosdie-1.0.0.jar`.
- No PC: push this folder to a GitHub repo. The Actions workflow builds it; download the `chaosdie-jar` artifact from the run.

## Install
Put the jar and Fabric API 0.92.x for 1.20.1 in your `mods` folder.
