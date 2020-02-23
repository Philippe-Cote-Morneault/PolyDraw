package datastore

import (
	"bufio"
	"crypto/sha256"
	"encoding/hex"
	"github.com/google/uuid"
	"github.com/spf13/viper"
	"io"
	"io/ioutil"
	"os"
)

//GetFile used to get the file name don't forget to close the file
func GetFile(key string) (*bufio.Reader, error) {
	file, err := os.Open(GetPath(key))
	if err != nil {
		return nil, err
	}
	return bufio.NewReader(file), err
}

//PostFile used to post the file name
func PostFile(reader io.Reader) (string, error) {
	fileName := GenerateFileKey()
	file, err := os.OpenFile(GetPath(fileName), os.O_WRONLY|os.O_CREATE, 0666)
	defer file.Close()
	if err != nil {
		return "", err
	}

	_, err = io.Copy(file, reader)
	if err != nil {
		return "", err
	}

	return fileName, nil
}

func PutFile(data *[]byte, key string) error {
	filePath := GetPath(key)
	return ioutil.WriteFile(filePath, *data, 0644)
}

//GenerateFileKey is used to generate a unique filename
func GenerateFileKey() string {
	//Generate a unique file name
	uniqueID := uuid.New()
	bytes, err := uniqueID.MarshalBinary()
	if err != nil {
		panic(err)
	}
	hashBytes := sha256.Sum256(bytes)

	fileName := hex.EncodeToString(hashBytes[:])
	return fileName
}

//GetPath is used to return the global path of a key
func GetPath(key string) string {
	return viper.GetString("datastore") + key
}
