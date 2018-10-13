
Cactus { var <projectPath;
         var <buffers, <projectName, <templateManager;
         var <buffersPath, <modulesPath, <initPath, <configPath;
         classvar <at;

  *new { arg projectPath;
    ^super.newCopyArgs(projectPath).init;
  }

  // public

  runModule { arg name, args; var path;
    path = modulesPath +/+ name ++ "/run.scd";
    path.load.value(args);
  }

  listModules { var path;
    path = PathName(modulesPath);
    this.printNewLine;
    path.folders.do{ arg i; i.folderName.postln};
    this.printNewLine;
  }

  runTemplate { arg templateName, options = ();
    options.projectName = "\\" ++ projectName;
    options.projectPath = "\"" ++ projectPath ++ "\"";
    options.targetDir = projectPath;
    this.templateManager.runTemplate(
      templateName, options
    );
    Routine({
      1.wait;
      this.restart;
    }).play;
  }

  openProjectDir {
    projectPath.openOS;
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

  listModulesGUI {
    this.listGUI(modulesPath);
  }

  listTemplatesGUI {
    this.listGUI(templateManager.templatesDir);
  }

  // private

  *initClass {
    at = Dictionary.new;
  }

  init {
    buffers = Dictionary.new;
    projectPath = projectPath.standardizePath; 
    this.storeMainPathVariables;
    this.initProjectPath;
    this.initTemplateManager;
  }

  storeMainPathVariables {
    buffersPath = projectPath ++ "/buffers";
    modulesPath = projectPath ++ "/modules";
    initPath = projectPath ++ "/init";
    configPath = projectPath   ++ "/config.scd";
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
    at[\instance] = this;
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
      { this.initWithPath }
    );
  }

  initTemplateManager {
    templateManager = CactusTemplateManager.new;
  }

  displayWelcome {
    "\n  Welcome to Cactus".postln;
      "  ----------------- \n".postln;
  }

  displayLoadInfo {
    ("\n  " ++ projectPath.basename ++ " has been initialised. \n").postln;
  }

  runConfig {
    "Running \'config.scd\'".postln;
    configPath.load.value;
  }

  runUserInit { var path;
    path = PathName(initPath);
    path.files.do{ arg i;
      i.fullPath.load.value;
    };
  }

  runModuleInits { var path;
    path = PathName(modulesPath);
    path.folders.do({ arg folder; var initPath;
      initPath = PathName(folder.fullPath++"/init");
      initPath.files.do{ arg i;
        i.fullPath.load.value;
      };
    });
  }

  createDirs {
    this.checkAndCreateDir(projectPath, "Project");
    this.checkAndCreateDir(buffersPath, "Buffers");
    this.checkAndCreateDir(modulesPath, "Modules");
    this.checkAndCreateDir(initPath, "Init");
    this.checkAndCreateFile(configPath, "Configuration");
    this.printNewLine;
  }

  checkAndCreateDir { arg path, name;
    if ( File.exists(path).not, {
      File.mkdir(path);
      ("created: " ++ path).postln;
    },{( "  " ++ name ++ " Dir - Done" ).postln});
  }

  checkAndCreateFile { arg path, name;
    if ( File.exists(path).not, {
      File.new(path, "w").write("");
      ("created: " ++ path ++ "\n").postln;
    },{( "  " ++ name ++ " File - Done" ).postln});
  }

  loadBuffers { var bufferArray;
    this.clearBuffers;
    bufferArray = SoundFile.collectIntoBuffers(buffersPath ++ "/*/*");
    bufferArray = this.gatherBuffersFromModules(bufferArray);
    bufferArray.do{arg soundFile; var folderName, soundFileName;
      folderName = this.getFolderNameFromString(soundFile.path);
      soundFileName = this.getFileNameWithoutExtension(soundFile.path);
      this.storeBuffersAsCollection(folderName, soundFile);
      this.storeBufferByName(soundFile, folderName, soundFileName);
    };
  }

  storeBuffersAsCollection { arg folderName, soundFile;
      if(buffers.at(folderName).isNil, {
        buffers.put(folderName, List.new);
        ("Buffer group " ++ folderName  ++ " contains:").postln;
      });
      buffers.at(folderName).add(soundFile);
  }

  storeBufferByName{ arg soundFile, folderName, soundFileName;
      buffers.put(folderName ++ "/" ++ soundFileName, soundFile);
      ("  ->  " ++ soundFileName).postln;
  }

  gatherBuffersFromModules { arg bufferArray;
    PathName(modulesPath).folders.do{
      arg folder; var newBufs;
      newBufs = SoundFile.collectIntoBuffers(folder.fullPath ++ "/buffers/*/*");
      bufferArray = bufferArray.addAll(newBufs);
    };
    ^bufferArray;
  }

  listGUI { arg path, action; var gui, infoGUI, infoWin;
    path = PathName(path);

    gui = EZListView.new(nil,200@200, "");
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
      arg item; var name, info;
      name = item.folderName;
      File.use(
        path.fullPath ++ "/" ++ item.folderName ++ "/" ++ "readme.txt", "r",
        {
          arg f; info = f.readAllString;
          gui.addItem(name, { infoGUI.string = info });
        }
      );
    };

    gui.valueAction = 0;
  }

  //helper methods

  printNewLine {
   "\n".postln; 
  }

  getFolderNameFromString { arg path;
    ^path.dirname.split.last;
  }

  getFileNameWithoutExtension { arg path;
    ^path.basename.splitext[0];
  }

}