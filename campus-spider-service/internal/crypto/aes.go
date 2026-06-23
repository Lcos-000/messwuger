package crypto

import (
	"crypto/aes"
	"crypto/cipher"
	"encoding/base64"
	"errors"
)

// AesDecrypt 使用 AES/CBC/PKCS5Padding 解密
// key 必须为 16、24 或 32 字节长度；IV 取 key 的前 16 字节
func AesDecrypt(encryptedBase64, key string) (string, error) {
	ciphertext, err := base64.StdEncoding.DecodeString(encryptedBase64)
	if err != nil {
		return "", err
	}

	block, err := aes.NewCipher([]byte(key))
	if err != nil {
		return "", err
	}

	if len(ciphertext) < aes.BlockSize {
		return "", errors.New("ciphertext too short")
	}

	iv := []byte(key)[:aes.BlockSize]
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
	return data[:(length - unpadding)], nil
}
