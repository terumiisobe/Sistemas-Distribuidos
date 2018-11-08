# Server in python
import json
from flask import Flask, request
from Classes import Flights

flight = Flights()

def main():
    while True:
        app.run()
        user_input = raw_input(showOptions(0))
        # add new flight
        if user_input == "1":
            user_input = raw_input(showOptions(1))
            if user_input == "0":
                continue
            flight.registerFlight()

        # add new accomodation
        if user_input == "2":
            user_input = raw_input(showOptions(2))
            if user_input == "0":
                continue
            # registerAccomodation()

        # remove flight
        if user_input == "3":
            user_input = raw_input(showOptions(3))
            if user_input == "0":
                continue
            flight.removeFlight(user_input)

        # remove hotel
        if user_input == "4":
            user_input = raw_input(showOptions(4))
            if user_input == "0":
                continue
            # removeAccomodation()

        # show all flights
        if user_input == "5":
            flight.showAllFlights()

        # show all accomodation
        #if user_input == "6":
            #hotel.showAllHotels()

# show options for travel agency
def showOptions(option):
    cover = "\nWhat do you want to do? Press the number matching your choice.\n"
    options = "  1 - Add flight\n  2 - Add accomodation.\n  3 - Remove plane ticket.\n  4 - Remove accomodation.\n  5 - Show all flights.\n  6 - Show all hotels.\n"
    options1 = "\n**NEW FLIGHT**\nEnter flight information as below. Or press 0 to go back.\n"
    options2 = "\n**NEW ACCOMODATION**\nEnter accomodation information as below. Or press 0 to go back.\n"
    options3 = "Enter the ID of the flight you want to remove.\n"
    options4 = "Enter the ID of the hotel you want to remove.\n"

    if option == 0:
        print(cover)
        print(options)
    elif option == 1:
        print(options1)
    elif option == 2:
        print(options2)
    elif option == 3:
        print(options3)
    else:
        print(options4)

app = Flask(__name__)

if __name__ == "__main__":
    main()
    
@app.route('/searchFlights', methods=('POST','GET'))
def searchFlights():
    print("func serchFlights")
    data={"hey":"yo"}
    return json.dumps(data)
