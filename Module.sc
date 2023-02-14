
Module { var <modulePath, <name, <arguments, <modules, <soundProcess;
         var <envir;

  *new { arg modulePath, name, arguments, modules;
    modulePath = modulePath.standardizePath;
    ^super.newCopyArgs(modulePath, name, arguments, modules).init.run;
  }

  init {
    envir = ();
  }

  run { var path;
    arguments = arguments.add(\module);
    arguments = arguments.add(this);
    arguments = arguments.add(\modules);
    arguments = arguments.add(modules);
    path = PathName(modulePath +/+ name);
    if(File.exists(path.fullPath), {
      soundProcess = this.runFile(path, "run");
    },{("Module"+name+"not installed.").error});
  }

  stop { var path;
    path = modulePath +/+ name;
    path = PathName(path);
    this.runFile(path, "stop");
  }

  runFile { arg folder, file; var path;
    path = (folder.fullPath+/+file.asString++".scd");
    if (File.exists(path), {
      ^path.load.performKeyValuePairs(\value, arguments);
    });
  }

  free {
    this.stop;
  }

}
