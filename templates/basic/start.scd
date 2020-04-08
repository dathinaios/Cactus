// Evaluate this to start your project

Cactus("{{projectPath}}");

// You have access to the project using the .at method.
// Here are a few useful things you can do:

Cactus.at[\{{projectName}}].openProjectDir;
Cactus.at[\{{projectName}}].restart;

/*

Any folder with sound files you place in the buffers folder becomes available
with the following syntax:

  Cactus.at[\{{projectName}}].buffers.at("[folder_name]/[file_name_without_extension]").play;

You can also access the folder as a List

  Cactus.at[\{{projectName}}].buffers.at("[folder_name]");

*/

// You can use the dictionary at .at to store anything you like. For example:

Cactus.at[\exampleVariable] = 10000.rand;

// Get the value

Cactus.at[\exampleVariable];
