const admin = require("firebase-admin");
const functions = require("firebase-functions");

admin.initializeApp();

exports.checkRides = functions.pubsub.schedule("every 1 hours").onRun(
  async () => {
    const ridesRef = admin.database().ref("rides");
    const snapshot = await ridesRef.once("value");
    const currentTime = new Date().getTime();

    snapshot.forEach((rideSnapshot) => {
      const ride = rideSnapshot.val();
      const rideTime = new Date(ride.date).getTime();
      if (ride.active && rideTime < currentTime) {
        rideSnapshot.ref.update({active: false});
      }
    });
    return null;
});
