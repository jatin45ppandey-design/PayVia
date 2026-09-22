const DB_NAME = 'PayViaCryptoStore';
const STORE_NAME = 'keys';

function getDB(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, 1);
    request.onupgradeneeded = () => {
      if (!request.result.objectStoreNames.contains(STORE_NAME)) {
        request.result.createObjectStore(STORE_NAME);
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

async function saveKey(deviceId: string, keyPair: CryptoKeyPair) {
  const db = await getDB();
  return new Promise<void>((resolve, reject) => {
    const transaction = db.transaction(STORE_NAME, 'readwrite');
    const store = transaction.objectStore(STORE_NAME);
    const request = store.put(keyPair, deviceId);
    request.onsuccess = () => resolve();
    request.onerror = () => reject(request.error);
  });
}

export async function getExistingKeyPair(deviceId: string): Promise<CryptoKeyPair | null> {
  const db = await getDB();
  return new Promise((resolve, reject) => {
    const transaction = db.transaction(STORE_NAME, 'readonly');
    const store = transaction.objectStore(STORE_NAME);
    const request = store.get(deviceId);
    request.onsuccess = () => resolve(request.result || null);
    request.onerror = () => reject(request.error);
  });
}

export async function clearKeyPair(deviceId: string): Promise<void> {
  const db = await getDB();
  return new Promise((resolve, reject) => {
    const transaction = db.transaction(STORE_NAME, 'readwrite');
    const store = transaction.objectStore(STORE_NAME);
    const request = store.delete(deviceId);
    request.onsuccess = () => resolve();
    request.onerror = () => reject(request.error);
  });
}

function arrayBufferToBase64(buffer: ArrayBuffer): string {
  let binary = '';
  const bytes = new Uint8Array(buffer);
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

export async function generateKeyPairAndExport(): Promise<{ keyPair: CryptoKeyPair, publicKeyBase64: string }> {
  const keyPair = await window.crypto.subtle.generateKey(
    {
      name: 'ECDSA',
      namedCurve: 'P-256',
    },
    false, // non-extractable private key
    ['sign', 'verify']
  );

  const exportedPublic = await window.crypto.subtle.exportKey('spki', keyPair.publicKey);
  const publicKeyBase64 = arrayBufferToBase64(exportedPublic);

  return { keyPair, publicKeyBase64 };
}

export async function storeKeyPair(deviceId: string, keyPair: CryptoKeyPair): Promise<void> {
  await saveKey(deviceId, keyPair);
}

export async function signPayload(deviceId: string, payload: string): Promise<string> {
  const keyPair = await getExistingKeyPair(deviceId);
  if (!keyPair) {
    throw new Error('No device key pair found for this device ID. Please register this browser first.');
  }

  const encoder = new TextEncoder();
  const data = encoder.encode(payload);

  const signatureBuffer = await window.crypto.subtle.sign(
    {
      name: 'ECDSA',
      hash: { name: 'SHA-256' },
    },
    keyPair.privateKey,
    data
  );

  return arrayBufferToBase64(signatureBuffer);
}