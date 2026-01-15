import * as admin from "firebase-admin";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { SessionsClient } from "@google-cloud/dialogflow";

admin.initializeApp();
const db = admin.firestore();


function computeDeadline(now: FirebaseFirestore.Timestamp, claimWindowDays: number) {
  const ms = now.toMillis() + claimWindowDays * 24 * 60 * 60 * 1000;
  return admin.firestore.Timestamp.fromMillis(ms);
}


export const onAppliedJobCreated = onDocumentCreated(
  "users/{uid}/applied/{jobId}",
  async (event) => {
    const uid = event.params.uid as string;

    const chSnap = await db
      .collection("challenges")
      .where("active", "==", true)
      .where("type", "==", "APPLY_ONCE")
      .get();

    if (chSnap.empty) return;

    const now = admin.firestore.Timestamp.now();
    const batch = db.batch();

    chSnap.docs.forEach((chDoc) => {
      const claimWindowDays =
        (chDoc.get("claimWindowDays") as number | undefined) ??
        (chDoc.get("claimWindowDay") as number | undefined) ??
        30;

      const stateRef = db
        .collection("users")
        .doc(uid)
        .collection("challengeStates")
        .doc(chDoc.id);

      batch.set(
        stateRef,
        {
          status: "COMPLETED_PENDING_CLAIM",
          completedAt: now,
          claimDeadlineAt: computeDeadline(now, claimWindowDays),
          updatedAt: now,
        },
        { merge: true }
      );
    });

    await batch.commit();
  }
);


export const onAppliedInternshipCreated = onDocumentCreated(
  "users/{uid}/appliedInternships/{internshipId}",
  async (event) => {
    const uid = event.params.uid as string;

    const chSnap = await db
      .collection("challenges")
      .where("active", "==", true)
      .where("type", "==", "APPLY_ONCE")
      .get();

    if (chSnap.empty) return;

    const now = admin.firestore.Timestamp.now();
    const batch = db.batch();

    chSnap.docs.forEach((chDoc) => {
      const claimWindowDays =
        (chDoc.get("claimWindowDays") as number | undefined) ??
        (chDoc.get("claimWindowDay") as number | undefined) ??
        30;

      const stateRef = db
        .collection("users")
        .doc(uid)
        .collection("challengeStates")
        .doc(chDoc.id);

      batch.set(
        stateRef,
        {
          status: "COMPLETED_PENDING_CLAIM",
          completedAt: now,
          claimDeadlineAt: computeDeadline(now, claimWindowDays),
          updatedAt: now,
        },
        { merge: true }
      );
    });

    await batch.commit();
  }
);

export const claimChallenge = onCall(async (request) => {
  const uid = request.auth?.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "User must be signed in.");
  }

  const challengeId = request.data?.challengeId as string | undefined;
  if (!challengeId) {
    throw new HttpsError("invalid-argument", "challengeId is required.");
  }

  const challengeRef = db.collection("challenges").doc(challengeId);
  const userRef = db.collection("users").doc(uid);
  const stateRef = userRef.collection("challengeStates").doc(challengeId);

  await db.runTransaction(async (tx) => {
    const [chSnap, stateSnap, userSnap] = await Promise.all([
      tx.get(challengeRef),
      tx.get(stateRef),
      tx.get(userRef),
    ]);

    if (!chSnap.exists) {
      throw new HttpsError("not-found", "Challenge not found.");
    }

    const rewardPoints = (chSnap.get("rewardPoints") as number | undefined) ?? 0;
    if (rewardPoints <= 0) {
      throw new HttpsError("failed-precondition", "Challenge has no rewardPoints.");
    }

    if (!stateSnap.exists) {
      throw new HttpsError("failed-precondition", "Challenge is not completed for this user.");
    }

    const status = stateSnap.get("status") as string | undefined;
    if (status !== "COMPLETED_PENDING_CLAIM") {
      throw new HttpsError("failed-precondition", `Challenge not claimable. Current status: ${status}`);
    }

    const deadline = stateSnap.get("claimDeadlineAt") as FirebaseFirestore.Timestamp | undefined;
    const now = admin.firestore.Timestamp.now();
    if (deadline && deadline.toMillis() < now.toMillis()) {
      tx.update(stateRef, { status: "EXPIRED", updatedAt: now });
      throw new HttpsError("failed-precondition", "Claim window expired.");
    }

    const ledgerRef = userRef.collection("pointsLedger").doc();
    tx.set(ledgerRef, {
      type: "CLAIM",
      amount: rewardPoints,
      refType: "challenge",
      refId: challengeId,
      createdAt: now,
    });

    const currentBalance = (userSnap.get("pointsBalance") as number | undefined) ?? 0;
    tx.set(
      userRef,
      {
        pointsBalance: currentBalance + rewardPoints,
        updatedAt: now,
      },
      { merge: true }
    );

    tx.update(stateRef, { status: "CLAIMED", claimedAt: now, updatedAt: now });
  });

  return { ok: true };
});

export const dialogflowDetectIntent = onCall(
  { region: "us-central1", timeoutSeconds: 60 },
  async (request) => {

    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "User must be signed in.");
    }

    const message = (request.data?.message ?? "").toString().trim();
    if (!message) {
      throw new HttpsError("invalid-argument", "message is required.");
    }

    const projectId = process.env.GCLOUD_PROJECT;
    if (!projectId) {
      throw new HttpsError("internal", "Missing GCLOUD_PROJECT env.");
    }

    const client = new SessionsClient();
    const sessionPath = client.projectAgentSessionPath(projectId, uid);

    const [response] = await client.detectIntent({
      session: sessionPath,
      queryInput: {
        text: { text: message, languageCode: "hr" },
      },
    });

    const result: any = response.queryResult || {};
    const fulfillment = (result.fulfillmentText || "").trim();
    const kb = (result.knowledgeAnswers?.answers?.[0]?.answer || "").trim();

    return { reply: fulfillment || kb || "Ne znam odgovor na to." };
  }
);

