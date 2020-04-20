
Modules { var <modulesPath, templateManager;

  *new { arg modulesPath;
    modulesPath = modulesPath.standardizePath;
    ^super.newCopyArgs(modulesPath).init;
  }

  init {
    this.runModuleInits;
  }

  // Public

  run { arg name, args; var path;
    path = modulesPath +/+ name +/+ "run.scd";
    ^path.load.valueWithEnvir(args);
  }

  restart {
    this.runModuleCleanups;
    this.runModuleInits;
  }

  getInfo { arg name, key; var path, yamlDictionary;
    path = modulesPath +/+ name +/+ "info.yaml";
    yamlDictionary = path.standardizePath.parseYAMLFile;
    ^yamlDictionary.at(key.asString);
  }

  hack { arg name;
  }

  gui { }

  // Private

  runModuleCleanups { var path;
    path = PathName(modulesPath);
    path.folders.do({ arg folder; var initPath;
      (folder.fullPath++"cleanup.scd").load;
    });
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

  // Drafts

  listModulesGUI {
    this.listGUI(modulesPath);
  }

  listGUI { arg path, action; var gui, infoGUI, infoWin;
    path = PathName(path);
    gui = EZListView.new(nil,200@200, "");
    gui.font = Font("Monaco", 11);
    infoWin = Window( "Info",
      Rect(
        gui.window.bounds.left+gui.window.bounds.width,
        gui.window.bounds.top-200, 400, 400),
        scroll: true
      ).front;
      infoGUI = StaticText(infoWin, Rect(10, 10, 380, 380));
      infoGUI.font = Font("Monaco", 14);
      path.folders.do{ arg item; var name, info;
        name = item.folderName;
        gui.addItem(
          this.getInfo(name, \name),
          {
            infoGUI.string =
            this.getInfo(name, \name) + "\n\n" +
            this.getInfo(name, \description) + "\n\n" +
            "Created by: " + this.getInfo(name, \author) + "\n\n" +
            "Tags: " + this.getInfo(name, \tags)

          }
        );
      };
      gui.valueAction = 0;
  }

}
