
Modules { var <modulesPath;
          classvar <globalPath;

  *new { arg modulesPath;
    modulesPath = modulesPath.standardizePath;
    ^super.newCopyArgs(modulesPath).init;
  }

  // Public

  run { arg name, args; var path;
    if(args.global == true,
      { path = globalPath },
      { path = modulesPath });
    ^Module(path).run(name, args)
  }

  getInfo { arg name, key, path = globalPath; var yamlDictionary;
    path = path +/+ name +/+ "info.yaml";
    yamlDictionary = path.standardizePath.parseYAMLFile;
    ^yamlDictionary.at(key.asString);
  }

  runInits { var path;
    path = PathName(modulesPath);
    path.folders.do({ arg folder;
      this.runInit(folder);
    });
  }

  runCleanUps { var path;
    path = PathName(modulesPath);
    path.folders.do({ arg folder; var initPath;
      this.runCleanUp(folder);
    });
  }

  restart {
    this.runCleanUps;
    this.runInits;
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
    this.runInits;
  }

  runInit{ arg folder; var initPath;
    initPath = PathName(folder.fullPath++"/init");
    initPath.files.do{ arg i;
      i.fullPath.load.value;
    };
  }

  runCleanUp { arg folder;
    (folder.fullPath++"cleanup.scd").load;
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
    var window, listView, textView;
    var windowRect, updateButton, previewButton, installButton, hackButton;
    var winWidth, winHeight, rawPath = path;

    winWidth = 815;
    winHeight = 453;
    path = PathName(path);

    windowRect = Rect(
      GUI.window.screenBounds.width-winWidth*0.5,
      GUI.window.screenBounds.height-winHeight*0.5,
      winWidth, winHeight);
    window = Window.new( "Module Browser", windowRect, resizable: false).front;
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));

    listView = EZListView.new(window,200@400);
    listView.font = Font("Monaco", 14);
    textView = TextView(window, 600@400).background_(Color.white);
    textView.editable = false;

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
          title = "🍃 " + this.getInfo(name, \name, path: path.fullPath) + "\n";
          body = "\n" + this.getInfo(name, \description, path: path.fullPath).stripWhiteSpace + "\n\n";
          example = this.getInfo(name, \example, path: path.fullPath).stripWhiteSpace + "\n\n";
          credits = "Created by: " + this.getInfo(name, \author, path: path.fullPath).stripWhiteSpace + "\n";
          tags = "Tags: " + this.getInfo(name, \tags, path: path.fullPath);

          textView.string = title ++ body ++ example + credits + tags;

          // From Title to body
          textView.setFont(Font("Palatino", 48), 0, title.size - 4);
          textView.setStringColor(Color(0.42, 0.57, 0.7640), 0, title.size - 4);

          // From body to example
          textView.setFont(Font("Palatino", 18, italic: true), title.size - 4, 10000);
          textView.setStringColor(Color.black, title.size - 4, 10000);

          // From example to credits
          textView.setFont(Font("Menlo", 14), title.size + body.size - 4 , 10000 );
          textView.setStringColor( Color.grey, title.size + body.size - 4, 10000);

          //credits
          textView.setFont(Font("Palatino", 16), title.size + body.size + example.size - 4, 10000 );
          textView.setStringColor( Color.black, title.size + body.size + example.size - 4, 10000);


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
    this.runInit(PathName(path +/+ name));
    this.getInfo(name, \preview, path: path).interpret.value(this);
    if(File.exists(path +/+ name ++ "/cleanup.scd")){
      { 5.wait; this.runCleanUp(PathName(path +/+ name ++ "/")) }.fork;
    };
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
