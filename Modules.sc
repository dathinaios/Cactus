
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
    this.browseFromPath(globalPath);
  }

  list {
    this.browseFromPath(modulesPath);
  }

  hack { arg name;
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
    var window, gui, textView;
    var windowRect;

    path = PathName(path);

    windowRect = Rect(
      GUI.window.screenBounds.width*0.5, 
      GUI.window.screenBounds.height*0.5, 
      815, 400);
    window = Window.new( "Browser", windowRect, resizable: false).front;
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));
    // window.onClose = { "placeholder" };

    gui = EZListView.new(window,200@400);
    gui.font = Font("Monaco", 14);
    textView = TextView(window, 600@400).background_(Color.white);
    textView.editable = false;
    // textView.font = Font("Monaco", 14);
    path.folders.do{ arg item; var name;
      name = item.folderName;
      gui.addItem(
        this.getInfo(name, \name),
        { 
          var title, body, credits, tags;
          title = "🍃 " + this.getInfo(name, \name) + "\n";
          body = "\n" + this.getInfo(name, \description).stripWhiteSpace + "\n\n";
          credits = "Created by: " + this.getInfo(name, \author).stripWhiteSpace + "\n";
          tags = "Tags: " + this.getInfo(name, \tags);

          textView.string = title + body + credits + tags;

          textView.setFont(Font("Palatino", 48), 0, title.size - 4 );
          textView.setStringColor(Color(0.42, 0.57, 0.7640), 0, title.size - 4);

          textView.setFont(Font("Palatino", 22), title.size - 2, title.size + body.size - 4 );
          textView.setStringColor(Color.black, title.size - 2, title.size + body.size - 4);

          textView.setFont(Font("Palatino", 16), title.size + body.size -2 , 10000 );
          textView.setStringColor( Color.grey, title.size + body.size -2, 10000);
        }
      );
    };
    gui.valueAction = 0;
  }

  installModule{ arg name, target;
    if (File.exists(globalPath +/+ name.asString), {
      var sourcePath, targetPath;
      sourcePath = this.getModuleGlobalPath(name);
      targetPath = target +/+ name.asString;
      if (File.exists(targetPath).not, {
        ("cp -R" + sourcePath.escapeChar($ ) + targetPath.escapeChar($ )).unixCmd;
        ("Module" + name + "installed succesfully 👍").postln;
      }, {
        "You are already using a module with that name!".error;
      });
    },{
      "There is no module with that name in the global repo!".error;
    });
  }

}
