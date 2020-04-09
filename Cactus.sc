
Cactus { var <projectPath;
         var <buffers, <projectName, <templateManager;
         var <buffersPath, <initPath, <configPath, <cleanupPath;
         var <at;
         classvar <at;

  *new { arg projectPath;
    ^super.newCopyArgs(projectPath).init;
  }

  // Public

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
  }

  buf { arg name;
    ^this.buffers.at(name);
  }

  runTemplate { arg templateName, options = ();
    options = options ++ this.returnCoreOptions;
    this.templateManager.runTemplate(
      templateName, options);
    SystemClock.sched(1, {this.restart});
  }

  // Private

  returnCoreOptions { var options;
    options = ();
    options.projectName = projectName;
    options.projectPath = projectPath;
    options.targetDir = projectPath;
    ^options
  }

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

  runConfig {
    this.postConfigInfo;
    configPath.load.value;
  }

  runCleanUp {
    this.postCleanupInfo;
    cleanupPath.load.value;
  }

  runUserInit { var path;
    path = PathName(initPath);
    path.files.do{ arg i;
      i.fullPath.load.value;
    };
  }

  createDirs {
    this.checkAndCreateDir(projectPath, "Project");
    this.checkAndCreateDir(buffersPath, "Buffers");
    this.checkAndCreateDir(initPath, "Initial");
    this.checkAndCreateFile(configPath, "Config");
    this.checkAndCreateFile(cleanupPath, "CleanUp");
  }

  loadBuffers { var bufferArray;
    this.clearBuffers;
    bufferArray = this.collectIntoBuffers(buffersPath);
    bufferArray.do{arg soundFile; var folderName, soundFileName;
      folderName = this.getFolderNameFromString(soundFile.path);
      soundFileName = this.getFileNameWithoutExtension(soundFile.path);
      this.storeBuffersAsCollection(folderName, soundFile);
      this.storeBufferByName(soundFile, folderName, soundFileName);
    };
  }

  collectIntoBuffers { arg path; var list;
    list = Array.new;
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
    });
    buffers.at(folderName).add(soundFile);
  }

  storeBufferByName{ arg soundFile, folderName, soundFileName;
    buffers.put(folderName ++ "/" ++ soundFileName, soundFile);
  }

  // Helper Methods

  checkAndCreateDir { arg path, name;
    if ( File.exists(path).not, {
      File.mkdir(path);
      ("created: " ++ path).postln;
    },{( name ++ " Dir - √" ).postln});
  }

  checkAndCreateFile { arg path, name;
    if ( File.exists(path).not, {
      File.new(path, "w").write("");
      ("created: " ++ path ++ "\n").postln;
    },{( name ++ " File - √" ).postln});
  }

  postConfigInfo {
    "Running \'config.scd\'".postln;
  }

  postCleanupInfo {
    "Running \'cleanup.scd\'".postln;
  }

  printNewLine {
    "\n".postln;
  }

  displayWelcome {
      "Welcome to Cactus".postln;
      "~~~~~~~~~~~~~~~~~ \n".postln;
  }

  displayLoadInfo {
    "~~~~~~~~~~~~~~~~~ \n".postln;
    ("\'" ++ projectPath.basename ++ "\'" ++ " has been initialised").postln;
  }

  getFolderNameFromString { arg path;
    ^path.dirname.split.last;
  }

  getFileNameWithoutExtension { arg path;
    ^path.basename.splitext[0];
  }

}
