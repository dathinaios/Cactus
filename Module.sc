
Module { var <modulePath, <>soundProcess;

  *new { arg modulePath;
    modulePath = modulePath.standardizePath;
    ^super.newCopyArgs(modulePath);
  }

  run { arg name, environment; var path;
    environment.module = this;
    path = modulePath +/+ name +/+ "run.scd";
    ^path.load.valueWithEnvir(environment);
  }

}
