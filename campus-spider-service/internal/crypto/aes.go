package crypto

import (
	"crypto/aes"
	"crypto/cipher"
	"encoding/base64"
	"errors"
)

// AesDecrypt 使用 AES/CBC/PKCS5Padding 解密。
// 新格式将随机 IV 前置在密文中；旧格式仍兼容使用 key 前 16 字节作为 IV。
func AesDecrypt(encryptedBase64, key string) (string, error) {
	ciphertext, err := base64.StdEncoding.DecodeString(encryptedBase64)
	if err != nil {
		return "", err
	}

	if len(ciphertext) < aes.BlockSize {
		return "", errors.New("ciphertext too short")
	}

	if len(key) != 16 && len(key) != 24 && len(key) != 32 {
		return "", errors.New("AES key must be 16, 24, or 32 bytes")
	}

	originalCiphertext := append([]byte(nil), ciphertext...)
	if len(ciphertext) >= 2 && ciphertext[0] == 'I' && ciphertext[1] == 'V' {
		if len(ciphertext) >= 2+aes.BlockSize*2 {
			iv := append([]byte(nil), ciphertext[2:2+aes.BlockSize]...)
			newCiphertext := append([]byte(nil), ciphertext[2+aes.BlockSize:]...)
			if plaintext, decryptErr := decryptCBC(newCiphertext, []byte(key), iv); decryptErr == nil {
				return plaintext, nil
			}
		}
	}

	// Legacy Java clients always used AES-128 with the first 16 key bytes.
	legacyKey := []byte(key)[:aes.BlockSize]
	return decryptCBC(originalCiphertext, legacyKey, legacyKey)
}

func decryptCBC(ciphertext, blockKey, iv []byte) (string, error) {
	if len(ciphertext) == 0 || len(ciphertext)%aes.BlockSize != 0 {
		return "", errors.New("ciphertext is not a multiple of the AES block size")
	}
	if len(iv) != aes.BlockSize {
		return "", errors.New("invalid IV length")
	}
	block, err := aes.NewCipher(blockKey)
	if err != nil {
		return "", err
	}
	mode := cipher.NewCBCDecrypter(block, iv)
	decrypted := append([]byte(nil), ciphertext...)
	mode.CryptBlocks(decrypted, decrypted)

	plaintext, err := pkcs5Unpadding(decrypted, aes.BlockSize)
	if err != nil {
		return "", err
	}
	return string(plaintext), nil
}

func pkcs5Unpadding(data []byte, blockSize int) ([]byte, error) {
	length := len(data)
	if length == 0 {
		return nil, errors.New("empty data")
	}
	unpadding := int(data[length-1])
	if unpadding > blockSize || unpadding == 0 {
		return nil, errors.New("invalid padding")
	}
	for _, value := range data[length-unpadding:] {
		if int(value) != unpadding {
			return nil, errors.New("invalid padding")
		}
	}
	return data[:(length - unpadding)], nil
}
