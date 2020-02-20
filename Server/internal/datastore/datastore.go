package datastore

import (
	"bufio"
	"crypto/sha256"
	"encoding/hex"
	"github.com/google/uuid"
	"github.com/spf13/viper"
	"io"
	"os"
)

//GetFile used to get the file name don't forget to close the file
func GetFile(key string) (*bufio.Reader, error) {
	file, err := os.Open(getPath(key))
	if err != nil {
		return nil, err
	}
	return bufio.NewReader(file), err
}

//PostFile used to post the file name
func PostFile(reader io.Reader) (string, error) {
	fileName := generateFileName()
	file, err := os.OpenFile(getPath(fileName), os.O_WRONLY|os.O_CREATE, 0666)
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

func generateFileName() string {
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

func getPath(key string) string {
	return viper.GetString("datastore") + key
}
