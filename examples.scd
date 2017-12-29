
//access or create the project
a = Cactus("~/Develop/SuperCollider/Cactus/_develop/test");

a.openProjectDir;
a.loadBuffers; //this would clear the current buffers first
a.buffers.at("test-1").play
a.buffers.at("lala-adsf").play

File.mkdir(a.projectPath ++ "/buffers").postln;

/* creates
  /config.scd
  /assets
  /helpers
  /bin
  /init
*/