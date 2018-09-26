
Cactus { var <projectPath;
         var <buffers, <projectName, <templateManager;
         classvar <at;

  *new { arg projectPath;
    ^super.newCopyArgs(projectPath).init;
  }

  *initClass {
    at = Dictionary.new;
  }

  init {
    buffers = Dictionary.new;
    this.initProjectPath;
    this.initTemplateManager;
  }

  initWithPath {
    this.initProjectName;
    this.displayWelcome;
    this.createDirs;
    this.loadBuffers;
    this.displayLoadInfo;
    this.runConfig;
    this.runUserInit;
    this.runModuleInits;
  }

  initProjectName {
    projectName = projectPath.basename.asSymbol;
    at[projectName] = this;
  }

  initProjectPath {
    if(projectPath.isNil,
      {
        FileDialog(
          okFunc: { arg path;
            projectPath = path;
            this.initWithPath;
            this.openProjectDir;
          },
          fileMode: 2,
          stripResult: true);
      },
      { projectPath = projectPath.standardizePath; this.initWithPath }
    );
  }

  initTemplateManager {
    templateManager = CactusTemplateManager(projectPath);
  }

  displayWelcome {
    "\n  Welcome to Cactus".postln;
      "  ----------------- \n".postln;
  }

  displayLoadInfo {
    ("\n  " ++ projectPath.basename ++ " has been initialised. \n").postln;
  }

  runConfig { var path;
    "Running \'config.scd\'".postln;
    path = projectPath ++ "/config.scd";
    path.load.value;
  }

  runUserInit { var path;
    path = projectPath ++ "/init/";
    path = PathName(path);
    path.files.do{ arg i;
      i.fullPath.load.value;
    };
  }

  runModuleInits { var path;
    path = projectPath ++ "/modules/";
    path = PathName(path);
    path.folders.do({ arg folder; var initPath;
      initPath = PathName(folder.fullPath++"/init");
      initPath.files.do{ arg i;
        i.fullPath.load.value;
      };
    });
  }

  runModule { arg name, args; var path;
    path = projectPath ++ "/modules/";
    path = path ++ name ++ "/run.scd";
    path.load.value(args);
  }

  listModules { var path;
    path = projectPath ++ "/modules/";
    path = PathName(path);
    "\n".postln;
    path.folders.do{ arg i; i.folderName.postln};
    "\n".postln;
  }

  runTemplate { arg templateName, options = ();
    options.projectName = "\\" ++ projectName;
    options.projectPath = "\"" ++ projectPath ++ "\"";
    options.targetDir = projectPath;
    this.templateManager.runTemplate(
      templateName, options
    );
  }

  createDirs { var buffersPath, modulePath, initPath, configPath;

    buffersPath = projectPath ++ "/buffers";
    modulePath = projectPath ++ "/modules";
    initPath = projectPath ++ "/init";
    configPath = projectPath   ++ "/config.scd";

    File.mkdir(projectPath);

    if ( File.exists(buffersPath ).not, {
      File.mkdir(buffersPath);
      ("created: " ++ buffersPath).postln;
    },{".     Project Dir - Done".postln});

    if ( File.exists(modulePath ).not, {
      File.mkdir(modulePath);
      ("created: " ++ modulePath).postln;
    },{"..    Modules Dir - Done".postln});

    if ( File.exists(initPath ).not, {
      File.mkdir(initPath);
      ("created: " ++ initPath).postln;
    },{"...   Init Dir - Done".postln});

    if ( File.exists(configPath).not, {
      File.new(configPath, "w").write("");
      ("created: " ++ configPath ++ "\n").postln;
    },{"....  Configuration File - Done".postln});

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
      if(buffers.at(folder).isNil, {
        buffers.put(folder, List.new);
        ("Buffer group " ++ folder  ++ " contains:").postln;
      });
      buffers.at(folder).add(i);
      buffers.put(folder ++ "/" ++ soundFile, i);
      ("  ->  " ++ soundFile).postln;
    };
  }

  clearBuffers {
    buffers.do{arg i; i.free;};
    buffers.clear;
  }

  restart {
    this.loadBuffers;
    this.displayLoadInfo;
    this.runUserInit;
  }

  listModulesGUI { var path; var gui, infoGUI, infoWin;
    path = projectPath ++ "/modules/";
    path = PathName(path);

    gui = EZListView.new(nil,200@200, "Modules");
    gui.font = Font("Monaco", 11);
    infoWin = Window(
      "Info",
      Rect(
        gui.window.bounds.left+gui.window.bounds.width,
        gui.window.bounds.top-200, 400, 400),
      scroll: true
    ).front;
    infoGUI = StaticText(infoWin, Rect(10, 10, 380, 380));
    infoGUI.font = Font("Monaco", 11);

    path.folders.do{
      arg module; var name, info;
      name = module.folderName;
      File.use(
        path.fullPath ++ "/" ++ module.folderName ++ "/" ++ "readme.txt", "r",
        {
          arg f; info = f.readAllString;
          gui.addItem(name, { infoGUI.string = info });
        }
      );
    };

    gui.valueAction = 0;

  }


}