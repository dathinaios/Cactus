
CactusGUI {

  var path, options;
  var <cactus;
  var <window, name;
  var <projectControls, serverWindow;
  var <windowHeight = 0, font, titleFontSize, marginTop, <active = false;

  *new { arg path, options = ();
    ^super.newCopyArgs(path, options).init;
  }

  init {
    if (path.isNil, {
      this.popUpWarning(
        string: "Would you like to open or create a project?",
        action1: { this.initDialog(0) },
        action2: { this.initDialog(1) },
        text1: "Open",
        text2: "Create")
      },
      {
        cactus = Cactus(path);
        this.run;
      }
    );
  }

  run {
    name = "Cactus";
    this.setDefaultOptions;
    this.initStyleVariables;
    this.createMainWindow;
    this.createProjectControls;
    if(options.shortcuts) { this.registerShortcuts };
    active = true;
    window.bounds.height = windowHeight;
    window.bounds = window.bounds.height_(windowHeight + 10);
    window.front;
  }

  initDialog { arg mode = 0;
      FileDialog(
        okFunc: { arg path;
          cactus = Cactus(path);
          this.run;
          ("You could also open this project by running: \n"++
          "c = CactusGUI(\""++path++"\");").postln;
        },
        fileMode: 2,
        stripResult: true,
        acceptMode: mode
      );
  }

  setDefaultOptions {
    options.placeholderOption ?? { options.placeholderOption = "testing" };
    options.left ?? { options.left = GUI.window.screenBounds.width*0.5 - 141 };
    options.top ?? { options.top = GUI.window.screenBounds.height*0.5 + 165 };
    options.serverControls ?? { options.serverControls = false };
    options.shortcuts ?? { options.shortcuts = true };
  }

  initStyleVariables {
    font = "Lucida Grande";
    titleFontSize = 11;
    marginTop = 3;
  }

  createMainWindow {
    var winWidth, winHeight, windowRect;

    winWidth = 256;
    winHeight = 330;

    windowRect = Rect(
      GUI.window.screenBounds.width-winWidth*0.5,
      GUI.window.screenBounds.height*0.5,
      winWidth, winHeight);

    window = Window.new(name, windowRect, resizable: false);
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));
    window.onClose = {
      serverWindow.clear;
      cactus.clear;
      active = false;
    };
  }

  registerShortcuts {
    window.view.keyDownAction = {
      arg view, char, modifiers, unicode, keycode;
      /* [char, modifiers, unicode, keycode].postln; */
      switch(unicode)
      {79}  { cactus.openProjectDir }         //O     - open project Dir
    };
  }

  createLabel { arg text = "placeholder text", width = 280, height = 20; var label;
    label = StaticText(window, Rect( width: width, height: height));
    label.font_(Font(font, titleFontSize));
    label.stringColor_(Color.fromHexString("#A0A0A0"););
    label.string_(text);
    ^label;
  }

  /* -------------- */
  // GUI Components //
  /* -------------- */

  createProjectControls {
    projectControls = ProjectControlsCactus(
      window,
      options: (
        projectNameFont: Font("Lucida Grande", 15),
        projectNameColor: Color.white )
    );
    windowHeight = windowHeight + projectControls.windowHeight;
    projectControls.openButton.action = { cactus.openProjectDir };
    projectControls.label.string_("Project:" + cactus.projectName.asString.toUpper);
    projectControls.restartButton.action = { cactus.restart };
    projectControls.buffersButton.action = { cactus.listBuffers };
    projectControls.modulesButton.action = { cactus.browseModules };
    projectControls.browseButton.action = { cactus.modules.browse };
  }

  popUpWarning {

    arg string="", action1, action2, text1="1", text2 = "2";
    var dialog;
    var buttonColor, destructiveButtonColor, backgroundColor, listsColor,
        buttonTextColor, destructiveButtonTextColor, textBoxColor;
    var winWidth, winHeight, windowRect;

    winWidth = 280;
    winHeight = windowHeight;

    windowRect = Rect(
      GUI.window.screenBounds.width-winWidth*0.5,
      GUI.window.screenBounds.height-winHeight*0.5,
      winWidth, winHeight);

    buttonColor = Color.new255(106,106,126);
    buttonTextColor = Color.white;
    destructiveButtonColor = Color.new255(106,106,126);
    destructiveButtonTextColor = Color.white;
    backgroundColor = Gradient(Color.new255(168, 183, 194),Color.new255(39, 43, 52),  \v, 1280);
    listsColor = Gradient(Color.white, Color.new255(168,173,194), \v, 1280);
    textBoxColor = Color.new255(168,173,194);

    dialog = Window.new("", windowRect, border: false).front;

    StaticText.new(dialog,Rect(30, 10, 220, 50))
    .string_(string)
    .align_(\left);

    Button.new(dialog,Rect(30, 70, 100, 20))
    .states_([ [text1, destructiveButtonTextColor, destructiveButtonColor] ])
    .action_{|v|
      action1.value;
      dialog.close;
    };

    Button.new(dialog,Rect(150, 70, 100, 20))
    .states_([ [text2, buttonTextColor, buttonColor] ])
    .action_{|v|
      action2.value;
      dialog.close;
    };

  }
}
