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

	var blockKey []byte
	iv := []byte(key)[:aes.BlockSize]
	if len(ciphertext) >= 2 && ciphertext[0] == 'I' && ciphertext[1] == 'V' {
		if len(ciphertext) < 2+aes.BlockSize*2 {
			return "", errors.New("new-format ciphertext too short")
		}
		iv = append([]byte(nil), ciphertext[2:2+aes.BlockSize]...)
		ciphertext = ciphertext[2+aes.BlockSize:]
		blockKey = []byte(key)
	} else {
		// Legacy Java clients always used AES-128 with the first 16 key bytes.
		blockKey = []byte(key)[:aes.BlockSize]
	}
	if len(ciphertext) == 0 || len(ciphertext)%aes.BlockSize != 0 {
		return "", errors.New("ciphertext is not a multiple of the AES block size")
	}
	block, err := aes.NewCipher(blockKey)
	if err != nil {
		return "", err
	}
	mode := cipher.NewCBCDecrypter(block, iv)
	mode.CryptBlocks(ciphertext, ciphertext)

	plaintext, err := pkcs5Unpadding(ciphertext, aes.BlockSize)
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
