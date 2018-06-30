
Cactus { var <projectPath;
  var <buffers;

  *new { arg projectPath;
    ^super.newCopyArgs(projectPath).init;
  }

  init {
    projectPath = projectPath.standardizePath;
    buffers = Dictionary.new;
    this.displayWelcome;
    this.createDirs;
    this.runUserInit;
    this.loadBuffers;
    this.displayLoadInfo;
  }

  displayWelcome {
    "\n  Welcome to Cactus".postln;
      "  ----------------- \n".postln;
  }

  displayLoadInfo {
    ("\n  " ++ projectPath.basename ++ " has been initialised. \n").postln;
  }

  runUserInit { var path;
    path = projectPath ++ "/init/";
    path = PathName(path);
    path.files.do{ arg i;
      i.fullPath.load.value;
    };
  }

  runPlugin { arg name, args; var path;
    path = projectPath ++ "/plugins/";
    path = path ++ name ++ ".scd";
    path.load.value(args);
  }

  listPlugins { var path;
    path = projectPath ++ "/plugins/";
    path = PathName(path);
    "\n".postln;
    path.files.do{ arg i; i.fileNameWithoutExtension.postln};
    "\n".postln;
  }

  createDirs { var buffersPath, helpersPath, pluginPath, initPath, configPath;

    buffersPath = projectPath ++ "/buffers";
    helpersPath = projectPath ++ "/helpers";
    pluginPath = projectPath ++ "/plugins";
    initPath = projectPath ++ "/init";
    configPath = projectPath   ++ "/config.scd";

    File.mkdir(projectPath);
    if ( File.exists(buffersPath ).not, {
      File.mkdir(buffersPath);
      ("created: " ++ buffersPath).postln;
    },{".     Project Dir - Done".postln});
    if ( File.exists(helpersPath ).not, {
      File.mkdir(helpersPath);
      ("created: " ++ helpersPath).postln;
    },{"..    Helpers Dir - Done".postln});
    if ( File.exists(pluginPath ).not, {
      File.mkdir(pluginPath);
      ("created: " ++ pluginPath).postln;
    },{"...   Plugin Dir - Done".postln});
    if ( File.exists(initPath ).not, {
      File.mkdir(initPath);
      ("created: " ++ initPath).postln;
    },{"....  Init Dir - Done".postln});
    if ( File.exists(configPath).not, {
      File.new(configPath, "w").write("");
      ("created: " ++ configPath).postln;
    },{"..... Configuration File - Done".postln});
  }

  openProjectDir {
    projectPath.openOS;
  }

  loadBuffers { var bufferArray;
    this.clearBuffers;
    bufferArray = SoundFile.collectIntoBuffers(projectPath ++ "/buffers/*/*");
    bufferArray.do{arg i; var folder, soundFile;
      folder = i.path.dirname.split.last;
      soundFile = i.path.basename.splitext[0];
      ("Buffer created: " ++ soundFile).postln;
      buffers.put(folder ++ "/" ++ soundFile, i);
    };
    // ^buffers;
  }

  clearBuffers {
    buffers.do{arg i; i.free;};
  }

}