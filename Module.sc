
Module { var <modulePath, <name, <arguments, <soundProcess;

  *new { arg modulePath, name, arguments;
    modulePath = modulePath.standardizePath;
    ^super.newCopyArgs(modulePath, name, arguments).run;
  }

  run { var path;
    arguments = arguments.insert(0, this);
    arguments = arguments.insert(0, \module);
    path = modulePath +/+ name;
    this.runInit(PathName(path));
    soundProcess = (path +/+ "run.scd").load.performKeyValuePairs(\value, arguments);
  }

  cleanup { var path;
    path = modulePath +/+ name;
    path = PathName(path);
    (path.fullPath+/+"cleanup.scd").load.performKeyValuePairs(\value, arguments);
  }

  runInit{ arg folder;
    (folder.fullPath+/+"init.scd").load.performKeyValuePairs(\value, arguments);
  }

  free {
    this.cleanup;
  }

  stop {
    this.free;
  }

}
