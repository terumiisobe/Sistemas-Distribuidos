
class Flights:
    all_flights = []

    def __init__(self):
        print("new flight instance")
        self.flight_id_control = 0

    def showAllFlights(self):
        if len(self.all_flights)==0:
            print("There are no flights today!\n")
            return
        for flight in self.all_flights:
            print flight

    def registerFlight(self):
        ori = raw_input("\nType origin: ")
        des = raw_input("\nType destination: ")
        dep = raw_input("\nType departure date: ")
        ret = raw_input("\nType return date: ")
        qty = raw_input("\nType quantity: ")
        pri = raw_input("\nType price: ")
        self.all_flights.append({'id':self.flight_id_control,'origin':ori,'destination':ori,'departure':dep,'returnDate':ret,'quantity':qty,'price':pri})
        self.flight_id_control+=1;

    def removeFlight(self, id):
        for flight in self.all_flights:
            if flight['id'] == id:
                self.all_flights.remove[flight]

    #def buyTicket(self, id):
