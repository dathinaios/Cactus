
Modules { var <modulesPath;
          var <envir;
          classvar <globalPath;

  *new { arg modulesPath;
    modulesPath = modulesPath.standardizePath;
    ^super.newCopyArgs(modulesPath).init;
  }

  // Public

  run { arg name, arguments, options = (); var path;
    if(options.global == true,
      { path = globalPath },
      { path = modulesPath });
    ^Module(path, name, arguments);
  }

  getInfo { arg name, key, path = globalPath; var yamlDictionary;
    path = path +/+ name +/+ "info.yaml";
    yamlDictionary = path.standardizePath.parseYAMLFile;
    ^yamlDictionary.at(key.asString);
  }

  runSetups { var path;
    "-> Running setup files for all modules".postln;
    this.runFilesForFolder(modulesPath, "setup");
  }

  runTearDowns { var path;
    "-> Running teardown files for all modules".postln;
    this.runFilesForFolder(modulesPath, "teardown");
  }

  restart {
    this.runTearDowns;
    this.runSetups;
  }

  clear {
    this.runTearDowns;
  }

  browseGlobal {
    if (File.exists(globalPath), {
      this.browseFromPath(globalPath);
    },{
      Modules.installGlobal;
      { "Downloading Global Modules".postln;
        1.wait; ".".postln;
        1.wait; "..".postln;
        1.wait; "...".postln;
        1.wait; "-> Done.".postln;
        this.browseFromPath(globalPath);
      }.fork(AppClock);
    });
  }

  browseLocal { // assumes that we are pointing at a folder with valid modules
    this.browseFromPath(modulesPath);
  }

  // Private

  *initClass {
    globalPath = Platform.userAppSupportDir ++ "/CactusModules";
  }

  init {
    envir = ();
    this.runSetups;
  }

  runFile { arg folder, file;
    ^(folder.fullPath+/+file.asString++".scd").load.value(this);
  }

  runFilesForFolder{ arg path, file;
    path = PathName(path);
    path.folders.do({ arg folder;
      this.runFile(folder, file);
    });
  }

  // Manage CactusModules

  *installGlobal {
    ("git clone https://github.com/dathinaios/CactusModules.git"
      + globalPath.escapeChar($ )).unixCmd;
  }

  *updateGlobal {
    ("git -C" + globalPath.escapeChar($ ) + "pull").unixCmd;
  }

  browseFromPath { arg path;
    var window, listView, textView, textViewTitle, textViewDescription,
        textViewExample, textViewCredits, textViewTags;
    var windowRect, updateButton, previewButton, installButton, hackButton;
    var winWidth, winHeight, rawPath = path;

    winWidth = 815;
    winHeight = 653;
    path = PathName(path);

    windowRect = Rect(
      GUI.window.screenBounds.width-winWidth*0.5,
      GUI.window.screenBounds.height-winHeight*0.5,
      winWidth, winHeight);
    window = Window.new( "Module Browser", windowRect, resizable: false).front;
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));

    listView = EZListView.new(window,200@600);
    listView.font = Font("Monaco", 14);

    textView = View(window, 600@600);
    textView.decorator = FlowLayout(textView.bounds, margin: 0@0, gap: 5@5);

    textViewTitle = TextView(textView, 600@45).background_(Color.white);
    textViewTitle.editable = false;
    textViewTitle.hasVerticalScroller = false;

    textViewDescription = TextView(textView, 600@345).background_(Color.white);
    textViewDescription.editable = false;

    textViewExample = TextView(textView, 600@140).background_(Color.white);
    textViewExample.editable = false;

    textViewCredits = TextView(textView, 600@25).background_(Color.white);
    textViewCredits.editable = false;

    textViewTags = TextView(textView, 600@25).background_(Color.white);
    textViewTags.editable = false;

    updateButton = Button(window, Rect(width: 128, height: 40) );
    updateButton.states = [["Global Update", Color.white, Color.grey]];
    updateButton.canFocus = false;

    StaticText(window, Rect(width: 277 , height: 40));

    previewButton = Button(window, Rect(width: 128, height: 40) );
    previewButton.states = [["Preview", Color.white, Color.grey]];
    previewButton.canFocus = false;

    installButton = Button(window, Rect(width: 128, height: 40) );
    installButton.states = [["install", Color.white, Color.grey]];
    installButton.canFocus = false;

    hackButton = Button(window, Rect(width: 128, height: 40) );
    hackButton.states = [["⚡️ Hack ⚡️", Color.white, Color.grey]];
    hackButton.canFocus = false;

    path.folders.do{ arg item; var name;
      name = item.folderName;
      listView.addItem(
        this.getInfo(name, \name, path: path.fullPath),
        {
          var title, body, example, credits, tags;
          title = "🍃 " + this.getInfo(name, \name, path: path.fullPath);
          body = this.getInfo(name, \description, path: path.fullPath).stripWhiteSpace;
          example = this.getInfo(name, \example, path: path.fullPath).stripWhiteSpace;
          credits = "Created by: " + this.getInfo(name, \author, path: path.fullPath).stripWhiteSpace;
          tags = "Tags: " + this.getInfo(name, \tags, path: path.fullPath);

          textViewTitle.string = title;
          textViewDescription.string = body;
          textViewExample.string = example;
          textViewCredits.string = credits;
          textViewTags.string = tags;

          textViewTitle.font = Font("Palatino", 28);
          textViewTitle.stringColor = Color(0.42, 0.57, 0.7640);
          textViewDescription.font = Font("Palatino", 16, italic: true);
          textViewDescription.stringColor = Color.black;
          textViewExample.font = Font("Menlo", 12);
          textViewExample.stringColor = Color.black;
          textViewExample.syntaxColorize;
          textViewCredits.font = Font("Palatino", 14);
          textViewCredits.stringColor = Color.black;
          textViewTags.font = Font("Palatino", 14);
          textViewTags.stringColor = Color.black;

          updateButton.action = {Modules.updateGlobal};
          previewButton.action = {this.previewModule(name, path.fullPath)};
          installButton.action = {this.installModule(name, modulesPath)};
          hackButton.action = {this.hackModule(name, source: rawPath); window.close};
        }
      );
    };

    if(listView.items.size > 0, { listView.valueAction = 0 });
  }

  installModule{ arg name, target, newName, source = globalPath;
    this.checkAndCreateSilently(modulesPath);
    if (File.exists(globalPath +/+ name.asString) or:{ newName.notNil }, {
      var sourcePath, targetPath;
      sourcePath = source +/+ name;
      targetPath = target +/+ name.asString;
      if(newName.notNil){ targetPath = target +/+ newName.asString; };
      if (File.exists(targetPath).not, {
        ("cp -R" + sourcePath.escapeChar($ ) + targetPath.escapeChar($ )).unixCmd;
        ("✅ Module" + targetPath.basename + "created succesfully.").postln;
        if(newName.notNil){
          this.replaceTitleWithNewName(name, newName, sourcePath, targetPath);
        };
      }, {
        "You are already using a module with that name!".error;
      });
    },{
      "There is no module with that name!".error;
    });
  }

  hackModule { arg name, source = globalPath, target = modulesPath;
    var dialog;
    dialog = Window.new("", Rect(
        GUI.window.screenBounds.width*0.5,
        GUI.window.screenBounds.height*0.5, 230, 120),
        border: true, resizable: false).front;

    StaticText.new(dialog,Rect(5, 10, 220, 50))
    .string_("Name your new module:")
    .font_(Font("Palatino", 16))
    .align_(\left);

    TextField(dialog, Rect(5, 50, 220, 50))
    .font_(Font("Palatino", 30))
    .action_{ arg textField;
      dialog.close;
      this.installModule(name, target, newName: textField.string.asSymbol, source: source);
    };
  }

  previewModule { arg name, path;
    this.runFile(PathName(path +/+ name), "setup");
    this.getInfo(name, \preview, path: path).interpret.value(this);
    { 5.wait;
      this.runFile(PathName(path +/+ name), "teardown");
    }.fork;
  }

  replaceTitleWithNewName {
    arg name, newName, sourcePath, targetPath;
    var sourceInfo, originalTitle, yamlDictionary;
    var stringResult;

    sourceInfo = sourcePath +/+ "info.yaml";
    yamlDictionary = sourceInfo.standardizePath.parseYAMLFile;
    originalTitle = yamlDictionary.at(\name.asString);
    stringResult = File.use(sourceInfo, "r", {
      arg file; var string;
      string = file.readAllString;
      string = string.replace(
        "name:" + originalTitle.asString,
        "name:" + newName.asString);
    });

    { 1.wait;
      this.createTheFile(targetPath +/+ "info.yaml", stringResult);
    }.fork;
  }

  createTheFile { arg targetFilePath, generatedFileContent;
    File.new(targetFilePath.standardizePath, "w").write(generatedFileContent).close;
  }

  checkAndCreateSilently { arg path;
    if ( File.exists(path).not, {
      File.mkdir(path)});
  }
}
