
//access of create using the file dialog
Cactus.new;

//access or create the project
Cactus("~/Develop/SuperCollider/cactus_tests/test");
Cactus.at[\test].runTemplate(\basic);

Cactus.at[\test].listModules;
Cactus.at[\test].listModulesGUI;
Cactus.at[\test].listTemplatesGUI;

Cactus.at[\test].openProjectDir;
Cactus.at[\test].restart;
Cactus.at[\test].loadBuffers; //this would clear the current buffers first
Cactus.at[\test].buffers.at("test/1").play;
Cactus.at[\test].buffers.at("example/a11wlk01").play;
Cactus.at[\test].buffers.at("lala/adsf").play;
Cactus.at[\test].buffers.do{arg i; i.postln;};

// plugins are just files with code to be executed
Cactus.at[\test].runModule("tgrainsFF1");
// they can accept arguments
Cactus.at[\test].runModule("testArgs", (one: "testing number one", two: "testing number two"));
// interface ideas
Cactus.at[\test].loadTemplate('Proxy Template'); // creates a config.scd and an init file setting up the proxyspace and other useful structures

a.scaffold('iterate module');
a.scaffold('probabilistic scattering');