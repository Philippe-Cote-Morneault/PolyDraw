package service

//Service interface used to represent a service
type Service interface {
	//Start the service
	Start()

	//Shutdown the service
	Shutdown()

	//Init anything that needs to be created before start is called
	Init()

	//Register all the broadcast this must be called before init
	Register()
}

var services []Service

//Add a service to the pool of services
func Add(service Service) {
	service.Register()
	services = append(services, service)
}

//StartAll the services in the pool
func StartAll() {
	//Before starting all the services we need to init all one of them
	for _, service := range services {
		service.Init()
	}

	for _, service := range services {
		service.Start()
	}
}

//ShutdownAll the services in the pool
func ShutdownAll() {
	for _, service := range services {
		service.Shutdown()
	}
}
