package crypto

import (
	"bytes"
	"crypto/aes"
	"crypto/cipher"
	"encoding/base64"
	"strings"
	"testing"
)

func TestAesDecryptSupportsAllAESKeyLengths(t *testing.T) {
	for _, key := range []string{
		"0123456789abcdef",
		"0123456789abcdefghijklmn",
		"0123456789abcdefghijklmnopqrstuv",
	} {
		ciphertext := testEncrypt("campus-password", key, true)
		plaintext, err := AesDecrypt(ciphertext, key)
		if err != nil {
			t.Fatalf("key length %d: decrypt failed: %v", len(key), err)
		}
		if plaintext != "campus-password" {
			t.Fatalf("key length %d: got %q", len(key), plaintext)
		}
	}
}

func TestAesDecryptKeepsLegacyAES128Compatibility(t *testing.T) {
	key := "0123456789abcdefghijklmn"
	plaintext, err := AesDecrypt(testEncrypt("legacy-password", key, false), key)
	if err != nil {
		t.Fatalf("legacy decrypt failed: %v", err)
	}
	if plaintext != "legacy-password" {
		t.Fatalf("got %q", plaintext)
	}
}

func TestAesDecryptRejectsMalformedCiphertext(t *testing.T) {
	malformed := []string{
		base64.StdEncoding.EncodeToString([]byte("IV" + strings.Repeat("x", aes.BlockSize))),
		base64.StdEncoding.EncodeToString([]byte(strings.Repeat("x", aes.BlockSize+1))),
	}
	for _, ciphertext := range malformed {
		if _, err := AesDecrypt(ciphertext, "0123456789abcdef"); err == nil {
			t.Fatalf("expected malformed ciphertext to be rejected: %q", ciphertext)
		}
	}
}

func testEncrypt(plaintext, key string, withIV bool) string {
	blockKey := []byte(key)
	iv := make([]byte, aes.BlockSize)
	if !withIV {
		blockKey = blockKey[:aes.BlockSize]
		copy(iv, blockKey)
	}
	block, _ := aes.NewCipher(blockKey)
	padded := pkcs5Pad([]byte(plaintext), aes.BlockSize)
	ciphertext := make([]byte, len(padded))
	cipher.NewCBCEncrypter(block, iv).CryptBlocks(ciphertext, padded)
	if withIV {
		return base64.StdEncoding.EncodeToString(append(append([]byte("IV"), iv...), ciphertext...))
	}
	return base64.StdEncoding.EncodeToString(ciphertext)
}

func pkcs5Pad(data []byte, blockSize int) []byte {
	padding := blockSize - len(data)%blockSize
	return append(append([]byte(nil), data...), bytes.Repeat([]byte{byte(padding)}, padding)...)
}
