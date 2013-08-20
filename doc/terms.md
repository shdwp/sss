# General terms:

* Entity - represents every quiet dynamic thing in universe (npc, actor)
* Actor - playable character :actor-path in gamestate must link to it in universe
* Game cycle - function, that called recursively and forms game-loop
* Gui backend - backend, that provide methods to render graphics and get keyboard input
* Universe - part of gamestate, representing all the universe

## Gamestate: 
* Gamestate - contains everything needed to handle game-cycle iteration
* Game dispatcher - object, that handles some aspect of the game. Game dispatchers are stackable in gamestate, and processed from top to end. Formally game dispatcher is hash-map, that provide :update fn [gs -gs], :paint fn [canvas gs] and can store dynamic data in gamestate
* Turner - function [gs], that called after each turn end, and designed to update universe
* Ruler - function [gs -gs], that designed for handling some keys and update gamestate
