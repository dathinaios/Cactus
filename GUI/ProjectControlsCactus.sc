
ProjectControlsCactus : AbstractGUIComponentCactus { 

  var serverInfoRoutine;
  var <openButton, <label;

  createComponent {
    this.createServerControls;
  }

  createServerControls {
    // this.createLabel("Main Controls").align_(\left);
    openButton = Button(window, Rect(width: 80, height: 20) );
    openButton.states = [["Open Dir", Color.white, Color.grey]];
    openButton.canFocus = false;
    openButton.font_(options.font);
    openButton.action = {
    };

    label = StaticText(window, Rect( width: 180, height: 20));
    label.font_(options.font);
    label.stringColor_(Color.fromHexString("#A0A0A0"););
    label.string_("");
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
    ^80
  }

}

