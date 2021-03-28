
CactusTemplateManager { var <cactus, <templatesDir;

  *new { arg cactus;
    ^super.newCopyArgs(cactus).init;
  }

  init {
    templatesDir = CactusTemplateManager.filenameSymbol.asString.dirname;
    templatesDir = templatesDir ++ "/templates/"
  }

  runTemplate { arg templateName, options = (); var path;
    path = PathName.new(templatesDir ++ templateName);
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
      folder.files.do{ arg file; var target;
        target = options.targetDir +/+ file.folderName;
        File.mkdir(target.standardizePath);
        this.createFromTemplateFile(
          sourcePath: file,
          targetDir: target,
          options: options
        );
      };
    };
  }

  createFromTemplateFile {
    arg sourcePath, targetDir, options;
    var targetFilePath, generatedFileContent;

    targetFilePath = targetDir +/+ sourcePath.fileName;
    targetFilePath = targetFilePath.standardizePath;
    generatedFileContent = this.parseTemplateFile(
      path: sourcePath.fullPath,
      options: options);
    this.carefullyCreateFile(targetFilePath, generatedFileContent)
  }

  carefullyCreateFile{ arg targetFilePath, generatedFileContent;
    if (File.exists(targetFilePath).not
        or:{File.readAllString(targetFilePath).size == 0}, {
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
        targetFilePath.standardizePath.splitext[0] ++ "-" ++ Date.getDate.stamp ++ ".scd");
      ("-> A file named" + targetFilePath.basename + "was already there!").postln;
      "-> Renamed old file".postln;
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

  // Drafts

  gui {
    this.browseFromPath(templatesDir;);
  }

  browseFromPath { arg path;
    var window, listView, textView;
    var windowRect, applyButton, cancelButton;
    var winWidth, winHeight, rawPath = path;

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

    listView = EZListView.new(window,200@400);
    listView.font = Font("Monaco", 14);
    textView = TextView(window, 600@400).background_(Color.white);
    textView.editable = false;

    StaticText(window, Rect(width: 540 , height: 40));

    cancelButton = Button(window, Rect(width: 128, height: 40) );
    cancelButton.states = [["Cancel", Color.white, Color.grey]];
    cancelButton.canFocus = false;

    applyButton = Button(window, Rect(width: 128, height: 40) );
    applyButton.states = [["Apply", Color.white, Color.grey]];
    applyButton.canFocus = false;

    path.folders.do{ arg item; var name;
      name = item.folderName;
      listView.addItem(
        this.getInfo(name, \name, path: path.fullPath),
        {
          var title, body, example, credits, tags;
          title = "📜 " + this.getInfo(name, \name, path: path.fullPath) + "\n";
          body = "\n" + this.getInfo(name, \description, path: path.fullPath).stripWhiteSpace + "\n\n";
          textView.string = title ++ body;
          // From Title to body
          textView.setFont(Font("Palatino", 48), 0, title.size - 4);
          textView.setStringColor(Color.fromHexString("#9d817f"), 0, title.size - 4);
          // From body to end
          textView.setFont(Font("Palatino", 20, italic: true), title.size - 4, 10000);
          textView.setStringColor(Color.black, title.size - 4, 10000);
          cancelButton.action = {window.close};
          applyButton.action = {cactus.runTemplate(name.asSymbol); window.close;};
        }
      );
    };

    if(listView.items.size > 0, { listView.valueAction = 0 });
  }

  getInfo { arg name, key, path = templatesDir; var yamlDictionary;
    path = path +/+ name +/+ "info.yaml";
    yamlDictionary = path.standardizePath.parseYAMLFile;
    ^yamlDictionary.at(key.asString);
  }


}
