
Cactus { var <projectPath;
         var <buffers, <projectName, <templateManager, <modules;
         var <buffersPath, <initPath, <configPath, <cleanupPath,
             <classesPath;
         var <at;
         classvar <at, <cachePath;

  *new { arg projectPath;
    ^super.newCopyArgs(projectPath).init;
  }

  *clearCache { var path;
    path = cachePath.asString.standardizePath;
    if (PathName(path).folders.size >= 1){
      ("rm -r" + path.escapeChar($ ) +/+ "/*").unixCmd;
    };
    "Clearing Cactus cache...".postln;
  }

  // Public

  openProjectDir {
    projectPath.openOS;
  }

  restart {
    this.runModulesCleanups;
    this.runCleanup;
    this.loadBuffers;
    this.displayLoadInfo;
    this.runUserInit;
    this.runModulesInits;
  }

  clear {
    this.runCleanup;
    this.clearBuffers;
    this.runModulesCleanups;
  }

  buf { arg name;
    ^this.buffers.at(name.asString);
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

  runModule { arg name, args, options;
    ^modules.run(name, args, options);
  }

  browseModules {
    modules.browseLocal;
  }

  clearBuffers {
    // if(buffers.size > 0){
    //   "-> Clearing Buffers from Memory".postln};
    buffers.do{arg i; i.free;};
    buffers.clear;
  }

  // Private

  *initClass {
    at = Dictionary.new;
    cachePath = Platform.userAppSupportDir +/+ "Extensions/_cactusCache";
  }

  returnCoreOptions { var options;
    options = ();
    options.projectName = projectName;
    options.projectPath = projectPath;
    options.targetDir = projectPath;
    ^options
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
    configPath = projectPath ++ "/config.scd";
    cleanupPath = projectPath ++ "/cleanup.scd";
    classesPath = projectPath ++ "/classes";
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
    this.initModules;
    this.initClasses;
  }

  initClasses { var projectClassesPath;
    projectClassesPath = cachePath +/+ projectName;
    if (File.exists(classesPath), {
      this.linkClassesFolder(projectClassesPath);
    });
  }

  initModules {
    modules = Modules(projectPath +/+ "modules");
  }

  linkClassesFolder { arg path;
    if (File.exists(path).not, {
      Cactus.clearCache;
      ("ln -s" + classesPath.escapeChar($ )+ path.escapeChar($ )).unixCmd;
      "🌵 You have custom classes in your Cactus project. They have been linked.".postln;
      "🌵 These classes will remain linked until you open a different Cactus project or run Cactus.clearCache".postln;
      "🌵 (You will need to recompile before the classes become available)".postln;
    });
  }

  initProjectName {
    projectName = projectPath.basename.asSymbol;
    Cactus.at[projectName] = this;
    Cactus.at[\instance] = this;
  }

  initTemplateManager {
    templateManager = CactusTemplateManager(this);
  }

  runConfig {
    this.postConfigInfo;
    configPath.load.value;
  }

  runCleanup {
    this.postCleanupInfo;
    cleanupPath.load.value;
  }

  runUserInit { var path;
    path = PathName(initPath);
    path.files.do{ arg i;
      i.fullPath.load.value;
    };
  }

  runModulesInits {
    modules.runInits;
  }

  runModulesCleanups {
    modules.clear;
  }

  createDirs {
    this.checkAndCreateDir(projectPath, "Project");
    this.checkAndCreateDir(buffersPath, "Buffers");
    this.checkAndCreateDir(initPath, "Init");
    this.checkAndCreateFile(configPath, "Config");
    this.checkAndCreateFile(cleanupPath, "CleanUp");
    this.checkAndCreateSilently(cachePath);
  }

  loadBuffers { var bufferArray;
    this.clearBuffers;
    // "-> Loading Buffers to Memory".postln;
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

  checkAndCreateSilently { arg path;
    if ( File.exists(path).not, {
      File.mkdir(path)});
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
    ("-> 🌵 Project \'" ++ projectPath.basename ++ "\'" ++ " has been initialised").postln;
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
