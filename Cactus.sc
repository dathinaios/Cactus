
Cactus { var <projectPath;
         var <buffers, <projectName, <templateManager;
         var <buffersPath, <modulesPath, <initPath, <configPath, <cleanupPath;
         var <at, bufferInfoString = "";
         classvar <at;

  *new { arg projectPath;
    ^super.newCopyArgs(projectPath).init;
  }

  // public

  runModule { arg name, args; var path;
    path = modulesPath +/+ name ++ "/run.scd";
    path.load.valueWithEnvir(args);
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
    this.printNewLine;
    this.runCleanUp;
    this.loadBuffers;
    this.displayLoadInfo;
    this.runUserInit;
    this.runModuleInits;
  }

  listModulesGUI {
    this.listGUI(modulesPath);
  }

  listTemplatesGUI {
    this.listGUI(templateManager.templatesDir);
  }

  buf { arg name;
    ^this.buffers.at(name);
  }

  printBufferInfo {
   bufferInfoString.postln; 
  }

  // private

  *initClass {
    at = Dictionary.new;
  }

  init {
    buffers = Dictionary.new;
    at = Dictionary.new;
    projectPath = projectPath.standardizePath;
    this.initProjectPath;
    this.initTemplateManager;
  }

  storeMainPathVariables {
    buffersPath = projectPath ++ "/buffers";
    modulesPath = projectPath ++ "/modules";
    initPath = projectPath ++ "/init";
    configPath = projectPath   ++ "/config.scd";
    cleanupPath = projectPath   ++ "/cleanup.scd";
  }

  initWithPath {
    this.storeMainPathVariables;
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
    Cactus.at[projectName] = this;
    Cactus.at[\instance] = this;
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
          stripResult: true,
          acceptMode: 1
        );
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

  runCleanUp {
    "Running \'cleanup.scd\'".postln;
    cleanupPath.load.value;
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
    // this.checkAndCreateDir(modulesPath, "Modules");
    this.checkAndCreateDir(initPath, "Initial");
    this.checkAndCreateFile(configPath, "Config");
    this.checkAndCreateFile(cleanupPath, "CleanUp");
    // this.printNewLine;
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
    buffersPath.postln;
    bufferArray = this.collectIntoBuffers(buffersPath);
    bufferArray = this.gatherBuffersFromModules(bufferArray);
    bufferArray.do{arg soundFile; var folderName, soundFileName;
      folderName = this.getFolderNameFromString(soundFile.path);
      soundFileName = this.getFileNameWithoutExtension(soundFile.path);
      this.storeBuffersAsCollection(folderName, soundFile);
      this.storeBufferByName(soundFile, folderName, soundFileName);
    };
  }

  collectIntoBuffers { arg path; var list;
    list = Array.new;
    path.postln;
    PathName(path).folders.do{ arg folder;
      PathName(folder.fullPath).files.do{arg i;
        list = list.add(
          Buffer.read(Server.default, i.fullPath))
      };
    }
    ^list
  }

  storeBuffersAsCollection { arg folderName, soundFile;
    if(buffers.at(folderName).isNil, {
      buffers.put(folderName, List.new);
      // this method for listing buffers in the next line is temporary
      bufferInfoString = bufferInfoString ++ ("Buffer group " ++ folderName  ++ " contains:\n");
    });
    buffers.at(folderName).add(soundFile);
  }

  storeBufferByName{ arg soundFile, folderName, soundFileName;
    buffers.put(folderName ++ "/" ++ soundFileName, soundFile);
    // this method for listing buffers in the next line is temporary
    bufferInfoString = bufferInfoString ++ ("  ->  " ++ soundFileName ++ "\n");
  }

  gatherBuffersFromModules { arg bufferArray;
    PathName(modulesPath).folders.do{
      arg folder; var newBufs;
      newBufs = this.collectIntoBuffers(folder.fullPath ++ "/buffers");
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