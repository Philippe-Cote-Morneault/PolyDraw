package model

import (
	"log"
	"time"

	_ "github.com/go-sql-driver/mysql" //Need to import it for gorm mysql support
	"github.com/google/uuid"
	"github.com/jinzhu/gorm"
	_ "github.com/jinzhu/gorm/dialects/sqlite" //Need to import it for gorm sqlite support

	"github.com/spf13/viper"
)

//Base model to use with every model
type Base struct {
	ID        uuid.UUID `gorm:"type:uuid;primary_key;"`
	CreatedAt time.Time
	UpdatedAt time.Time
	DeletedAt *time.Time `sql:"index"`
}

// BeforeCreate will set a UUID rather than numeric ID.
func (base *Base) BeforeCreate(scope *gorm.Scope) error {
	uuid := uuid.New()
	return scope.SetColumn("ID", uuid)
}

//DB used for assignement
var dbVariable *gorm.DB

//DBConnect connect to the database
func DBConnect() {
	db, err := gorm.Open(viper.GetString("database.engine"), viper.GetString("database.connection"))
	if err != nil {
		log.Println(err)
		log.Fatal("failed to connect database")
	}
	dbVariable = db

	migrate()
	log.Println("Migration complete!")
}

//DB Return the database object
func DB() *gorm.DB {
	if dbVariable != nil {
		return dbVariable
	}
	return nil
}

//DBClose disconnect the database
func DBClose() {
	if dbVariable != nil {
		dbVariable.Close()
	}
}

//migrate run database migration for the database
func migrate() {
	log.Println("Migrating database")
	dbVariable.AutoMigrate(&User{})
}
