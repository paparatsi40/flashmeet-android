// index.js
require("dotenv").config({ path: __dirname + "/.env" });

// IMPORTO V2 en lugar de V1
const { onCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");
admin.initializeApp();

const stripeSecret = process.env.STRIPE_SECRET;
const webhookSecret = process.env.STRIPE_WEBHOOK_SECRET;

if (!stripeSecret) throw new Error("Missing STRIPE_SECRET in .env");
if (!webhookSecret) {
  console.warn("[WARN] STRIPE_WEBHOOK_SECRET not set. Webhook verification will fail.");
}

const stripe = require("stripe")(stripeSecret);
const db = admin.firestore();

// Set global options for all 2nd Gen functions in this file
// You can configure CPU, memory, timeout, etc., here.
setGlobalOptions({
  region: "us-central1", // Moved region here
  // cpu: 1, // Example: Set CPU to 1 core (this is where your previous error came from)
  // memory: "512MiB", // Example: Set memory
  // maxInstances: 5 // Example: Control scaling
});

// ---- Gen 2 onCall ----
exports.createPaymentIntent = onCall(async (data, context) => { // Removed .region()
    try {
      if (!context.auth) {
        throw new HttpsError("unauthenticated", "Usuario no autenticado.");
      }

      const { amount, currency = "usd", purpose = "FlashMeet Promotion", metadata = {} } = data;
      if (!amount || amount <= 0) {
        throw new HttpsError("invalid-argument", "Monto inválido.");
      }

      const paymentIntent = await stripe.paymentIntents.create({
        amount,
        currency,
        description: purpose,
        metadata: { userId: context.auth.uid, ...metadata },
        automatic_payment_methods: { enabled: true }
      });

      // stub opcional
      try {
        await db.collection("payments").doc(paymentIntent.id).set({
          paymentId: paymentIntent.id,
          userId: context.auth.uid,
          eventId: metadata?.eventId || null,
          amount,
          currency,
          purpose,
          status: "created",
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        }, { merge: true });
      } catch (e) {
        console.warn("[payments] stub inicial falló:", e.message);
      }

      return { clientSecret: paymentIntent.client_secret, paymentId: paymentIntent.id };
    } catch (err) {
      console.error("createPaymentIntent error:", err);
      // Ensure you're throwing HttpsError from the *new* functions module if you still need that type
      throw new HttpsError("internal", err.message || "Error interno");
    }
  });

// ---- Gen 2 onRequest ----
exports.stripeWebhook = onRequest(async (req, res) => { // Removed .region() and .runWith()
    if (req.method !== "POST") return res.status(405).send("Method Not Allowed");
    if (!webhookSecret) return res.status(500).send("Webhook secret not configured");

    let event;
    const sig = req.headers["stripe-signature"];

    try {
      // For onRequest, make sure to use rawBody as Stripe expects it
      event = stripe.webhooks.constructEvent(req.rawBody, sig, webhookSecret);
    } catch (err) {
      console.error("Webhook signature verification failed:", err.message);
      return res.status(400).send(`Webhook Error: ${err.message}`);
    }

    try {
      if ([
        "payment_intent.succeeded",
        "payment_intent.payment_failed",
        "payment_intent.canceled",
        "payment_intent.processing",
        "payment_intent.requires_action"
      ].includes(event.type)) {
        const pi = event.data.object;
        const paymentId = pi.id;
        const status = pi.status;
        const userId = pi.metadata?.userId || null;
        const eventId = pi.metadata?.eventId || null;

        await db.collection("payments").doc(paymentId).set({
          paymentId,
          userId,
          eventId: eventId || null,
          amount: pi.amount || null,
          currency: pi.currency || "usd",
          purpose: pi.description || "FlashMeet Payment",
          status,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        }, { merge: true });

console.log("[stripeWebhook]", { type: event.type, paymentId, eventId, status });

        if (status === "succeeded" && eventId) {
          await db.collection("flyers").doc(eventId).set({
            promoted: true,
            lastPaymentId: paymentId,
            promotedAt: admin.firestore.FieldValue.serverTimestamp()
          }, { merge: true });
        }
      } else {
        console.log(`Unhandled event type ${event.type}`);
      }

      return res.json({ received: true });
    } catch (e) {
      console.error("stripeWebhook handler error:", e);
      return res.status(500).send("Internal error");
    }
  });
