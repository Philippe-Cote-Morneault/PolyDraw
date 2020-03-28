package context

type key int

const (
	//CtxUserID represent the user id of the context
	CtxUserID key = 0
	//CtxLang represent the language of the context
	CtxLang key = 1
)
