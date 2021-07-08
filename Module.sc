
Module { var <modulePath, <name, <environment, <soundProcess;

  *new { arg modulePath, name, environment;
    modulePath = modulePath.standardizePath;
    ^super.newCopyArgs(modulePath, name, environment).run;
  }

  run { var path;
    environment.module = this;
    path = modulePath +/+ name;
    this.runInit(PathName(path));
    soundProcess = (path +/+ "run.scd").load.valueWithEnvir(environment);
  }

  cleanup { var path;
    path = modulePath +/+ name;
    path = PathName(path);
    (path.fullPath+/+"cleanup.scd").load.valueWithEnvir(environment);
  }

  runInit{ arg folder;
    (folder.fullPath+/+"init.scd").load.valueWithEnvir(environment);
  }

  free {
    this.cleanup;
  }

  stop {
    this.free;
  }

}
