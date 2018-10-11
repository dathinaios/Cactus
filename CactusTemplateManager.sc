
CactusTemplateManager { var <cactusDir, <templatesDir;

  *new {
    ^super.newCopyArgs.init;
  }

  init {
    cactusDir = Cactus.filenameSymbol.asString.dirname;
    templatesDir = cactusDir++"/templates/";
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

  runTemplate { arg templateName, options = (); var path;
    path = PathName.new(templatesDir ++ templateName);
    this.copyTemplateBaseFiles(path, options);
    this.copyTemplateSubFolderFiles(path, options);
    this.copyModules(path, options);
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

}