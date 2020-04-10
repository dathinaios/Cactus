
Modules { var <modulesPath, templateManager;

  *new { arg modulesPath;
    modulesPath = modulesPath.standardizePath;
    ^super.newCopyArgs(modulesPath).init;
  }

  init {
    this.runModuleInits;
    this.initTemplateManager;
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

  hack { arg name;
  }

  gui {
  }

  // Private

  initTemplateManager {
    templateManager = CactusTemplateManager.new;
  }

  // Drafts

  listModules { var path;
    path = PathName(modulesPath);
    this.printNewLine;
    path.folders.do{ arg i; i.folderName.postln};
    this.printNewLine;
  }

  listModulesGUI {
    this.listGUI(modulesPath);
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

    // gatherBuffersFromModules { arg bufferArray;
    //   PathName(modulesPath).folders.do{
    //     arg folder; var newBufs;
    //     newBufs = this.collectIntoBuffers(folder.fullPath ++ "/buffers");
    //     bufferArray = bufferArray.addAll(newBufs);
    //   };
    //   ^bufferArray;
    // }

  }