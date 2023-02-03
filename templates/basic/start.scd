// Evaluate this to start your project
// The Cactus instnce is stored in c by convention

c = CactusGUI(Document.current.dir).cactus; // with GUI
c = Cactus(Document.current.dir); // without GUI

/*

Any folder with sound files you place in the buffers folder becomes available
with the following syntax:

  c.buffers.at("[folder_name]/[file_name_without_extension]").play;

You can also access the folder as a List

  c.buffers.at("[folder_name]");

*/

// You can use the dictionary at .at to store anything you like. For example:

c.at[\exampleVariable] = 10000.rand;

// Get the value

c.at[\exampleVariable];
