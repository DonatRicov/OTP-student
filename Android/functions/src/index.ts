import * as admin from "firebase-admin";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { onCall, HttpsError } from "firebase-functions/v2/https";

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


export const submitQuizResult = onCall(async (request) => {
  const uid = request.auth?.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "User must be signed in.");
  }

  const challengeId = request.data?.challengeId as string | undefined;
  const selectedIndex = request.data?.selectedIndex as number | undefined;

  if (!challengeId) {
    throw new HttpsError("invalid-argument", "challengeId is required.");
  }
  if (typeof selectedIndex !== "number") {
    throw new HttpsError("invalid-argument", "selectedIndex (number) is required.");
  }

  const challengeRef = db.collection("challenges").doc(challengeId);
  const userRef = db.collection("users").doc(uid);
  const stateRef = userRef.collection("challengeStates").doc(challengeId);

  // 1) Ucitaj challenge (izvan transakcije) da znamo rewardPoints i provjerimo type
  const chDocOutside = await challengeRef.get();
  if (!chDocOutside.exists) {
    throw new HttpsError("not-found", "Challenge not found.");
  }

  const typeOutside = (chDocOutside.get("type") as string | undefined) ?? "";
  if (typeOutside !== "QUIZ_WEEKLY") {
    throw new HttpsError("failed-precondition", "Challenge is not a QUIZ_WEEKLY type.");
  }

  const rewardPointsOutside = (chDocOutside.get("rewardPoints") as number | undefined) ?? 0;

  // 2) Ucitaj 1. pitanje
  const qSnap = await challengeRef
    .collection("quizQuestions")
    .orderBy("order")
    .limit(1)
    .get();

  if (qSnap.empty) {
    throw new HttpsError("not-found", "Quiz question not found for this challenge.");
  }

  const qDoc = qSnap.docs[0];
  const correctIndex = qDoc.get("correctIndex");

  if (typeof correctIndex !== "number") {
    throw new HttpsError("failed-precondition", "Quiz question missing correctIndex.");
  }

  const isCorrect = selectedIndex === correctIndex;

  // 3) Transakcija: upis state + (ako tocno) ledger + points
  await db.runTransaction(async (tx) => {
    const [stateSnap, userSnap] = await Promise.all([
      tx.get(stateRef),
      tx.get(userRef),
    ]);

    const now = admin.firestore.Timestamp.now();

    // sprijeci dupli pokusaj
    if (stateSnap.exists) {
      const status = stateSnap.get("status") as string | undefined;
      if (status === "CLAIMED") {
        throw new HttpsError("failed-precondition", "Quiz already submitted.");
      }
      if (status === "EXPIRED") {
        throw new HttpsError("failed-precondition", "Quiz expired.");
      }
    }

    // uvijek markiraj kao CLAIMED da se sakrije u listi
    tx.set(
      stateRef,
      {
        status: "CLAIMED",
        claimedAt: now,
        updatedAt: now,
        quiz: {
          questionId: qDoc.id,
          selectedIndex,
          correct: isCorrect,
        },
      },
      { merge: true }
    );

    if (isCorrect) {
      if (rewardPointsOutside <= 0) {
        throw new HttpsError("failed-precondition", "Challenge has no rewardPoints.");
      }

      const ledgerRef = userRef.collection("pointsLedger").doc();
      tx.set(ledgerRef, {
        type: "QUIZ",
        amount: rewardPointsOutside,
        refType: "challenge",
        refId: challengeId,
        createdAt: now,
      });

      const currentBalance = (userSnap.get("pointsBalance") as number | undefined) ?? 0;
      tx.set(
        userRef,
        {
          pointsBalance: currentBalance + rewardPointsOutside,
          updatedAt: now,
        },
        { merge: true }
      );
    }
  });

  return { ok: true, correct: isCorrect, pointsAwarded: isCorrect ? rewardPointsOutside : 0 };
});