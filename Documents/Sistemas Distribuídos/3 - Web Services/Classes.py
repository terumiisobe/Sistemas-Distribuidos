
class Flights:
    all_flights = []

    def __init__(self):
        print("new flight instance")
        self.flight_id_control = 1

    def showAllFlights(self):
        if len(self.all_flights) == 0:
            print("There are no flights today!\n")
            return
        for flight in self.all_flights:
            print("\t\t--**--")
            for f in flight:
                print("\t\t" + str(f) + ": " + str(flight[f]))
        print("\t\t--**--\n")

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
            if str(flight.get('id')) == id:
                self.all_flights.remove(flight)
                print("Flight with id " + id + " removed!\n")

    def buy(self, id, number):
        for flight in self.all_flights:
            if flight['id'] == id:
                exists = True
                print flight['id']
                print flight['quantity']
                q = int(flight['quantity'])
                # not enought tickets
                if q < number:
                    return 0
                q-=number
                flight['quantity'] = q
                if flight['quantity'] == 0:
                    this.removeFlight(id)
                return 1
        # ticket doesn't exists
        if exists == False:
            return 2

class Hotels:
    all_hotels = []

    def __init__(self):
        print("new hotel instance")
        self.hotel_id_control = 1

    def showAllHotels(self):
        if len(self.all_hotels) == 0:
            print("There are no hotels today!\n")
            return
        for hotel in self.all_hotels:
            print("\t\t--**--")
            for h in hotel:
                print("\t\t" + str(h) + ": " + str(hotel[h]))
        print("\t\t--**--\n")

    def registerHotel(self):
        loc = raw_input("\nType location: ")
        num = raw_input("\nType number of rooms: ")
        pri = raw_input("\nType price of room: ")
        self.all_hotels.append({'id':self.hotel_id_control,'location':loc,'number':num,'price':pri})
        self.hotel_id_control+=1;

    def removeHotel(self, id):
        for hotel in self.all_hotels:
            if str(hotel['id']) == id:
                self.all_hotels.remove(hotel)
                print("Hotel with id " + id + " removed!\n")

    def buy(self, id, number):
        for hotel in self.all_hotels:
            if hotel['id'] == id:
                exists = True
                print hotel['id']
                print hotel['number']
                q = int(hotel['number'])
                # not enought tickets
                if q < number:
                    return 0
                q-=number
                hotel['number'] = str(q)
                if hotel['number'] == 0:
                    this.removeHotel(id)
                return 1
        # rooms doesn't exists
        if exists == False:
            return 2
