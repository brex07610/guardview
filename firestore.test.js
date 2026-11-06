const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const USER_UID = "guard_alice_123";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read sites", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("sites").get());
});

test("Unauthenticated user: cannot create checkin", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("checkins").doc("chk_1").set({
      guardId: "guard_1",
      siteId: "site_1",
      type: "clock_in",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can read sites", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("sites").doc("site_metro").set({
      name: "Metro Plaza",
      address: "100 Main St",
      clientId: "client_1",
      createdAt: new Date(),
    });
  });

  const authDb = testEnv.authenticatedContext(USER_UID).firestore();
  await assertSucceeds(authDb.collection("sites").get());
});

test("Authenticated user: can create valid checkin", async () => {
  const authDb = testEnv.authenticatedContext(USER_UID).firestore();
  await assertSucceeds(
    authDb.collection("checkins").doc("chk_valid_1").set({
      guardId: USER_UID,
      siteId: "site_metro",
      type: "clock_in",
      guardName: "Alice Guard",
      siteName: "Metro Plaza",
      isWithinGeofence: true,
      lat: 37.7749,
      lng: -122.4194,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create invalid checkin (bad type)", async () => {
  const authDb = testEnv.authenticatedContext(USER_UID).firestore();
  await assertFails(
    authDb.collection("checkins").doc("chk_invalid_1").set({
      guardId: USER_UID,
      siteId: "site_metro",
      type: "invalid_clock_type",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can create complaint ticket", async () => {
  const authDb = testEnv.authenticatedContext(USER_UID).firestore();
  await assertSucceeds(
    authDb.collection("tickets").doc("ticket_1").set({
      siteId: "site_metro",
      category: "guard_absent",
      status: "open",
      description: "Guard did not arrive for scheduled 08:00 shift",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can create ticket with all optional fields", async () => {
  const authDb = testEnv.authenticatedContext(USER_UID).firestore();
  await assertSucceeds(
    authDb.collection("tickets").doc("tkt_full_1").set({
      clientId: "client_metro",
      siteId: "site_metro",
      siteName: "Metro Plaza",
      category: "guard_absent",
      description: "Guard missing at morning gate opening",
      status: "open",
      supervisorName: "Capt. Ramos",
      firstResponseTargetMinutes: 30,
      resolutionTargetMinutes: 240,
      satisfactionRating: 0,
      autoEvidenceText: "Shift #101 scheduled 06:00, no checkin recorded.",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});
