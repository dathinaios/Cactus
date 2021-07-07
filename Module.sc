
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
    this.runCleanUp(PathName(path));
  }

  runInit{ arg folder;
    (folder.fullPath+/+"init.scd").load;
  }

  runCleanUp { arg folder;
    (folder.fullPath+/+"cleanup.scd").load;
  }

}
