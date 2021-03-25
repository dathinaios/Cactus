
ProjectControlsCactus : AbstractGUIComponentCactus {

  var serverInfoRoutine;
  var <openButton, <label, <restartButton,
      <modulesButton, <buffersButton, <helpButton;

  createComponent {
    this.createServerControls;
  }

  createServerControls {
    this.createLabel("----------------", height: 10, width: 256).align_(\center);
    this.createLabel("🌵", height: 65, width: 256)
    .align_(\center)
    .font_(Font("Lucida Grande", 60));
    this.createLabel("----------------", height: 10, width: 256).align_(\center);
    this.createLabel("", height: 10, width: 256).align_(\center);
    label = StaticText(window, Rect( width: 185, height: 20));
    label.align_(\center);
    label.font_(options.projectNameFont);
    label.stringColor_(options.projectNameColor);
    label.string_("");

    openButton = Button(window, Rect(width: 59, height: 20) );
    openButton.states = [["Open Dir", Color.white, Color.grey]];
    openButton.canFocus = false;
    openButton.font_(options.font);

    this.createLabel("", height: 10).align_(\center);

    restartButton = Button(window, Rect(width: 59, height: 20) );
    restartButton.states = [["Restart", Color.white, Color.grey]];
    restartButton.canFocus = false;
    restartButton.font_(options.font);

    modulesButton = Button(window, Rect(width: 59, height: 20) );
    modulesButton.states = [["Modules", Color.white, Color.grey]];
    modulesButton.canFocus = false;
    modulesButton.font_(options.font);

    buffersButton = Button(window, Rect(width: 59, height: 20) );
    buffersButton.states = [["Buffers", Color.white, Color.grey]];
    buffersButton.canFocus = false;
    buffersButton.font_(options.font);

    helpButton = Button(window, Rect(width: 59, height: 20) );
    helpButton.states = [["Help", Color.white, Color.grey]];
    helpButton.canFocus = false;
    helpButton.font_(options.font);

  }

  runResources {
  }

  clear {
  }

  cmdPeriodAction {
  }

  windowName {
    ^"Cactus"
  }

  windowHeight {
    ^180
  }

}

