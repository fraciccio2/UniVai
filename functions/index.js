const admin = require("firebase-admin");

admin.initializeApp();

const database = admin.database();

function isDateBeforeNow(date) {
  return new Date(date) < new Date();
}

function updateActiveStatus(key, value) {
  database.ref("rides/" + key).update({
    active: value,
  }, (error) => {
    if (error) {
      console.error("Error updating \"active\" status:", error);
    } else {
      console.log("Updated \"active\" status:", key, value);
    }
  });
}

function checkDates() {
  database.ref("rides").once("value", (snapshot) => {
    snapshot.forEach((childSnapshot) => {
      const key = childSnapshot.key;
      const date = childSnapshot.child("date").val();
      const active = childSnapshot.child("active").val();

      if (active === false) {
        return;
      }

      if (isDateBeforeNow(date)) {
        updateActiveStatus(key, false);
      }
    });
  });
}

setInterval(checkDates, 60 * 60 * 1000);
