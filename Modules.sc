
Modules { var <modulesPath, globalPath, templateManager;

  *new { arg modulesPath;
    modulesPath = modulesPath.standardizePath;
    ^super.newCopyArgs(modulesPath).init;
  }

  // Public

  runInits { var path;
    path = PathName(modulesPath);
    path.folders.do({ arg folder; var initPath;
      initPath = PathName(folder.fullPath++"/init");
      initPath.files.do{ arg i;
        i.fullPath.load.value;
      };
    });
  }

  run { arg name, args; var path;
    path = modulesPath +/+ name +/+ "run.scd";
    ^path.load.valueWithEnvir(args);
  }

  getInfo { arg name, key, path = globalPath; var yamlDictionary;
    path = path +/+ name +/+ "info.yaml";
    yamlDictionary = path.standardizePath.parseYAMLFile;
    ^yamlDictionary.at(key.asString);
  }

  getModuleGlobalPath {arg name; var path;
    path = globalPath +/+ name;
    ^path;
  }

  runCleanups { var path;
    path = PathName(modulesPath);
    path.folders.do({ arg folder; var initPath;
      (folder.fullPath++"cleanup.scd").load;
    });
  }

  restart {
    this.runCleanups;
    this.runInits;
  }

  browse {
    if (File.exists(modulesPath), {
      this.browseFromPath(globalPath);
    },{
      File.mkdir(modulesPath);
      this.browseFromPath(globalPath);
    });
  }

  list { // assumes that we are pointing at a folder with valid modules
    this.browseFromPath(modulesPath);
  }

  // Private

  init {
    globalPath = Platform.userAppSupportDir ++ "/CactusModules";
    this.runInits;
  }

  // Manage CactusModules

  installGlobal {
    ("git clone https://github.com/dathinaios/CactusModules.git"
      + globalPath.escapeChar($ )).unixCmd;
  }

  updateGlobal {
    ("git -C" + globalPath.escapeChar($ ) + "pull").unixCmd;
  }

  browseFromPath { arg path;
    var window, listView, textView;
    var windowRect, previewButton, installButton, hackButton;
    var winWidth, winHeight;

    winWidth = 815;
    winHeight = 453;
    path = PathName(path);

    windowRect = Rect(
      GUI.window.screenBounds.width-winWidth*0.5,
      GUI.window.screenBounds.height-winHeight*0.5,
      winWidth, winHeight);
    window = Window.new( "Browser", windowRect, resizable: false).front;
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));
    // window.onClose = { "placeholder" };

    listView = EZListView.new(window,200@400);
    listView.font = Font("Monaco", 14);
    textView = TextView(window, 600@400).background_(Color.white);
    textView.editable = false;

    StaticText(window, Rect(width: 409, height: 40));
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
          var title, body, credits, tags;
          title = "🍃 " + this.getInfo(name, \name, path: path.fullPath) + "\n";
          body = "\n" + this.getInfo(name, \description, path: path.fullPath).stripWhiteSpace + "\n\n";
          credits = "Created by: " + this.getInfo(name, \author, path: path.fullPath).stripWhiteSpace + "\n";
          tags = "Tags: " + this.getInfo(name, \tags, path: path.fullPath);

          textView.string = title + body + credits + tags;

          textView.setFont(Font("Palatino", 48), 0, title.size - 4 );
          textView.setStringColor(Color(0.42, 0.57, 0.7640), 0, title.size - 4);

          textView.setFont(Font("Palatino", 22), title.size - 2, title.size + body.size - 4 );
          textView.setStringColor(Color.black, title.size - 2, title.size + body.size - 4);

          textView.setFont(Font("Palatino", 16), title.size + body.size -2 , 10000 );
          textView.setStringColor( Color.grey, title.size + body.size -2, 10000);

          previewButton.action = { "-> not implemented".postln; };
          installButton.action = {this.installModule(name, modulesPath)};
          hackButton.action = {this.hackModule(name, modulesPath); window.close};
        }
      );
    };

    listView.valueAction = 0;
  }

  installModule{ arg name, target, newName;

    if (File.exists(globalPath +/+ name.asString) or:{ newName.notNil }, {
      var sourcePath, targetPath;
      sourcePath = this.getModuleGlobalPath(name);
      targetPath = target +/+ name.asString;
      if(newName.notNil){ targetPath = target +/+ newName.asString; };
      if (File.exists(targetPath).not, {
        ("cp -R" + sourcePath.escapeChar($ ) + targetPath.escapeChar($ )).unixCmd;
        ("✅ Module" + targetPath.basename + "created succesfully.").postln;
      }, {
        "You are already using a module with that name!".error;
      });
    },{
      "There is no module with that name in the global repo!".error;
    });
  }

  hackModule { arg name, target;
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
      this.installModule(name, target, newName: textField.string.asSymbol);
    };

  }

}
