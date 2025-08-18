const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Inicializa Firebase Admin (opcional si necesitas acceder a Firestore, autenticación, etc.)
admin.initializeApp();

// Obtiene la clave secreta de Stripe desde la configuración segura de Firebase
const stripe = require("stripe")(functions.config().stripe.secret);

exports.createPaymentIntent = functions.https.onCall(async (data, context) => {
  const { amount, currency = "usd" } = data;

  if (!amount || amount <= 0) {
    return { success: false, error: "Monto inválido." };
  }

  try {
    const paymentIntent = await stripe.paymentIntents.create({
      amount: amount,
      currency: currency,
      automatic_payment_methods: { enabled: true }, // activa métodos de pago automáticamente
      // Puedes agregar metadata aquí, por ejemplo eventId: data.eventId
    });

    return {
      success: true,
      clientSecret: paymentIntent.client_secret,
      paymentIntentId: paymentIntent.id
    };
  } catch (error) {
    console.error("Error en Stripe:", error.message);
    return { success: false, error: error.message };
  }
});
