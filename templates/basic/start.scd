// Evaluate this to start your project

Cactus({{projectPath}});

// You have access to the project using the .at method.
// Here are a few useful things you can do:

Cactus.at[{{projectName}}].openProjectDir;
Cactus.at[{{projectName}}].restart;
Cactus.at[{{projectName}}].listModulesGUI;

// an example module is included
Cactus.at[{{projectName}}].runModule(\example_module).value("testing 1", "and 2");

// Any folder with sound files you place in the buffers folder becomes available
// with the following syntax:
// Cactus.at[projectName].buffers.at("[folder_name]/[file_name_without_extension]").play;
// You can also access the folder as a List
//Cactus.at[projectName].buffers.at("[folder_name]);

// Any soundfiles included in a module can be accessed in the same manner
// Here is the one inlcuded in the example module

Cactus.at[{{projectName}}].buffers('some_buffers/a11wlk01').play;

// You can use the dictionary at .at to store anything you like. For example:
Cactus.at[\exampleVariable] = 10000.rand;

// Get the value
Cactus.at[\exampleVariable];