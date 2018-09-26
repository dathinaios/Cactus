
{
  arg a, b;
  "Use this structure to pass arguments to your modules".postln;
  a.debug("This is arg a");
  b.debug("This is arg b");

  // You can access buffers included with the module
  // \instance keeps the last activated projects (if you start another cactus project
  // it will take its place)

  Cactus.at[\instance].buffers.at("some_buffers/a11wlk01").play;
}