
CactusTemplateManager { var <>templatesDir;

  *new { arg templatesDir;
    ^super.newCopyArgs(templatesDir).init;
  }

  init {
    templatesDir ?? {
      templatesDir = CactusTemplateManager.filenameSymbol.asString.dirname;
      templatesDir = templatesDir ++ "/templates/"
    };
  }

  runTemplate { arg templateName, options = (); var path;
    path = PathName.new(templatesDir ++ templateName);
    this.copyTemplateBaseFiles(path, options);
    this.copyTemplateSubFolderFiles(path, options);
    this.copyModules(path, options);
  }

  argFormatFile { arg path, options;
    File.use(path.standardizePath, "r", {
      arg file; var string;
      string = file.readAllString;
      options.pairsDo{ arg key, value;
        string = string.replace(key.asString, value.asString);
      };
      ^string;
    });
  }

  createFromTemplateFile {
    arg sourcePath, targetDir, options;
    var targetFilePath, generatedString;

    targetFilePath = targetDir ++ "/" ++ sourcePath.fileName;
    generatedString = this.argFormatFile(
      path: sourcePath.fullPath,
      options: options
    );
    if ( File.exists(targetFilePath).not, {
      File.new(targetFilePath.standardizePath, "w").write(generatedString).close;
      ("File" + targetFilePath.basename + "created").postln;
    },{
      File.copy(
        targetFilePath.standardizePath,
        targetFilePath.standardizePath ++ ".bkp"++(120000.rand)
      );
      File.new(targetFilePath.standardizePath, "w").write(generatedString).close;
      ("A file named" + targetFilePath.basename + "was already there!").postln;
      ("Renamed it to" + targetFilePath.basename ++ ".bkp").postln;
    });
  }

  copyTemplateBaseFiles { arg path, options;
    path.files.do{ arg file;
      if(file.extension != "txt")
      {
        this.createFromTemplateFile(
          sourcePath: file,
          targetDir: options.targetDir,
          options: options
        );
      }
    };
  }

  copyTemplateSubFolderFiles { arg path, options;
    path.folders.do{ arg folder;
      folder.files.do{ arg file;
        if(file.extension != "txt")
        {
          this.createFromTemplateFile(
            sourcePath: file,
            targetDir: options.targetDir +/+ file.folderName,
            options: options
          );
        }
      };
    };
  }

  copyModules { arg path, options;
    path = path +/+ "modules";
    path.folders.do{ arg folder; var sourceDir, targetDir;
      sourceDir = folder.fullPath;
      targetDir = options.targetDir ++ "/modules/" ++ folder.folderName;
      ("cp -r" + sourceDir + targetDir).unixCmd({
        arg code;
        if(code == 0, {
          ("succesfully copied module" + sourceDir.basename).postln;
        },{
          ("Could not copy module" + sourceDir.basename).postln;
        });
      }, false);
    };
  }

  gui {
    this.listGUI(templatesDir);
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

}
