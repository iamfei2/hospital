import CryptoJS from 'crypto-js'

const SALT = 'hospital_salt_2024'

export function encryptPassword(password) {
  return CryptoJS.MD5(password + SALT).toString()
}

export function encryptData(data) {
  return CryptoJS.AES.encrypt(JSON.stringify(data), SALT).toString()
}

export function decryptData(ciphertext) {
  const bytes = CryptoJS.AES.decrypt(ciphertext, SALT)
  return JSON.parse(bytes.toString(CryptoJS.enc.Utf8))
}
