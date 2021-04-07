
Module { var <modulePath, <>soundProcess;

  *new { arg modulePath;
    modulePath = modulePath.standardizePath;
    ^super.newCopyArgs(modulePath);
  }

  run { arg name, args; var path;
    args.module = this;
    path = modulePath +/+ name +/+ "run.scd";
    ^path.load.valueWithEnvir(args);
  }

}
