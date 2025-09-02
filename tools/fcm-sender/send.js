const admin = require('firebase-admin');
const svc = require('./service-account.json'); // NO se sube a Git
admin.initializeApp({ credential: admin.credential.cert(svc) });

const token = process.env.FCM_TOKEN; // pasa el token por env var
if (!token) { console.error('Falta FCM_TOKEN'); process.exit(1); }

async function main() {

if (!token || token.length < 100) {
  console.error('Token inválido o muy corto:', token);
  process.exit(1);
}
console.log('Usando token (len):', token.length);

  const res = await admin.messaging().send({
    token,
    data: {
      type: 'event',
      eventId: 'flashmeettest1',
      lat: '-34.6037',
      lon: '-58.3816',
      title: '¡Nuevo evento cerca!',
      body: 'Toca para ver detalles'
    },
    android: { priority: 'high' }
  });
  console.log('Sent:', res);
  process.exit(0);
}
main().catch(err => { console.error(err); process.exit(1); });
