
CactusGUI {

  var cactus, options;
  var <window, name;
  var <serverWindow;
  var <windowHeight = 0, font, titleFontSize, marginTop, <active = false;

  *new { arg cactus, options = ();
    ^super.newCopyArgs(cactus, options).init;
  }

  init {
    Server.default.waitForBoot{
      name = "Cactus";

      this.setDefaultOptions;
      this.initStyleVariables;
      this.createMainWindow;
      if(options.serverControls) { this.createServerControls };
      if(options.shortcuts) { this.registerShortcuts };

      active = true;
      window.bounds.height = windowHeight;
      window.bounds = window.bounds.height_(windowHeight + 10);
      window.front;
    }
  }

  setDefaultOptions {
    options.placeholderOption ?? { options.placeholderOption = "testing" };
    options.left ?? { options.left = GUI.window.screenBounds.width - 282 };
    options.top ?? { options.top = GUI.window.screenBounds.height - 330 };
    options.serverControls ?? { options.serverControls = true };
    options.shortcuts ?? { options.shortcuts = true };
  }

  initStyleVariables {
    font = "Lucida Grande";
    titleFontSize = 11;
    marginTop = 3;
  }

  createMainWindow {
    window = Window.new(name, Rect(options.left, options.top, 282, 330), resizable: false);
    window.view.decorator = FlowLayout( window.view.bounds );
    window.background_(Color.fromHexString("#282828"));
    window.onClose = {
      serverWindow.clear;
      cactus.removeDependant(this);
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

  /* -------- */

  createServerControls {
    serverWindow = ServerWindowCactus(window, options: (font: Font(font, titleFontSize)));
    windowHeight = windowHeight + serverWindow.windowHeight;
  }

  /* Handle Events from Dependants */

  update { arg theChanged, message;
    switch (message)
    {\somethingFromDependants}
    {"this.setCurrent(theChanged)"}
  }

}
