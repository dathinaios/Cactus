# Cactus <a href="http://fasmatwist.com/opensource"><img src="https://user-images.githubusercontent.com/481589/216767388-d94cdd88-dc8f-4f95-9d87-1275583fb73b.jpg" alt="CuePlayer" width="130px" height="38px" align="right"></a>

<p align="center">
<a href="https://user-images.githubusercontent.com/481589/218518920-f86b2b44-a200-41bf-9640-50abc013d19e.png"><img src="https://user-images.githubusercontent.com/481589/218518920-f86b2b44-a200-41bf-9640-50abc013d19e.png" alt="cactus" width="45%" height="45%"></a>
</p>

A style agnostic framework for creative coding using the SuperCollider audio programming language. At its most basic level it will:

* Create a project file structure with configuration, initialisation and cleanup files.
* Create a `buffers` folder from which it collects all sound files and make them available through an intuitive interface.
* Allow for defining quickstart templates for different types of projects using a (very) basic templating engine.
* Allow for the creation and access of modules of sound processes (WIP).

## Installation

Install it as a quark from within SuperCollider, via:

    Quarks.install("Cactus");

or download it from [GitHub](https://github.com/dathinaios/Cactus/releases/latest), unzip & and place the folder in:

    ~/Library/Application Support/SuperCollider/Extensions/

## Quickstart

Run:

```supercollider
CactusGUI.new;
```

This will create a project file structure with configuration, initialisation and cleanup files. It will also create a buffers folder from which it collects all sound files and make them available through an intuitive interface.

The `config.scd` is run only once when you first initialize the `Cactus` project. The `.scd` files found in the `init` folder will run after `config.scd` on initialisation and also every time you call `.restart`. On restart `cleanup.scd` will be called before reinitialisation.

Any `wav` or `aif` file placed in the `buffers` folder will be automagically available as a buffer:

```supercollider
c = Cactus("/path/to/the/project");
// for a test.wav file found at the root of the buffers folder
c.buf("/test");
// for an asdf.wav file found in the folder moreSounds
c.buf("/moreSounds/asdf");
// get a List of all files in the moreSounds folder
c.buf("/moreSounds")
```

For more see the Cactus help files.

---
###### <i>Copyright © 2020, Dionysis Athinaios</br>This program is free software; you can redistribute it and/or modify it under the terms of the [GNU General Public License](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html).</i>
