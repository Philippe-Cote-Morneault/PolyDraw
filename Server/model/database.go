package model

import (
	"log"
	"time"

	"github.com/google/uuid"
	"github.com/jinzhu/gorm"
	_ "github.com/jinzhu/gorm/dialects/postgres" //Need to import it for gorm mysql support
	_ "github.com/jinzhu/gorm/dialects/sqlite"   //Need to import it for gorm sqlite support
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
var dbVar *gorm.DB

//DBConnect connect to the database
func DBConnect() {
	db, err := gorm.Open(viper.GetString("database.engine"), viper.GetString("database.connection"))
	if err != nil {
		log.Println(err)
		log.Fatal("failed to connect database")
	}
	dbVar = db

	migrate()
	log.Println("Migration complete!")
}

//DB Return the database object
func DB() *gorm.DB {
	if dbVar != nil {
		return dbVar
	}
	return nil
}

//DBClose disconnect the database
func DBClose() {
	if dbVar != nil {
		dbVar.Close()
	}
}

//migrate run database migration for the database
func migrate() {
	log.Println("Migrating database")
	//Users
	dbVar.AutoMigrate(&User{})

	dbVar.AutoMigrate(&Session{})
	dbVar.Model(&Session{}).AddForeignKey("user_id", "users(id)", "CASCADE", "RESTRICT")

	//Chat
	dbVar.AutoMigrate(&ChatChannel{})
	dbVar.Exec("INSERT INTO chat_channels (id,name) values('00000000-0000-0000-0000-000000000000', 'Général') ON CONFLICT DO NOTHING;")

	dbVar.AutoMigrate(&ChatMessage{})
	dbVar.Model(&ChatMessage{}).AddForeignKey("channel_id", "chat_channels(id)", "CASCADE", "RESTRICT")
	dbVar.Model(&ChatMessage{}).AddForeignKey("user_id", "users(id)", "CASCADE", "RESTRICT")

	//Stats
	dbVar.AutoMigrate(&Stats{})
	dbVar.AutoMigrate(&Connection{})
	dbVar.AutoMigrate(&MatchPlayed{})
	dbVar.AutoMigrate(&PlayerName{})
	dbVar.AutoMigrate(&Achievement{})

	//Game
	dbVar.AutoMigrate(&Game{})
	dbVar.AutoMigrate(&GameHint{})
	dbVar.Model(&GameHint{}).AddForeignKey("game_id", "games(id)", "CASCADE", "RESTRICT")

}
