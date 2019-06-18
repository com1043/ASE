window.addEventListener('load', function () {
  // At first, let's check if we have permission for notification
  // If not, let's ask for it
  if (Notification && Notification.permission !== "granted") {
    Notification.requestPermission(function (status) {
      if (Notification.permission !== status) {
        Notification.permission = status;
      }
    });
  }

  var button = document.getElementsByTagName('button')[0];

  button.addEventListener('click', function () {
    // If the user agreed to get notified
    if (Notification && Notification.permission === "granted") {
      var n = new Notification("Hi!");
    }

    // If the user haven't tell if he want to be notified or not
    // Note: because of Chrome, we are not sure the permission property
    // is set, therefore it's unsafe to check for the "default" value.
    else if (Notification && Notification.permission !== "denied") {
      Notification.requestPermission(function (status) {
        if (Notification.permission !== status) {
          Notification.permission = status;
        }

        // If the user said okay
        if (status === "granted") {
          var n = new Notification("Hi!");
        }

        // Otherwise, we can fallback to a regular modal alert
        else {
          alert("Hi!");
        }
      });
    }

    // If the user refuse to get notified
    else {
      // We can fallback to a regular modal alert
      alert("Hi!");
    }
  });
});