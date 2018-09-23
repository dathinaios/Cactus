
CactusTemplateManager { var <templatesDir;

  *new { arg templatesDir;
    ^super.newCopyArgs(templatesDir).init;
  }

  init {
    templatesDir = templatesDir ++ "/templates";
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

  createFileFromTemplate {
    arg templateName, targetDir, options;
    var targetFilePath, generatedString;

    targetFilePath = targetDir ++ "/" ++ templateName ++ ".scd";
    generatedString = this.argFormatFile(
      path: templatesDir ++ "/" ++ templateName++ ".scd",
      options: options
    );
    if ( File.exists(targetFilePath).not, {
      File.new(targetFilePath.standardizePath, "w").write(generatedString).close;
      ("File" + targetFilePath + "created from template" + templateName).postln;
    },{"File at" + targetDir + "already exists!".postln});
  }

}