
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
    // this.copyTemplateSubFolderFiles(path, options);
    // this.copyModules(path, options);
  }

  copyTemplateBaseFiles { arg path, options;
    path.files.do{ arg file;
      this.createFromTemplateFile(
        sourcePath: file,
        targetDir: options.targetDir,
        options: options
      );
    };
  }

  createFromTemplateFile {
    arg sourcePath, targetDir, options;
    var targetFilePath, generatedFileContent;

    targetFilePath = targetDir ++ "/" ++ sourcePath.fileName;
    targetFilePath = targetFilePath.standardizePath;
    generatedFileContent = this.parseTemplateFile(
      path: sourcePath.fullPath,
      options: options);
    this.carefullyCreateFile(targetFilePath, generatedFileContent)
  }

  carefullyCreateFile{ arg targetFilePath, generatedFileContent;
    if (File.exists(targetFilePath).not, {
      this.createTheFile(targetFilePath, generatedFileContent)
    },{
      this.createBackupFileCopy(targetFilePath, generatedFileContent);
      this.createTheFile(targetFilePath, generatedFileContent);
    });
  }

  createTheFile { arg targetFilePath, generatedFileContent;
      File.new(targetFilePath.standardizePath, "w").write(generatedFileContent).close;
      ("File" + targetFilePath.basename + "has been created.").postln;
  }

  createBackupFileCopy { arg targetFilePath;
      File.copy(
        targetFilePath.standardizePath,
        targetFilePath.standardizePath ++ "." ++ UniqueID.next ++ ".bkp");
      ("A file named" + targetFilePath.basename + "was already there!").postln;
      ("Renamed old file to" + targetFilePath.basename ++ ".bkp").postln;
  }

  parseTemplateFile { arg path, options;
    File.use(path.standardizePath, "r", {
      arg file; var string;
      string = file.readAllString;
      options.pairsDo{ arg key, value;
        string = string.replace("{{" ++ key.asString ++ "}}", value.value.asString);
      };
      ^string;
    });
  }

  // obsolete

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

}
