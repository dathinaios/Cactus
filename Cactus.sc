
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
    this.runCleanUp;
    this.loadBuffers;
    this.displayLoadInfo;
    this.runUserInit;
  }

  buf { arg name;
    ^this.buffers.at(name);
  }

  bufnums { arg name; var bufs;
    ^this.buf(name).getCactusBufNums;
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
    this.initWithPath;
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

  clear {
    "-> Clearing Buffers from Memory".postln;
    this.clearBuffers;
    this.runCleanUp;
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
      folderName = this.getPathAfterBuffersFolder(soundFile.path);
      soundFileName = this.getFileNameWithoutExtension(soundFile.path);
      this.storeBuffersAsCollection(folderName, soundFile);
      this.storeBufferByName(soundFile, folderName, soundFileName);
    };
    this.fixBuffersRootDirEntry;
  }

  collectIntoBuffers { arg path; var list;
    list = Array.new;
    PathName(path).filesDo{ arg i;
      list = list.add(
      Buffer.read(Server.default, i.fullPath))
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

  getPathAfterBuffersFolder {
    arg path; var result, done = false;
    path = path.dirname.split;
    result = "";
    path.reverseDo{ arg item;
      if(done.not and:{item != "buffers"},
        {result = item +/+ result},
        {done = true}
      )};
     result = "" +/+ result;
    ^result.withoutTrailingSlash;
  }

  fixBuffersRootDirEntry {
    buffers.put("/", buffers.at(""));
    buffers.removeAt("");
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
    "-> Running \'config.scd\'".postln;
  }

  postCleanupInfo {
    "-> Running \'cleanup.scd\'".postln;
  }

  displayWelcome {
      "Welcome to Cactus".postln;
      "~~~~~~~~~~~~~~~~~".postln;
  }

  displayLoadInfo {
    "\n".postln;
    ("-> \'" ++ projectPath.basename ++ "\'" ++ " has been initialised").postln;
  }

  getFolderNameFromString { arg path;
    ^path.dirname.split.last;
  }

  getFileNameWithoutExtension { arg path;
    ^path.basename.splitext[0];
  }

  listBuffers { arg list;
    list = List.new;
    buffers.keysDo{ arg key;
      if(key != "/"){list = list.add(key)};
    };
    list.sort.do{ arg key;
      if(key.findRegexp("/*/").size == 1){"-----------------".postln;};
      key.postln;
    };
  }

}

+ List {
  getCactusBufNums{
    ^this.collect{arg i; i.bufnum}
  }
}

+ Buffer {
  getCactusBufNums{
    ^this.bufnum
  }
}
