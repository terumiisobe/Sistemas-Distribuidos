# Server in python
import json
import thread
from flask import Flask, request
from Classes import Flights

flight = Flights()

app = Flask(__name__)

@app.route('/searchFlights', methods=['GET','POST'])
def searchFlights():

    print("Called function /serchFlights")

    # no flight tickets available
    if(len(flight.all_flights) == 0):
        data = {'Status':'There are no tickets available :('}
        return json.dumps(data)
    data = {}
    for f in flight.all_flights:
        data[f['id']]=f
    return json.dumps(data)

@app.route('/buyTicket/<int:ticket_id>/<int:number>',methods=['GET','POST'])
def buyTicket(ticket_id, number):
    status = flight.buy(ticket_id, number)
    if status == 1:
        data = {'Status':'**Ticket(s) bought sucessfully!'}
        print(str(number) + " flight ticket(s) bought from id " + str(ticket_id))
    elif status == 0:
        data = {'Status':'**There are not enought tickets available :('}
    else:
        data = {'Status':'**This ticket does not exists :('}
    return json.dumps(data)



# thread with menu options for travel agency
def system():
    while True:
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

# prints options for travel agency
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
    elif option == 4:
        print(options4)
    else:
        print("else error")


if __name__ == "__main__":
    try:
        thread.start_new_thread( system, () )
        app.run()
    except:
        print "Error: unable to start thread"
    while 1:
        pass
