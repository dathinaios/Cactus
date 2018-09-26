
CactusTemplateManager {

  *new {
    ^super.newCopyArgs.init;
  }

  init {
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
      ("Renamed it to" + targetFilePath.basename ++ ".bkp.").postln;
    });
  }

  runTemplate { arg templateName, options = (); var path;
    path = PathName.new(File.getcwd++"/templates/"++ templateName);
    this.copyTemplateBaseFiles(path, options);
    this.copyTemplateSubFolderFiles(path, options);
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

  copyTemplateSubFolderFiles { arg path, options;
    path.folders.do{ arg folder;
      folder.files.do{ arg file;
        this.createFromTemplateFile(
          sourcePath: file,
          targetDir: options.targetDir +/+ file.folderName,
          options: options
        );
      };
    };
    
  }

}