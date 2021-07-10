
Module { var <modulePath, <name, <arguments, <soundProcess;

  *new { arg modulePath, name, arguments;
    modulePath = modulePath.standardizePath;
    ^super.newCopyArgs(modulePath, name, arguments).run;
  }

  run { var path;
    arguments = arguments.add(\module);
    arguments = arguments.add(this);
    path = modulePath +/+ name;
    this.runFile(PathName(path), "init");
    soundProcess = this.runFile(PathName(path), "run");
  }

  cleanup { var path;
    path = modulePath +/+ name;
    path = PathName(path);
    this.runFile(path, "cleanup");
  }

  runFile { arg folder, file;
    ^(folder.fullPath+/+file.asString++".scd")
      .load.performKeyValuePairs(\value, arguments);
  }

  free {
    this.cleanup;
  }

  stop {
    this.free;
  }

}
