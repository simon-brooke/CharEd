# CharEd

CharEd is a character editor for the animated 3-D models used with [the jMonkeyEngine (JME) game engine][jme]. It is forked from Stephen Gold's [Maud](https://github.com/stephengold/Maud) character editor, and retains the BSD licence which he used, rather than the GPL I would normally use.

I have renamed the project because my intention for it is different from his. If you want something you can use quickly, you should start from Maud, rather than from here.

My intentions for this are

1. It should support development of the [simulated-genetics](https://github.com/simon-brooke/simulated-genetics) system I'm currently working on;
2. In the longer term, it should be a reasonably reskinable character editor that people can integrate into their own game projects. 

In short: do not use this (at least yet). Maud is a better foundation for anything you are building.

## TODO

A lot of what Maud does I don't want or need to do I need to start with a small number of body 'prototype' models:

* Male adult
* Female adult
* Male pre-teen
* Female pre-teen

with possibly a few variants; and to produce a set of tools for morphing those models in line with the changes which can be made using the simulated genome.

Maud's user interface is both too complex and insufficiently intuitive for what I need. 

I need to be able to

1. Select a model from a small set;
2. Rotate the model (Maud does this well);
3. Zoom into the model (Maud does this well);
4. Pan up and down the model (Maud either does not do this at all, or does it in an unobvious way);
5. Change the limited number of parameters the simulated genome provides, probably using sliders;
6. View the genome value which corresponds to the current mutation of the model;
7. Provide a name for the current mutation of the model;
8. Save the name/genome value pair into a database.
